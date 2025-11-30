package com.example.g7_back_mobile.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import com.example.g7_back_mobile.controllers.dtos.NotificationEvent;
import com.example.g7_back_mobile.services.LongPollService;

import java.util.List;

@RestController
@RequestMapping("/api/longpoll")
public class LongPollController {

    private final LongPollService service;

    public LongPollController(LongPollService service) {
        this.service = service;
    }

    @GetMapping("/events")
    public DeferredResult<ResponseEntity<List<NotificationEvent>>> events(
            @RequestParam String userId,
            @RequestParam(required = false) Long since) {
        return service.waitForEvents(userId, since == null ? 0L : since);
    }
}