package com.mingri.train12306.framework.starter.idempotent.core.spel;

import com.mingri.train12306.framework.starter.cache.DistributedCache;
import com.mingri.train12306.framework.starter.idempotent.annotation.Idempotent;
import com.mingri.train12306.framework.starter.idempotent.core.*;
import com.mingri.train12306.framework.starter.idempotent.enums.IdempotentMQConsumeStatusEnum;
import com.mingri.train12306.framework.starter.idempotent.toolkit.LogUtil;
import com.mingri.train12306.framework.starter.idempotent.toolkit.SpELUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 基于 SpEL 方法验证请求幂等性，适用于 MQ 场景
 */
@RequiredArgsConstructor
public final class IdempotentSpELByMQExecuteHandler extends AbstractIdempotentExecuteHandler implements IdempotentSpELService {

    private final static int TIMEOUT = 600;

    private final static String WRAPPER = "wrapper:spEL:MQ";

    private final static String LUA_SCRIPT_SET_IF_ABSENT_AND_GET_PATH = "lua/set_if_absent_and_get.lua";

    private final DistributedCache distributedCache;

    @SneakyThrows
    @Override
    protected IdempotentParamWrapper buildWrapper(ProceedingJoinPoint joinPoint) {
        Idempotent idempotent = IdempotentAspect.getIdempotent(joinPoint);
        // 通过执行 SpEL 表达式获取值
        String key = (String) SpELUtil.parseKey(idempotent.key(),
                ((MethodSignature) joinPoint.getSignature()).getMethod(), joinPoint.getArgs());
        return IdempotentParamWrapper.builder().lockKey(key).joinPoint(joinPoint).build();
    }

    @Override
    public void handler(IdempotentParamWrapper wrapper) {
        // 拼接前缀和 SpEL 表达式对应的 Key 生成最终放到 Redis 中的唯一标识
        String uniqueKey = wrapper.getIdempotent().uniqueKeyPrefix() + wrapper.getLockKey();
        // 向 Redis 触发命令，如果值不存在则存储返回 True，值存在返回 False
        String absentAndGet = this.setIfAbsentAndGet(uniqueKey,
                IdempotentMQConsumeStatusEnum.CONSUMING.getCode(), TIMEOUT, TimeUnit.SECONDS);
        // 如果值为 False，那么就代表要么消息已经执行完成了或者执行中，两个不同的状态需要执行不同的逻辑
        // 为此，需要再进行判断
        if (Objects.nonNull(absentAndGet)) {
            // 如果已经执行成功了，那么 error 为 false；执行中 error 为 true
            boolean error = IdempotentMQConsumeStatusEnum.isError(absentAndGet);
            LogUtil.getLog(wrapper.getJoinPoint()).warn("[{}] MQ repeated consumption, {}.", uniqueKey, error ? "Wait for the client to delay consumption" : "Status is completed");
            // 抛出异常，交给上层判断应该重试还是将消息吞掉
            throw new RepeatConsumptionException(error);
        }
        IdempotentContext.put(WRAPPER, wrapper);
    }

    public String setIfAbsentAndGet(String key, String value, long timeout, TimeUnit timeUnit) {
        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
        ClassPathResource resource = new ClassPathResource(LUA_SCRIPT_SET_IF_ABSENT_AND_GET_PATH);
        redisScript.setScriptSource(new ResourceScriptSource(resource));
        redisScript.setResultType(String.class);

        long millis = timeUnit.toMillis(timeout);
        return ((StringRedisTemplate) distributedCache.getInstance()).execute(redisScript, List.of(key), value, String.valueOf(millis));
    }

    @Override
    public void exceptionProcessing() {
        IdempotentParamWrapper wrapper = (IdempotentParamWrapper) IdempotentContext.getKey(WRAPPER);
        if (wrapper != null) {
            Idempotent idempotent = wrapper.getIdempotent();
            String uniqueKey = idempotent.uniqueKeyPrefix() + wrapper.getLockKey();
            try {
                // 删除幂等标识
                distributedCache.delete(uniqueKey);
            } catch (Throwable ex) {
                LogUtil.getLog(wrapper.getJoinPoint()).error("[{}] Failed to del MQ anti-heavy token.", uniqueKey);
            }
        }
    }

    @Override
    public void postProcessing() {
        IdempotentParamWrapper wrapper = (IdempotentParamWrapper) IdempotentContext.getKey(WRAPPER);
        if (wrapper != null) {
            Idempotent idempotent = wrapper.getIdempotent();
            String uniqueKey = idempotent.uniqueKeyPrefix() + wrapper.getLockKey();
            try {
                distributedCache.put(uniqueKey, IdempotentMQConsumeStatusEnum.CONSUMED.getCode(), idempotent.keyTimeout(), TimeUnit.SECONDS);
            } catch (Throwable ex) {
                LogUtil.getLog(wrapper.getJoinPoint()).error("[{}] Failed to set MQ anti-heavy token.", uniqueKey);
            }
        }
    }
}
