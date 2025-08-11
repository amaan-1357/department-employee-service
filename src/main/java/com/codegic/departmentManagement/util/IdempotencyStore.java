package com.codegic.departmentManagement.util;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyStore {

    private final ConcurrentHashMap<Long, Instant> lastAdjustmentMap = new ConcurrentHashMap<>();

    public boolean canAdjust(Long departmentId) {
        Instant now = Instant.now();
        Instant last = lastAdjustmentMap.get(departmentId);
        if (last == null || now.isAfter(last.plusSeconds(1800))) {
            lastAdjustmentMap.put(departmentId, now);
            return true;
        }
        return false;
    }
}