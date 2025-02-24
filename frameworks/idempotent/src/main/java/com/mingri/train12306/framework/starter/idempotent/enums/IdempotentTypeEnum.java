package com.mingri.train12306.framework.starter.idempotent.enums;

/**
 * 幂等验证类型枚举
 * ● 分布式锁：PARAM 和 SPEL。
 * ● Token 令牌：TOKEN。
 * ● 去重表：SPEL。
 * TOKEN 和 PARAM 以及 SPEL 都可以应用于接口防重复提交，SPEL 应用于消息队列防重复消费
 */
public enum IdempotentTypeEnum {

    /**
     * 基于 Token 方式验证
     */
    TOKEN,

    /**
     * 基于方法参数方式验证
     */
    PARAM,

    /**
     * 基于 SpEL 表达式方式验证
     */
    SPEL
}
