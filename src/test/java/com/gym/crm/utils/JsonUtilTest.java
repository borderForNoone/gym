package com.gym.crm.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonUtilTest {

    @Test
    void readJson_shouldThrowRuntimeException_whenResourceNotFound() {
        String nonExistentPath = "json/non-existent-file.json";

        assertThatThrownBy(() -> JsonUtil.readJson(nonExistentPath)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to read JSON resource: " + nonExistentPath);
    }
}