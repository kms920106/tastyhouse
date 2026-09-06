package com.tastyhouse.application.notification.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.notification.service.NotificationService;
import com.tastyhouse.domain.notification.vo.NotificationId;
import com.tastyhouse.application.notification.port.in.NotificationCommandUseCase;
import com.tastyhouse.application.notification.port.in.NotificationMarkAllAsReadCommand;
import com.tastyhouse.application.notification.port.in.NotificationMarkAsReadCommand;

@Service
@WebApp
@Transactional
public class NotificationCommandService implements NotificationCommandUseCase {

    private final NotificationService notificationService;

    public NotificationCommandService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void markAsRead(NotificationMarkAsReadCommand command) {
        NotificationId notificationId = NotificationId.of(command.notificationId());
        MemberId targetMemberId = MemberId.of(command.memberId());
        notificationService.markAsRead(notificationId, targetMemberId, LocalDateTime.now());
    }

    @Override
    public void markAllAsRead(NotificationMarkAllAsReadCommand command) {
        MemberId targetMemberId = MemberId.of(command.memberId());
        notificationService.markAllAsRead(targetMemberId, LocalDateTime.now());
    }
}
