package com.example.g7_back_mobile.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import com.example.g7_back_mobile.controllers.dtos.NotificationEvent;

import java.util.*;
import java.util.concurrent.*;

@Service
public class LongPollService {

    private final Map<String, Queue<NotificationEvent>> pending = new ConcurrentHashMap<>();
    private final Map<String, List<DeferredResult<ResponseEntity<List<NotificationEvent>>>>> waiters = new ConcurrentHashMap<>();

    private static final long TIMEOUT_MS = 25_000L;

    public DeferredResult<ResponseEntity<List<NotificationEvent>>> waitForEvents(String userId, long since) {
        DeferredResult<ResponseEntity<List<NotificationEvent>>> dr = new DeferredResult<>(TIMEOUT_MS);
        // si existen eventos pendientes que sean > since, devolver inmediatamente
        List<NotificationEvent> ready = drainPending(userId, since);
        if (!ready.isEmpty()) {
            dr.setResult(ResponseEntity.ok(ready));
            return dr;
        }
        waiters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(dr);

        dr.onTimeout(() -> {
            dr.setResult(ResponseEntity.ok(Collections.emptyList()));
            waiters.getOrDefault(userId, Collections.emptyList()).remove(dr);
        });
        dr.onCompletion(() -> {
            waiters.getOrDefault(userId, Collections.emptyList()).remove(dr);
        });
        return dr;
    }

    private List<NotificationEvent> drainPending(String userId, long since) {
        Queue<NotificationEvent> q = pending.get(userId);
        if (q == null) return Collections.emptyList();
        List<NotificationEvent> out = new ArrayList<>();
        Iterator<NotificationEvent> it = q.iterator();
        while (it.hasNext()) {
            NotificationEvent e = it.next();
            if (e.getCreatedAt() > since) {
                out.add(e);
                it.remove();
            }
        }
        return out;
    }

    // publicar evento para un usuario (invocado desde lógica de reprogramación/cancelación)
    public void publishEvent(String userId, NotificationEvent event) {
        pending.computeIfAbsent(userId, k -> new ConcurrentLinkedQueue<>()).add(event);
        List<DeferredResult<ResponseEntity<List<NotificationEvent>>>> list = waiters.get(userId);
        if (list != null && !list.isEmpty()) {
            for (DeferredResult<ResponseEntity<List<NotificationEvent>>> dr : list) {
                try {
                    dr.setResult(ResponseEntity.ok(Collections.singletonList(event)));
                } catch (Exception ignored) {}
            }
            list.clear();
        }
    }
}