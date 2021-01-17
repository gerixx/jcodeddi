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
Second constructor argument is the class of the service bean, it is used to retrieve the service bean from the injector, 
which constructs the bean if is not existing yet. 
Every service bean in turn can implement interface `Dependent` and declaring `Dependency` members and so forth.  
This results in a cascading creation of the complete dependency graph with root bean `A` when `Injector.getContext("myapp").makeBeans(A.class)` is executed, see also [Wikipedia: Dependency Injection](https://en.wikipedia.org/wiki/Dependency_injection).

Within the `Injector` is every bean identified by its class. For example to retrieve bean `A` use  
`Injector.getContext("myapp").getBean(A.class);`.

## IoC

Lambdas can be used for construction of beans.

An optional bean construction `Supplier` can be defined for its class or interface (see also [lifecycle](#lifecycle-of-beans) example).
With that no reflection is needed to create objects. For example Java compact profiles can omit reflection completely, 
in that case for every bean a construction supplier has to be defined, see also [JCP](https://www.oracle.com/java/technologies/javase-embedded/compact-profiles-overview.html).
If no construction is defined for a bean class, the default constructor of is invoked by `Class#newInstance()`. 

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
With that the injector invokes the start/stop methods of a bean if it implements 
the interface `Lifecycle`, see also the [lifecycle](#lifecycle-of-beans) example below.

## Features

It is light weight, fast, debugable and transparent.

Cyclic dependencies are prohibited.

Type safety when retrieving beans from the injection context.

Dependency graph is printable.

Individual construction suppliers for instance creation can be defined.

Life cycle support for starting and stopping beans.

Multiple independent injector instances (application contexts), e.g., for Servlet sessions, are possible.

Logs injection use in production code with class and line number.

## Limitations

Supports only field injection.

As the system configuration is coded, it cannot be changed without compilation.

## Anonymous Client

A client bean that is instantiated by the application and not by the injector, can also use class `Dependency` to declare dependencies to service beans. For this an additional `Dependency` constructor defines the injection context. But, that means also the client object is 'unknown' for the Injector.

Example:

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

Example:

```
coded.dependency.injection.MyApp (coded.dependency.injection.MyAppImpl)
  -> MyServiceImpl (coded.dependency.injection.MyServiceImpl)
    -> HelperProcessStarter (coded.dependency.injection.HelperProcessStarter)
```

```Java
interface MyApp extends Dependent, Lifecycle {
	public boolean isRunning();
}

interface MyService extends Lifecycle {
	public boolean isRunning();
	public String greets();
}

class HelperProcessStarter implements Lifecycle {
	private boolean isRunning;

	@Override
	public void start() {
		if (isRunning) {
			throw new IllegalStateException("already running");
		}
		isRunning = true;
		System.out.println("SomeHelperProcess is started");
	}

	@Override
	public void stop() {
		if (!isRunning) {
			throw new IllegalStateException("already stopped");
		}
		isRunning = false;
		System.out.println("SomeHelperProcess stopped");
	}

	public boolean isRunning() {
		return isRunning;
	}
}

class MyServiceImpl implements Dependent, MyService {

	HelperProcessStarter extSvc = new Dependency<>(this, HelperProcessStarter.class).get();

	private boolean isRunning;

	@Override
	public void start() {
		if (isRunning) {
			throw new IllegalStateException("already running");
		}

		if (!extSvc.isRunning()) {
			throw new IllegalStateException("HelperProcess must run");
		}

		isRunning = true;
		System.out.println("MyService started.");
	}

	@Override
	public void stop() {
		if (!isRunning) {
			throw new IllegalStateException("already stopped");
		}

		if (!extSvc.isRunning()) {
			throw new IllegalStateException("HelperProcess must run");
		}

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

	MyService svc = new Dependency<>(this, MyService.class).get();

	@Override
	public void start() {
		if (isRunning) {
			throw new IllegalStateException("already running");
		}
		if (!svc.isRunning()) {
			throw new IllegalStateException("svc is not running");
		}
		isRunning = true;
		String svcGreets = svc.greets();
		System.out.println("MyApp started, greets from svc: " + svcGreets);
	}

	@Override
	public void stop() {
		if (!isRunning) {
			throw new IllegalStateException("already stopped");
		}
		if (!svc.isRunning()) {
			throw new IllegalStateException("svc is not running");
		}
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
Injector injector = Injector.getContext("myapp");
injector.defineConstruction(MyApp.class, MyAppImpl::new)
	.defineConstruction(MyService.class, MyServiceImpl::new)
	.defineConstruction(HelperProcessStarter.class, HelperProcessStarter::new)
	.makeBeans(MyApp.class)
	.start();

assertTrue(injector.getBean(HelperProcessStarter.class)
	.isRunning());
assertTrue(injector.getBean(MyApp.class)
	.isRunning());
assertTrue(injector.getBean(MyService.class)
	.isRunning());

injector.print();

injector.stop();

assertFalse(injector.getBean(HelperProcessStarter.class)
	.isRunning());
assertFalse(injector.getBean(MyApp.class)
	.isRunning());
assertFalse(injector.getBean(MyService.class)
	.isRunning());
```
## Dagger 2 CoffeeShop Example

See [`coded.dependency.injection.example.dagger2.CoffeeApp`](./src/test/java/coded/dependency/injection/example/dagger2/CoffeeApp.java), ported from
[Dagger CoffeeMaker example at github](https://github.com/google/dagger/tree/master/examples/maven/coffee/src/main/java/example/dagger).
See also [Dagger 2](https://dagger.dev/dev-guide/).

Dependency tree printed by `Injector#print`:

```
CoffeeShop (coded.dependency.injection.example.dagger2.CoffeeApp$CoffeeShop)
  -> CoffeeMaker (coded.dependency.injection.example.dagger2.CoffeeMaker)
    -> CoffeeLogger (coded.dependency.injection.example.dagger2.CoffeeLogger)
    -> ElectricHeater (coded.dependency.injection.example.dagger2.ElectricHeater)
      -> CoffeeLogger (coded.dependency.injection.example.dagger2.CoffeeLogger)
    -> Thermosiphon (coded.dependency.injection.example.dagger2.Thermosiphon)
      -> CoffeeLogger (coded.dependency.injection.example.dagger2.CoffeeLogger)
      -> ElectricHeater (coded.dependency.injection.example.dagger2.ElectricHeater)
  -> CoffeeLogger (coded.dependency.injection.example.dagger2.CoffeeLogger)
```

## springframework.guru Example

See [`coded.dependency.injection.example.springfw.guru.DiExampleApplication`](./src/test/java/coded/dependency/injection/example/springfw/guru/DiExampleApplication.java), ported from 
[springframework.guru](https://springframework.guru/dependency-injection-example-using-spring/).


## vogella.com Example

See [`coded.dependency.injection.example.vogella.MainVogellaDiExample`](./src/test/java/coded/dependency/injection/example/vogella/MainVogellaDiExample.java), ported from
[vogella.com](https://www.vogella.com/tutorials/SpringDependencyInjection/article.html).
