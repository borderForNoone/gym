package com.gym.crm.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DiskSpaceHealthIndicator implements HealthIndicator {
    private static final long THRESHOLD = 100L * 1024 * 1024;

    @Override
    public Health health() {
        long freeSpace = getFreeSpace();

        if (freeSpace < THRESHOLD) {
            return Health.down()
                    .withDetail("message", "Low disk space")
                    .build();
        }

        return Health.up()
                .withDetail("free_memory_bytes", freeSpace)
                .withDetail("threshold", THRESHOLD)
                .build();
    }

    protected long getFreeSpace() {
        File file = new File("/");

        return file.getFreeSpace();
    }
}