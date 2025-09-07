package com.multibank.notification_routing.utils;

public interface Constants {
    String ERROR_CODE_NOT_FOUND = "404";
    String ERROR_CODE_BAD_REQUEST= "400";

    Long DEFAULT_TTL_IN_MILLIS = 3000L;
    String LOCK_KEY_PREFIX = "notification-service-lock::";
}
