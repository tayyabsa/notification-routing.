package com.multibank.notification_routing.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedissonLockManager implements LockManager {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public boolean lock(String lockName, Long ttlLockInMillis) {
        try {
            redissonClient.getLock(lockName).lock(ttlLockInMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean unlock(String lockName) {
        boolean isUnlocked = false;
        try {
            RLock lock = redissonClient.getLock(lockName);
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
                isUnlocked = true;
            }
        } catch (RedisException redisException) {
            isUnlocked = false;
        }
        return isUnlocked;
    }
}