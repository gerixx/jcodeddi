# Coded Dependency Injection

Use plain Java code for dependency injection. No reflection and no annotation processing needed.

For example A depends on B and C:

```
A -> B, C
```

```Java
Wiring.getContext("myapp")
	.connect(A.class, B.class)
	.connect(A.class, C.class)
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

Explicit wiring is done with `Wiring#connect(classFrom, classTo)`, instance creation and wiring is asynchronous and completed with terminal function `Wiring#await()`. 

Implicit wiring is performed with `Wiring#connect(classFrom)`,  
all depending instances are created recursively. 

Reflection is per default used for instance creation (`Class#newInstance()`).  But, construction code can be defined to avoid reflection completely:

```Java
Wiring.getContext("myapp")
        .defineConstruction(A.class, A::new)
        .defineConstruction(B.class, () -> new B(arg1, arg2))
        .connect(A.class, B.class)
        ...
```

For every class only one instance is created within a wiring context.

## Features

It is light weight, fast, debugable and transparent.

Asynchronous instance creation and wiring.

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
    .define(MyAppImpl.class, app -> app.stop()) // <----- DESTRUCTION lambda
    .connect(MyAppImpl.class, MyServiceImpl.class)
    .get(MyAppImpl.class);

myApp.start();

...

Wiring.getContext("app")
    .shutdown() // invokes DESTRUCTION lambda(s)
    .await();
```


https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet