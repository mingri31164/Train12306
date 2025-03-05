package com.mingri.train12306.biz.aggregationservice.config;

import cn.hippo4j.common.web.base.Result;
import cn.hippo4j.common.web.base.Results;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.PrintWriter;

/**
 * 部署公网环境演示模式配置
 */
@Configuration
@RequiredArgsConstructor
public class DemoModeConfiguration implements WebMvcConfigurer {

    private final DemoModeProperties demoModeProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DemoModeInterceptor())
                .addPathPatterns("/**");
    }

    public class DemoModeInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object handler) throws Exception {
            if (demoModeProperties.getEnable() && demoModeProperties.getBlacklist().contains(request.getRequestURI())) {
                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                Result<Void> result = Results.failure("B000001", "演示环境部分功能受限，禁止操作用户和乘车人等通用数据");
                out.print(JSON.toJSONString(result)); // 返回指定的 JSON 字符串
                out.flush();
                return false;
            }
            return true;
        }
    }
}
