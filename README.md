# Coded Dependency Injection

Use plain Java 8+ code for dependency injection. No reflection, no annotation processing needed and no 3rd party dependencies.

For example A depends on B and C:

```
A -> B, C
```

```Java
Wiring.getContext("myapp")
	.connectAll(A.class)
	.await();

class B {
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

A new injector is created with `Wiring.getContext("myapp")`

Proxy based injection: `Dependency<B> b` acts as a proxy and returns with `get()` the service object `B`.

Recursive wiring is done with `Wiring#connectAll(clz)` stargint with creation of `clz`. Instance creation and wiring is asynchronous and completed with terminal function `Wiring#await()`. 

Reflection is per default used for instance creation (`Class#newInstance()`).  But, construction code can be defined to avoid reflection completely:

```Java
Wiring.getContext("myapp")
	.defineConstruction(A.class, A::new)
	.defineConstruction(B.class, () -> new B(arg1, arg2))
	.connectAll(A.class)
	...
```

Classes are singletons, that means for every class only one instance is created within a wiring context.

## Features

It is light weight, fast, debugable and transparent.

Asynchronous instance creation and wiring. (TODO)

Dependency graph is printable.

Individual construction suppliers for instance creation can be defined.

Individual start up consumers for starting the system can be defined.

Individual destruction consumers for instance shutdown and resource releases can be defined.

Multiple independent injector instances (wiring contexts) are possible.

Scalable - Injectors can be created or cloned during runtime.

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
	defineStartStop(MyAppImpl.class, app -> greets = app.start(), app -> app.stop())
	.defineStartStop(MyServiceImpl.class, svc -> svc.initialize(), svc -> svc.destroy())
	.connectAll(MyAppImpl.class)
	.start()
	.await()
   .get(MyAppImpl.class);

myApp.start();

...

Wiring.getContext("app")
    .shutdown() // invokes DESTRUCTION lambda(s)
    .await();
```

For used interfaces in `Dependency` declarations a construction supplier has to be defined, for interface `MyService` this could be `.defineConstruction(MyService.class, MyServiceImpl::new)`.

https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet