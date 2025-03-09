package com.mingri.train12306.framework.starter.designpattern.chain;

import com.mingri.train12306.framework.starter.bases.ApplicationContextHolder;
import com.mingri.train12306.framework.starter.bases.init.ApplicationInitializingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 抽象责任链上下文
 */
public final class AbstractChainContext<T> implements ApplicationListener<ApplicationInitializingEvent> {

    private final Map<String, List<AbstractChainHandler>> abstractChainHandlerContainer = new HashMap<>();

    /**
     * 责任链组件执行
     *
     * @param mark         责任链组件标识
     * @param requestParam 请求参数
     */
    public void handler(String mark, T requestParam) {
        List<AbstractChainHandler> abstractChainHandlers = abstractChainHandlerContainer.get(mark);
        if (CollectionUtils.isEmpty(abstractChainHandlers)) {
            throw new RuntimeException(String.format("[%s] Chain of Responsibility ID is undefined.", mark));
        }
        abstractChainHandlers.forEach(each -> each.handler(requestParam));


    }

    @Override
    public void onApplicationEvent(ApplicationInitializingEvent event) {
        Map<String, AbstractChainHandler> chainFilterMap = ApplicationContextHolder
                .getBeansOfType(AbstractChainHandler.class);
        Map<String, List<AbstractChainHandler>> groupedHandlers = new HashMap<>();
        chainFilterMap.forEach((beanName, bean) -> {
            String mark = bean.mark();
            groupedHandlers.computeIfAbsent(mark, k -> new ArrayList<>()).add(bean);
        });

        groupedHandlers.forEach((mark, handlers) -> {
            List<AbstractChainHandler> sortedHandlers = handlers.stream()
                    .sorted(Comparator.comparingInt(Ordered::getOrder))
                    .collect(Collectors.toList());
            abstractChainHandlerContainer.put(mark, sortedHandlers);
        });
    }


}
