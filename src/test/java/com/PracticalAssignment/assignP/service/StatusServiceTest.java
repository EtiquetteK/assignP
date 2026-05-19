package com.PracticalAssignment.assignP.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;

public class StatusServiceTest {

    private StatusService statusService;

    @BeforeEach
    void setUp() {
        statusService = new StatusService();
    }

    @Test
    void testCreateEmitter() {
        SseEmitter emitter = statusService.createEmitter();

        assertNotNull(emitter);
        assertEquals(1, statusService.activeEmitters());
    }

    @Test
    void testMultipleEmitters() {
        SseEmitter emitter1 = statusService.createEmitter();
        SseEmitter emitter2 = statusService.createEmitter();
        SseEmitter emitter3 = statusService.createEmitter();

        assertEquals(3, statusService.activeEmitters());
    }

    @Test
    void testPublishEventToMultipleEmitters() {
        SseEmitter emitter1 = statusService.createEmitter();
        SseEmitter emitter2 = statusService.createEmitter();

        // Should not throw exception
        statusService.publishEvent("test_event", "test_data");

        assertEquals(2, statusService.activeEmitters());
    }

    @Test
    void testPublishEventWithVariousDataTypes() {
        SseEmitter emitter = statusService.createEmitter();

        // Test with string
        statusService.publishEvent("string_event", "string_data");

        // Test with map
        statusService.publishEvent("map_event", java.util.Map.of("key", "value"));

        // Test with number
        statusService.publishEvent("number_event", 42);

        assertEquals(1, statusService.activeEmitters());
    }

    @Test
    void testActiveEmittersCount() {
        assertEquals(0, statusService.activeEmitters());

        statusService.createEmitter();
        assertEquals(1, statusService.activeEmitters());

        statusService.createEmitter();
        statusService.createEmitter();
        assertEquals(3, statusService.activeEmitters());
    }
}
