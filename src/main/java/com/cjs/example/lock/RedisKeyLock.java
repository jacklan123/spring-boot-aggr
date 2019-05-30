package com.cjs.example.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RedisKeyLock {
    private static Logger logger = LoggerFactory.getLogger(RedisKeyLock.class);
    private final static long ACQUIRE_LOCK_TIMEOUT_IN_MS = 10 * 1000;
    /**
     * 锁失效时间
     */
    private final static int EXPIRE_IN_SECOND = 5;
    private final static long WAIT_INTERVAL_IN_MS = 100;


    private final static String LUA_SCRIPT = "if (redis.call('exists',KEYS[1] == 0) then \n" +
            "\tredis.call('hset', KEYS[1], ARGV[2], 1);\n" +
            "\tredis.call('pexpire',KEYS[1],ARGV[1]);\n" +
            "\treturn nil;\n" +
            "end;\n" +
            "if(redis.call('hexists',KEYS[1],ARGV[2]) == 1) then\n" +
            "\tredis.call('hincrby',KEYS[1], ARGV[2], 1);\n" +
            "\tredis.call('pexpire', KEYS[1], ARGV[1]);\n" +
            "\treturn nil;\n" +
            "end;\n" +
            "return redis.call('pttl', KEYS[1]);";


    @Autowired
    RedisTemplate<String, String> stringRedisTemplate;

    public void lock(final String redisKey) {
        try {
            long now = System.currentTimeMillis();
            long timeoutAt = now + ACQUIRE_LOCK_TIMEOUT_IN_MS;
            boolean flag = false;
            while (true) {
                String expireAt = String.valueOf(now + EXPIRE_IN_SECOND * 1000);
                boolean ret = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, expireAt);

                //已获取锁
                if (ret) {
                    stringRedisTemplate.expire(redisKey, now + EXPIRE_IN_SECOND * 1000, TimeUnit.MILLISECONDS);
                    flag = true;
                    break;
                } else {//未获取锁，重试获取锁
                    Long oldExpireAt = stringRedisTemplate.getExpire(redisKey);
                    if (oldExpireAt != null && oldExpireAt < now) {
                        oldExpireAt = stringRedisTemplate.getExpire(redisKey);
                        if (oldExpireAt < now) {
                            flag = true;
                            break;
                        }
                    }
                }
                if (timeoutAt < now) {
                    break;
                }
                TimeUnit.NANOSECONDS.sleep(WAIT_INTERVAL_IN_MS);
            }
            if (!flag) {
                throw new RuntimeException("canot acquire lock now ...");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("lock", e);
        }
    }

    public boolean unlock(final String redisKey) {
        try {
            stringRedisTemplate.delete(redisKey);
            return true;
        } catch (Exception e) {
            logger.error("lock", e);
            return false;
        }
    }


    /**
     * @param acquireTimeout * 在获取锁之前的超时时间
     * @param timeOut * 在获取锁之后的超时时间
     */
    public String getRedisLock(Jedis jedis, String lockKey, Long acquireTimeout, Long timeOut) {
        try { // 定义 redis 对应key 的value值(uuid) 作用 释放锁 随机生成value,根据项目情况修改
            String identifierValue = UUID.randomUUID().toString();
            // 定义在获取锁之前的超时时间
            // 使用循环机制 如果没有获取到锁，要在规定acquireTimeout时间 保证重复进行尝试获取锁
            // 使用循环方式重试的获取锁
            Long endTime = System.currentTimeMillis() + acquireTimeout;
            while (System.currentTimeMillis() < endTime) {
                // 获取锁 // set使用NX参数的方式就等同于 setnx()方法，成功返回OK.PX以毫秒为单位
                if ("OK".equals(jedis.set(lockKey, identifierValue, "NX", "PX", timeOut))) {
                    return identifierValue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void unRedisLock(Jedis jedis, String lockKey, String identifierValue) {
        try {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long result = (Long) jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(identifierValue));
            //0释放锁失败。1释放成功
            if (1 == result) {
                //如果你想返回删除成功还是失败，可以在这里返回
                System.out.println(result + "释放锁成功");
            }
            if (0 == result) {
                System.out.println(result + "释放锁失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

