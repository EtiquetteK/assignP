package com.PracticalAssignment.assignP.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class StatusService {

    private final Set<SseEmitter> emitters = new CopyOnWriteArraySet<>();

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30 minutes
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((ex) -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("connected"));
        } catch (IOException ignored) {}

        return emitter;
    }

    public void publishEvent(String eventName, Object data) {
        SseEmitter.SseEventBuilder builder = SseEmitter.event().name(eventName).data(data);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(builder);
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    public int activeEmitters() {
        return emitters.size();
    }
}
