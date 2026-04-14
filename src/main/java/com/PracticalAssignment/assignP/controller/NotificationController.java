package com.PracticalAssignment.assignP.controller;

import com.PracticalAssignment.assignP.dto.NotificationDTO;
import com.PracticalAssignment.assignP.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationDTO> getMyNotifications(@RequestParam(defaultValue = "false") boolean unreadOnly) {
        return notificationService.getMyNotifications(unreadOnly);
    }

    @PostMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }

    @PostMapping("/read-all")
    public void markAllAsRead() {
        notificationService.markAllAsRead();
    }
}
