package com.multibank.notification_routing.lock;

public interface LockManager {

    boolean lock(String lockName, Long ttLockInMillis);
    boolean unlock(String lockName);
}
