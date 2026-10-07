package com.tastyhouse.application.notification.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.notification.port.in.NotificationMarkAllAsReadCommand;
import com.tastyhouse.application.notification.port.in.NotificationMarkAllAsReadUseCase;

@Service
@Transactional
class NotificationMarkAllAsReadService implements NotificationMarkAllAsReadUseCase {

    private final NotificationService notificationService;

    public NotificationMarkAllAsReadService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void markAllAsRead(NotificationMarkAllAsReadCommand command) {
        MemberId targetMemberId = MemberId.of(command.memberId());
        notificationService.markAllAsRead(targetMemberId, LocalDateTime.now());
    }
}
