# Coded Dependency Injection

Use plain Java 8+ code for dependency injection. No reflection, no annotation processing needed and no 3rd party dependencies.

For example A depends on B and C. `Wiring` is the dependency injector provider, beans are always referenced by their class:

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

Wiring.getContext("myapp")	// creates a named dependency injector
	.connectAll(A.class); 	// creates the beans and injects them accordingly

```

## Proxy Based Injection

A dependency injector named 'myapp' is created with `Wiring.getContext("myApp")`.
The 'magic' happens when a bean like `A` is instantiated by the injector, then also its `Dependency` objects are instantiated.
Every `Dependency` object instantiates the referenced service bean and stores it, this results in a cascading creation of the complete dependency tree with root bean `A`. 

`Dependency<B> b` acts as a proxy and returns with `b.get()` the service bean `B`.
The `Dependency` constructor requires as first argument the client bean as type of the interface `Dependent`, 
and second the service class.

`Wiring#connectAll(clz)` creates the dependency tree and starts top down instantiation of all beans beginning with `clz`.

When using the `Wiring` API every bean is referenced by its class. For example to retrieve bean `A` use 
`Wiring.getContext("myapp").get(A.class);`.

Optional bean construction `Supplier`s can be defined to avoid reflection. If not defined, default constructors are used for creating beans (`Class#newInstance()`), for example:

```Java
int val1=0, val2=0;

Wiring.getContext("myapp")
	.defineConstruction(A.class, A::new) // only needed if reflection should/cannot be used
	.defineConstruction(B.class, () -> new B(val1, val2))
	.connectAll(A.class)
	...
```

Classes are treated as singletons within the scope of a named dependency injector, 
that means for every class only one instance is created within a `Wiring` context.

## Features

It is light weight, fast, debugable and transparent.

Dependency graph is printable.

Individual construction suppliers for instance creation can be defined.

Individual consumers for starting beans can be defined.

Individual consumers for stopping beans can be defined.

Multiple independent injector instances (wiring contexts) are possible.

Cyclic dependencies are prohibited.

## Limitations

Supports only field injection.

## Examples

https://springframework.guru/dependency-injection-example-using-spring/
https://www.vogella.com/tutorials/SpringDependencyInjection/article.html

### Lifecycle of Beans

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
	
	Dependency<MyService> svc = new Dependency<>(this, MyService.class);

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
WiringInterface injector = Wiring.getContext("myapp");
injector.defineConstruction(MyApp.class, MyAppImpl::new)
	.defineConstruction(MyService.class, MyServiceImpl::new)
	.connectAll(MyApp.class)
	.start();

assertTrue(injector.get(MyApp.class)
	.isRunning());
assertTrue(injector.get(MyService.class)
	.isRunning());

injector.stop();

assertFalse(injector.get(MyApp.class)
	.isRunning());
assertFalse(injector.get(MyService.class)
	.isRunning());
```

https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet