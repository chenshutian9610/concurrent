package org.tree.demo.concurrent.executor.task;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author er_dong_chen
 * @date 2019/6/15
 * @describe 以 html 页面渲染为例子 ( 渲染文本与图片 )
 */
public class Callable_And_Future {

    /* 1.使用单线程渲染, 进行第二步 ( 网络 IO ) 时, CPU 是空闲的 */
    public void singleThreadRender(CharSequence content) {
        // 1.渲染文本
        HtmlUtils.renderText(content);

        // 2.下载图片
        List<URL> imageUrls = HtmlUtils.getImageUrls(content);
        List<Image> imageList = new ArrayList<>();
        imageUrls.forEach(url -> imageList.add(HtmlUtils.downloadImg(url)));

        // 3.渲染图片
        imageList.forEach((image -> HtmlUtils.renderImg(image)));
    }

    /* 2.使用 Callable 和 Future 来执行异步任务 */
    public void renderByFuture(CharSequence content) {
        // 1.创建图片下载任务并运行
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<Image>> future = executor.submit(() -> {
            List<URL> imageUrls = HtmlUtils.getImageUrls(content);
            List<Image> imageList = new ArrayList<>();
            imageUrls.forEach(url -> imageList.add(HtmlUtils.downloadImg(url)));
            return imageList;
        });

        try {
            // 2.渲染文本
            HtmlUtils.renderText(content);
            // 3.从下载任务中取出图片实例
            List<Image> imageList = future.get();
            // 4.渲染图片
            imageList.forEach(image -> HtmlUtils.renderImg(image));
        } catch (InterruptedException e) {
            // ?.重新刷新线程的中断状态
            Thread.currentThread().interrupt();
            // 取消任务
            future.cancel(true);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /* 3.1.使用 CompletionService 为每一个下载任务单独使用一个线程, 下载好一个就渲染一张图片 */
    public void renderByExecutorCompletionService(CharSequence content) {
        // 1.为每一个下载任务使用一个线程
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        ExecutorCompletionService<Image> executor = new ExecutorCompletionService<>(executorService);
        List<URL> imageUrls = HtmlUtils.getImageUrls(content);
        imageUrls.forEach((url) -> executor.submit(() -> HtmlUtils.downloadImg(url)));

        // 2.渲染文本
        HtmlUtils.renderText(content);

        // 3.每下载完一张图片后马上渲染图片
        for (int i = 0; i < imageUrls.size(); i++) {
            try {
                Future<Image> future = executor.take();
                Image image = future.get();
                HtmlUtils.renderImg(image);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /* 3.2.如果每个请求超过 3 秒将停止, 并使用默认图片渲染 */
    public void renderOptional(CharSequence content) throws InterruptedException {
        // 1.为每一个图片下载设置一个定时任务( 3 秒 ), 并执行
        ExecutorService executor = Executors.newFixedThreadPool(100);
        List<Callable<Image>> callables = new ArrayList<>();
        List<URL> imageUrls = HtmlUtils.getImageUrls(content);
        imageUrls.forEach(url -> callables.add(() -> HtmlUtils.downloadImg(url)));
        List<Future<Image>> futures = executor.invokeAll(callables, 3, TimeUnit.SECONDS);

        // 2.渲染文本
        HtmlUtils.renderText(content);

        // 3.使用下载或默认的图片渲染
        for (Future<Image> future : futures) {
            try {
                Image image = future.get();
                HtmlUtils.renderImg(image);
            } catch (CancellationException e) { // 超时的时候 Future 实例会执行 cancel(true) 方法
                HtmlUtils.renderImg(Image.DEFAULT_IMG);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }


    }

    // you don't need to know.

    private static class Image {
        private static final Image DEFAULT_IMG = new Image();
    }

    private static class HtmlUtils {

        static void renderText(CharSequence content) {
        }

        static void renderImg(Image image) {
        }

        static List<URL> getImageUrls(CharSequence content) {
            return Collections.emptyList();
        }

        static Image downloadImg(URL url) {
            return null;
        }
    }
}
