## Executor Framework

### Executor

Executor 是比 Thread 更高级的线程抽象, 在 [Thread2Executor](src/main/java/org/tree/demo/concurrent/executor/Thread2Executor.java) 中 ( 使用了 Web Server 的例子 ) 讲解了使用 Executor 取代 Thread 的好处. 

Executors 是 Executor 的工具类, 提供了多种常用的实现 ( 详情见源码 ) : 

* newFixedThreadPool 返回一个 ThreadPool, 线程池中线程数量固定
* newCachedThreadPool 返回一个 ThreadPool, 线程池中线程数量没有限制
* newScheduledThreadPool 返回 ThreadPool 的一个子类对象, 可以延迟与定时 (类似的框架: timer || quartz, 与 timer 的对比可见 [Timer2ScheduledExecutorService](src/main/java/org/tree/demo/concurrent/executor/task/Timer2ScheduledExecutorService.java))
* newSingleThreadExecutor 当一个线程挂了之后会重新创建一个新的线程, 但会始终保证任务的串行执行

### ExecutorService

程序运行的 JVM 会在所有线程都结束的时候才关闭, 如果使用单纯的 Executor, 是没有办法关闭 JVM 的. 为此 Executor 扩展了 ExecutorService 接口, 用于 Executor 的生命周期管理, 两者的差别可见 [Executor2ExecutorService](src/main/java/org/tree/demo/concurrent/executor/Executor2ExecutorService.java) ( 带有生命周期的 Web Server ) . 

ExecutorService 的生命周期有三种: 运行, 关闭, 已终止 ( 详情见源码 )
* 关闭时: shutdown 会等待已接收的所有线程运行结束并停止接收新任务; shutdownNow 则会粗暴地停止所有任务 ( 正在运行或等待的线程 )
* 关闭后: 提交的任务会转到 RejectedExecutionHandler 处理 ( 一般是抛出一个运行时异常 RejectedExecutionException, 如 ThreadPoolExecutor )
* 使用 isShutdown 查看是否关闭, isTerminated 查看是否终止

### Callable & Future

执行多线程任务的时候一般是实现 Runnable 接口, 但是 Runnable 只能执行, 没法返回结果. 从 Java 5 开始提供了 Callable 和 Future 接口, 前者用来取代 Runnable, 后者以 Runnable 或 Callable 为基础, 用于任务的生命周期

Callable 只有一个方法 call 方法, ExecutorService 通过 submit 一个 Callable 实例或 invokeAll 一个 Callable 集合来创建一个 Future 实例或集合, Future 再通过 get 同步获取返回值 ( 值还没有返回时阻塞 ), 或通过 cancel(true) 取消任务 ( ExecutorService 的 invokeAll 和 Future 的 get 可以定时, 超时未完成将取消任务 )

Callable 与 Future 的应用方式可见 [Callable_And_Future](src/main/java/org/tree/demo/concurrent/executor/task/Callable_And_Future.java)

### ExecutorCompletionService

ExecutorCompletionService = ExecutorService + BlockingQueue

ExecutorCompletionService 使用 submit 提交一个任务, 任务执行完成后会添加进 ExecutorCompletionService 内部的 BlockingQueue 中, 然后每次使用 take 都能从 BlockingQueue 中获取已完成的 Future, 使用方式见 [Callable_And_Future :: renderByExecutorCompletionService](src/main/java/org/tree/demo/concurrent/executor/task/Callable_And_Future.java) 

### In the end

* Thread < Executor < ExecutorService ( 使用 Executor 和 ExecutorService 时不一样的只是类名而已, 可以无损替代 )

```
Executor executor = Executors.newFixedThreadPool(100);
ExecutorService executor = Executors.newFixedThreadPool(100);
```

* Runnable < Callable & Future

```
ExecutorService executor = Executors.newFixedThreadPool(100);
Future<Integer> future = executor.submit(() -> 100);
Integer result = future.get();
```