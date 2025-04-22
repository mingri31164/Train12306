package com.mingri.train12306.framework.starter.bases.init;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 监听ApplicationReadyEvent事件，如果是第一次发生ApplicationReadyEvent，
 * 发布我们自定义的事件，表示Spring容器初始化完成
 */
@RequiredArgsConstructor
public class ApplicationContentPostProcessor implements ApplicationListener<ApplicationReadyEvent> {

    private final ApplicationContext applicationContext;

    /**
     * 执行标识，确保Spring事件 {@link ApplicationReadyEvent} 有且执行一次
     */
    private final AtomicBoolean executeOnlyOnce = new AtomicBoolean(false);


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        if (!executeOnlyOnce.compareAndSet(false, true)) {
            /**
             * 避免容器刷新造成二次调用初始化逻辑
             **/
            return;
        }

        applicationContext.publishEvent(new ApplicationInitializingEvent(this));
    }
}
