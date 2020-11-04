# Coded Dependency Injection

Can be used if reflection and annotation processing is not allowed or possible, the memory footprint is also very very low.
Minimum Java version is 8, recommended is version 11+.

For example A depends on B and C. `Injector` is the dependency injector provider, beans are always referenced by their classes:

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

A dependency injector named 'myapp' is created with `Injector.getContext("myapp")`.
The 'magic' happens when a bean like `A` is instantiated by the injector, then also its `Dependency` objects are instantiated.
Every `Dependency` object instantiates the referenced service bean and stores it, this results in a cascading creation of the complete dependency tree with root bean `A` when executing `...makeBeans(A.class)`. 

`Dependency<B> b` acts as a proxy and returns with `b.get()` the service bean `B`.
The `Dependency` constructor requires as first argument the client bean as type of the interface `Dependent`, 
and second the service class.

`Injector#makeBeans(clz)` creates the dependency tree and starts top down instantiation of all beans beginning with `clz`.

When using the `Injector` API every bean can be addressed by its class. For example to retrieve bean `A` use 
`Injector.getContext("myapp").getBean(A.class);`.

## IoC

Lambdas can be used for construction and the lifecycle of beans.

An optional bean construction `Supplier` can be defined for its class or interface (see also JUnit tests with interfaces).
With that no reflection is needed to create objects. 
If not defined, default constructors are used for creating beans (`Class#newInstance()`).

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

Optionally the basic lifecycle of beans can be controlled by `Injector#start()` and `Injector#stop()`.
With that the injector invokes the start/stop consumer of a bean if it was defined by `Injector#defineStart()`
`Injector#defineStop()`. 
Alternatively the interface `Lifecycle` can be implemented, see also "Lifecycle" example below.

## Features

It is light weight, fast, debugable and transparent.

Dependency graph is printable.

Individual construction suppliers for instance creation can be defined.

Individual consumers for starting beans can be defined.

Individual consumers for stopping beans can be defined.

Multiple independent injector instances (Injector contexts) are possible.

Cyclic dependencies are prohibited.

No casts needed when using beans.

## Limitations

Supports only field injection.

# Examples

## Lifecycle of Beans

MyApp > MyService


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

See `coded.dependency.ijection.example.springfw.guru.DiExampleApplication`, ported from 
[springframework.guru](https://springframework.guru/dependency-injection-example-using-spring/).


## vogella.com Example

See `coded.dependency.ijection.example.vogella.Main`, ported from
[vogella.com](https://www.vogella.com/tutorials/SpringDependencyInjection/article.html)