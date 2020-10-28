# Coded Dependency Injection

Use plain Java 8+ code for dependency injection. No reflection, no annotation processing needed and no 3rd party dependencies.
Recommended is Java 11+ as lambda performance was fixed.

For example A depends on B and C:

```
A -> B, C
```

```Java
Wiring.getContext("myapp")
	.connectAll(A.class);

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
	b.get().foo(); // b is a proxy to the service object
}

```

An injector named 'myApp' is created with `Wiring.getContext("myapp")`. Multiple injectors can be created.

Proxy based injection: `Dependency<B> b` acts as a proxy and returns with `b.get()` the service bean `B`.
The `Dependency` constructor needs the service bean as type of `Dependent` and the client class.

`Wiring#connectAll(clz)` starts top down instantiation of all beans beginning with `clz` and its dependencies.
The initialization of the `Dependency` members, therefore the injection, happens during bottom up traversal.

Bean construction `Supplier`s can be defined to avoid reflection. If not defined default constructors are used for creating beans (`Class#newInstance()`):

```Java
int val1=0, val2=0;

Wiring.getContext("myapp")
	.defineConstruction(A.class, A::new) // only needed if reflection should/can not be used
	.defineConstruction(B.class, () -> new B(val1, val2))
	.connectAll(A.class)
	...
```

Classes are singletons, that means for every class only one instance is created within a wiring context.

## Features

It is light weight, fast, debugable and transparent.

Dependency graph is printable.

Individual construction suppliers for instance creation can be defined.

Individual start up consumers for starting the system can be defined.

Individual destruction consumers for instance shutdown and resource releases can be defined.

Multiple independent injector instances (wiring contexts) are possible.

Scalable - Injectors can be created or cloned during runtime.

Cyclic dependencies are prohibited.

## Examples

### Construction and Destruction

```Java
interface MyApp extends Dependent {
	public String start();
	public void stop();
}

interface MyService {
	public void initialize();
	public void destroy();
	public String greets();
}

class MyAppImpl implements MyApp {
	private Dependency<MyService> svc = new Dependency<>(this, MyService.class);

	@Override
	public String start() {
		return svc.get().greets();
	}

	@Override
	public void stop() {
		svc.get().destroy();
	}
}

MyApp myApp = Wiring.getContext("app")
	.defineConstruction(MyService.class, MyServiceImpl::new)
	.defineStartStop(MyAppImpl.class, app -> greets = app.start(), app -> app.stop())
	.defineStartStop(MyServiceImpl.class, svc -> svc.initialize(), svc -> svc.destroy())
	.connectAll(MyAppImpl.class)
	.start()
   .get(MyAppImpl.class);

myApp.start();

...

Wiring.getContext("app")
    .shutdown() // invokes DESTRUCTION lambda(s)
    .await();
```

For used interfaces in `Dependency` declarations a construction supplier has to be defined, for interface `MyService` this could be `.defineConstruction(MyService.class, MyServiceImpl::new)`.

https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet