package org.tree.demo.concurrent.executor.task;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author er_dong_chen
 * @date 2019/6/15
 * @describe Timer 定时任务在 Java 5 之后就很少用了, 取而代之是 ScheduledThreadPoolExecutor
 */
public class Timer2ScheduledExecutorService {

    public static void main(String[] args) throws Exception {
        schedule();
    }

    /**
     * Timer 实例可以使用 schedule 执行多个任务, 但是: ( IllegalStateException: Task already scheduled or cancelled 简称 e )
     * <p>
     * 1.一个 TimerTask 实例算一个任务, 只能被执行一次, 重复执行会抛错 e
     * 2.如果前面有一个任务抛出异常, 会导致后续执行的任务不能执行, 抛错 e
     * 3.如果执行完所有任务后没有显式地使用 cancel 方法, 会导致线程一直存活, 从而无法停止程序
     * 4.如果 cancel 方法使用太快, 会导致后续执行的任务不能执行, 抛错 e
     */
    public static void timer() throws InterruptedException {
        Timer timer = new Timer();

        // 执行任务 ( 1 秒后抛出一个异常 )
        timer.schedule(newTask(() -> {
            throw new RuntimeException("下一个任务无法执行 ( 不会显示 Hello China! )");
        }), 1000);

        // 等待 1 秒后 ( 等待前面的任务执行 ), 执行任务 ( 3 秒后显示 "Hello China!" )
        TimeUnit.SECONDS.sleep(1);
        timer.schedule(newTask(() -> System.out.println("Hello China!")), 3000);

        // 等待 3 秒后 ( 不等待 3 秒的话会将正在等待的第二个任务取消掉 ), 关闭 timer 实例
        TimeUnit.SECONDS.sleep(3);
        timer.cancel();
    }

    /* ScheduledExecutorService 的时间误差比 Timer 小, 并且当有一个任务抛错时不会影响其他任务 */
    public static void schedule() throws InterruptedException {
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(2);
        scheduler.schedule(() -> {
            throw new RuntimeException("下一个任务可以执行");
        }, 1, TimeUnit.SECONDS);
        TimeUnit.SECONDS.sleep(1);
        scheduler.schedule(() -> System.out.println("Hello China!"), 3, TimeUnit.SECONDS);

        // 关闭执行器
        scheduler.shutdown();
    }

    private static TimerTask newTask(Runnable runnable) {
        return new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        };
    }
}
