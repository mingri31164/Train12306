package com.mingri.train12306.biz.userservice.service.filter.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mingri.train12306.biz.userservice.common.enums.UserRegisterErrorCodeEnum;
import com.mingri.train12306.biz.userservice.dao.entity.UserDO;
import com.mingri.train12306.biz.userservice.dao.mapper.UserMapper;
import com.mingri.train12306.biz.userservice.dto.req.UserRegisterReqDTO;
import com.mingri.train12306.biz.userservice.service.filter.UserRegisterCreateChainFilter;
import com.mingri.train12306.biz.userservice.service.impl.UserServiceImpl;
import com.mingri.train12306.framework.starter.cache.DistributedCache;
import com.mingri.train12306.framework.starter.convention.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import static com.mingri.train12306.biz.userservice.common.constant.RedisKeyConstant.USER_REGISTER_REUSE_SHARDING;
import static com.mingri.train12306.biz.userservice.toolkit.UserReuseUtil.hashShardingIdx;

/**
 * 用户注册用户名唯一检验
 */
@Component
@RequiredArgsConstructor
public final class UserRegisterHasUsernameChainHandler implements UserRegisterCreateChainFilter<UserRegisterReqDTO> {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final DistributedCache distributedCache;
    @Autowired
    private UserMapper userMapper;

    @Override
    public void handler(UserRegisterReqDTO requestParam) {
        String username = requestParam.getUsername();
        if (userRegisterCachePenetrationBloomFilter.contains(username)) {
            // 布隆过滤器存在该用户名
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();

            // 判断数据库中是否存在该用户名
            QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);

            // 判断数据库中是否存在该用户名
            if (userMapper.exists(queryWrapper)) {
                // 如果 Redis 中不存在该用户名，则抛出异常
                if (Boolean.FALSE.equals(instance.opsForSet().isMember(USER_REGISTER_REUSE_SHARDING + hashShardingIdx(username), username)))
                    throw new ClientException(UserRegisterErrorCodeEnum.HAS_USERNAME_NOTNULL);
            }
        }
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
