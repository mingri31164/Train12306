package com.mingri.train12306.framework.starter.common.threadpool.support.eager;

import com.mingri.train12306.framework.starter.common.toolkit.ThreadUtil;

import java.util.concurrent.*;

public class EagerThreadPoolExecutorTest {

    public static void main(String[] args) {
        ThreadPoolExecutor.AbortPolicy abortPolicy = new ThreadPoolExecutor.AbortPolicy();
        TaskQueue taskQueue = new TaskQueue<>(1);
        EagerThreadPoolExecutor eagerThreadPoolExecutor =
                new EagerThreadPoolExecutor(1, 3,
                        1024, TimeUnit.SECONDS, taskQueue, abortPolicy);
        taskQueue.setExecutor(eagerThreadPoolExecutor);
        for (int i = 0; i < 5; i++) {
            try {
                eagerThreadPoolExecutor.execute(() -> ThreadUtil.sleep(100000L));
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        System.out.println("================ 线程池拒绝策略执行次数: " + eagerThreadPoolExecutor.getRejectCount());
    }
}