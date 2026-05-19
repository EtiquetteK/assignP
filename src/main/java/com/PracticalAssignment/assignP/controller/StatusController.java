package com.PracticalAssignment.assignP.controller;

import com.PracticalAssignment.assignP.service.StatusService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    private final StatusService statusService;

    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    // Client connects here to receive server-sent events
    @GetMapping(value = "/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamStatus() {
        return statusService.createEmitter();
    }

    // Publish a status event to connected clients (requires auth)
    @PostMapping("/publish")
    public ResponseEntity<?> publish(@RequestBody Map<String, Object> body) {
        String event = body.getOrDefault("event", "status").toString();
        Object data = body.getOrDefault("data", "");
        statusService.publishEvent(event, data);
        return ResponseEntity.ok(Map.of("sent", true, "event", event));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(Map.of("emitters", statusService.activeEmitters()));
    }
}
