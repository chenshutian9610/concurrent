package org.tree.demo.concurrent.executor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author er_dong_chen
 * @date 2019/6/15
 * @describe 基于 Thread 和 Executor 的 Web Server 的区别
 */
public class Thread2Executor {

    /* 1.单线程, 每次只能处理一个请求, 性能糟糕 */
    public static void executeSerially() throws IOException {
        ServerSocket serverSocket = new ServerSocket(80);
        while (true) {
            Socket socket = serverSocket.accept();
            handle(socket);
        }
    }

    /* 2.并行执行, 但无限制的创建线程会消耗大量资源, 导致系统崩溃 */
    public static void executeByThread() throws IOException {
        ServerSocket serverSocket = new ServerSocket(80);
        while (true) {
            final Socket socket = serverSocket.accept();
            new Thread(() -> handle(socket)).start();
        }
    }

    /**
     * 3.将线程数量控制在 threadCount 以内, 既能并发执行, 又不会创建过多线程
     * P.S.当请求速度大于处理速度时会导致大量的 Runnable 排队, 最终耗尽内存
     */
    public static void executeByExecutor() throws IOException {
        final int threadCount = 100;
        Executor executor = Executors.newFixedThreadPool(threadCount);
        ServerSocket serverSocket = new ServerSocket(80);
        while (true) {
            Socket socket = serverSocket.accept();
            executor.execute(() -> handle(socket));
        }
    }

    private static void handle(Socket socket) {
    }
}
