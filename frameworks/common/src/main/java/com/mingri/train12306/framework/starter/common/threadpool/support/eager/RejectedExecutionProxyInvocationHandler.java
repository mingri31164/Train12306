package com.mingri.train12306.framework.starter.common.threadpool.support.eager;

import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.RejectedExecutionHandler;

/**
 * 拒绝策略代理
 */
@AllArgsConstructor
public class RejectedExecutionProxyInvocationHandler implements InvocationHandler {

    private RejectedExecutionHandler target;

    private EagerThreadPoolExecutor executor;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 执行拒绝策略前自增拒绝次数 & 发起报警
        executor.incrementRejectCount();
        System.out.println("线程池触发了任务拒绝...");
        return method.invoke(target, args);
    }
}
