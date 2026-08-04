package laboratorioxyz.com.ZoneControl.modulo_control_acceso.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Publicador de eventos SSE para el panel de zonas en vivo (§9.3 item 2.3).
 * Mantiene los SseEmitter suscritos, con heartbeat cada 15s para evitar que
 * los proxies cierren conexiones inactivas.
 */
@Service
@Slf4j
public class RealtimeEventPublisher {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService heartbeat = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "sse-heartbeat");
        t.setDaemon(true);
        return t;
    });

    public RealtimeEventPublisher() {
        heartbeat.scheduleAtFixedRate(this::heartbeatAll, 15, 15, TimeUnit.SECONDS);
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(300_000L);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        emitters.add(emitter);
        return emitter;
    }

    public void publish(String type, Map<String, Object> data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(type).data(data));
            } catch (IOException | IllegalStateException e) {
                emitters.remove(emitter);
            }
        }
    }

    public void sendSnapshot(SseEmitter emitter, Map<String, Object> snapshot) {
        try {
            emitter.send(SseEmitter.event().name("snapshot").data(snapshot));
        } catch (IOException | IllegalStateException e) {
            emitters.remove(emitter);
        }
    }

    private void heartbeatAll() {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").comment("keep-alive"));
            } catch (IOException | IllegalStateException e) {
                emitters.remove(emitter);
            }
        }
    }
}
