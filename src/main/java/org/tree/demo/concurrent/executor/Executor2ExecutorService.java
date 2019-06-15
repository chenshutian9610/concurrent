package org.tree.demo.concurrent.executor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * @author er_dong_chen
 * @date 2019/6/15
 * @describe 带有生命周期的 Web Server
 */
public class Executor2ExecutorService {

    private static final ExecutorService executor = Executors.newFixedThreadPool(100);

    /* 与 Thread2Executor 的 executeByExecutor 基本一样, 但是会拦截一个 RejectedExecutionException */
    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(80);
        while (!executor.isShutdown()) {
            try {
                final Socket socket = serverSocket.accept();
                executor.execute(() -> handle(socket));
            } catch (RejectedExecutionException e) { // 拦截 RejectedExecutionException 这一步骤是可选的
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        executor.shutdown();
    }

    private static void handle(Socket socket) {
    }
}
