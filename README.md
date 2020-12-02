# Coded Dependency Injection

Can be used if reflection and annotation processing is not allowed or possible.
Minimum Java version is 8. 
Useful with Java compact profiles or for Java SE Embedded projects. 
Also injectors in a server session context are supported, e.g., for Servlets.

For example A depends on B and C:

```
A -> B, C
```

```Java
class B {
	B(int arg1, int arg2) {...}
	void foo() {...}
}

class C {
}

class A implements Dependent {
	Dependency<B> b = new Dependency<>(this, B.class);
	Dependency<C> c = new Dependency<>(this, C.class);
	...
	b.get().foo(); // b is a proxy to the service object of type B
}

Injector.getContext("myapp")	// creates a named dependency injector
	.makeBeans(A.class);	// creates the beans and injects them accordingly

```

## Proxy Based Injection

An application context is represented by a named `Injection` instance.
The dependency injector named 'myapp' is created with `Injector.getContext("myapp")`. 
Beans of an application context are always referenced by their classes.
  
The setup of the graph and the implicit injection happens as soon as the client bean `A` (see above) is instantiated by the injector.
The constructor of `A` instantiates its `Dependency` members `b, c`, which act as a proxy to the service bean. 
With that `Dependency<B> b` returns with `b.get()` the service bean object of type `B`.

The `Dependency` constructor expects as first argument the client bean as type of the interface `Dependent`, 
it is internally used to track the dependencies of every bean. 
Second constructor argument is the class of the service bean, it is used to retrieve the service bean from the injector. 
Every service bean in turn can implement interface `Dependent` and declaring `Dependency` members and so forth.  
This results in a cascading creation of the complete dependency graph with root bean `A` when `Injector.getContext("myapp").makeBeans(A.class)` is executed, see also [Wikipedia: Dependency Injection](https://en.wikipedia.org/wiki/Dependency_injection).

Within the `Injector` is every bean identified by its class. For example to retrieve bean `A` use  
`Injector.getContext("myapp").getBean(A.class);`.

## IoC

Lambdas can be used for construction and the lifecycle of beans.

An optional bean construction `Supplier` can be defined for its class or interface (see also [lifecycle](#lifecycle-of-beans) example).
With that no reflection is needed to create objects. For example Java compact profiles can omit reflection completely, 
in that case for every bean a construction supplier has to be defined, see also [JCP](https://www.oracle.com/java/technologies/javase-embedded/compact-profiles-overview.html).
If not defined, the default constructor of a bean is invoked using `Class#newInstance()`. 

Example:

```Java
int val1=0, val2=0;

Injector.getContext("myapp")
	.defineConstruction(A.class, A::new) // only needed if reflection should/cannot be used
	.defineConstruction(B.class, () -> new B(val1, val2))
	.makeBeans(A.class)
	...
```

Classes are treated as singleton beans within the scope of a named dependency injector. 
That means for every class one object is created within an `Injector` context.
For example with `A -> B` and `A2 -> B`, the 3 instances `A, B, A2` would be created by:

```Java
Injector.getContext("app")
	.makeBeans(A.class)
	.makeBeans(A2.class);
	
A a = Injector.getContext("app").getBean(A.class);
A2 a2 = Injector.getContext("app").getBean(A2.class);
B b = Injector.getContext("app").getBean(B.class);
```

Optionally the basic lifecycle of beans can be controlled by `Injector#start()` and `Injector#stop()`.
With that the injector invokes the start/stop consumer of a bean if it was defined by `Injector#defineStart()`
`Injector#defineStop()`. 
Alternatively the interface `Lifecycle` can be implemented, see also the [lifecycle](#lifecycle-of-beans) example below.

## Features

It is light weight, fast, debugable and transparent.

Cyclic dependencies are prohibited.

Type safety when retrieving beans from the injection context.

Dependency graph is printable.

Individual construction suppliers for instance creation can be defined.

Individual consumers for starting beans can be defined.

Individual consumers for stopping beans can be defined.

Multiple independent injector instances (application contexts), e.g., for Servlet sessions, are possible.

## Limitations

Supports only field injection.

As the system configuration is coded, it cannot be changed without compilation.

## Anonymous Client

A client bean that is instantiated by the application and not by the injector, can also use class `Dependency` to declare dependencies to service beans. For this an additional constructor declares the injection context. But, the client object is then unknown for the Injector, for example:

```Java
public class MyAnonymousApp implements Dependent {
	Dependency<A> a = new Dependency<>("appcontext", this, A.class);
}

MyAnonymousApp app = new MyAnonymousApp(); // not instantiated by Injector

assertNull(Injector.getContext("appcontext") // MyAnonymousApp is 'unknown' for the Injector
	.getBean(MyAnonymousApp.class));

A a = app.a.get(); // injection was done

assertTrue(a == Injector.getContext("appcontext") // and A is 'known' for the Injector
	.getBean(A.class));
```

This can be useful for interim migration steps. Recommended is to use non anonymous beans with `Injector#makeBeans()`.

# Examples

## Lifecycle of Beans

For example: `MyApp -> MyService`

```Java
interface MyApp extends Dependent, Lifecycle {
	public boolean isRunning();
}

interface MyService extends Lifecycle {
	public boolean isRunning();
	public String greets();
}

class MyServiceImpl implements MyService {
	private boolean isRunning;

	@Override // interface Lifecycle
	public void start() {
		isRunning = true;
		System.out.println("MyService started.");
	}

	@Override // interface Lifecycle
	public void stop() {
		isRunning = false;
		System.out.println("MyService stopped.");
	}

	@Override
	public String greets() {
		return "hello, MyService is ready to go";
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

}

class MyAppImpl implements MyApp {
	private boolean isRunning;
	
	Dependency<MyService> svc = new Dependency<>(this, MyService.class); // svc.get() can also be inlined

	@Override
	public void start() {
		isRunning = true;
		String svcGreets = svc.get()
			.greets();
		System.out.println("MyApp started, greets from svc: " + svcGreets);
	}

	@Override
	public void stop() {
		isRunning = false;
		System.out.println("MyApp stopped");
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}
}
```

For every interface that is used in a `Dependency` declaration, a construction supplier has to be defined.
See for example `defineConstruction(MyService.class, MyServiceImpl::new)`.

```Java
Injector injector = .getContext("myapp");
injector.defineConstruction(MyApp.class, MyAppImpl::new)
	.defineConstruction(MyService.class, MyServiceImpl::new)
	.makeBeans(MyApp.class)
	.start();

assertTrue(injector.getBean(MyApp.class)
	.isRunning());
assertTrue(injector.getBean(MyService.class)
	.isRunning());

injector.stop();

assertFalse(injector.getBean(MyApp.class)
	.isRunning());
assertFalse(injector.getBean(MyService.class)
	.isRunning());
```

## springframework.guru Example

See `coded.dependency.injection.example.springfw.guru.DiExampleApplication`, ported from 
[springframework.guru](https://springframework.guru/dependency-injection-example-using-spring/).


## vogella.com Example

See `coded.dependency.injection.example.vogella.Main`, ported from
[vogella.com](https://www.vogella.com/tutorials/SpringDependencyInjection/article.html)
