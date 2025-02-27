package com.mingri.train12306.framework.starter.designpattern.chain;

import org.springframework.core.Ordered;

/**
 * 抽象业务责任链组件
 * Ordered 接口通常用于定义组件的顺序或优先级。
 * 实现 Ordered 接口的类需要实现 getOrder() 方法，返回一个整数值，表示该组件的优先级。数值越小优先级越高
 */
public interface AbstractChainHandler<T> extends Ordered {

    /**
     * 执行责任链逻辑
     *
     * @param requestParam 责任链执行入参
     */
    void handler(T requestParam);

    /**
     * @return 责任链组件标识
     */
    String mark();
}
