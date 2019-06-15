package org.tree.demo.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * @author er_dong_chen
 * @date 2019/6/15
 * @describe
 */
public class Main {
    public static void main(String[] args) throws InterruptedException{
        Thread thread = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(10);
                System.out.println("Hello World!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("线程被中断");
            }
        });
        thread.start();

        TimeUnit.SECONDS.sleep(2);
        thread.interrupt();
    }
}
