package com.mingri.train12306.biz.aggregationservice;

import cn.crane4j.spring.boot.annotation.EnableCrane4j;
import cn.hippo4j.core.enable.EnableDynamicThreadPool;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

/**
 * 12306 聚合服务应用启动器
 */
@EnableDynamicThreadPool
@SpringBootApplication(scanBasePackages = {
        "com.mingri.train12306.biz.userservice",
        "com.mingri.train12306.biz.ticketservice",
        "com.mingri.train12306.biz.orderservice",
        "com.mingri.train12306.biz.payservice",
        "com.mingri.train12306.biz.aggregationservice"
})
@EnableRetry
@MapperScan(value = {
        "com.mingri.train12306.biz.userservice.dao.mapper",
        "com.mingri.train12306.biz.ticketservice.dao.mapper",
        "com.mingri.train12306.biz.orderservice.dao.mapper",
        "com.mingri.train12306.biz.payservice.dao.mapper"
})
@EnableFeignClients(value = {
        "com.mingri.train12306.biz.ticketservice.remote",
        "com.mingri.train12306.biz.orderservice.remote"
})
@EnableCrane4j(enumPackages = "com.mingri.train12306.biz.orderservice.common.enums")
public class AggregationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggregationServiceApplication.class, args);
    }
}
