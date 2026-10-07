package com.tastyhouse.application.notification.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.notification.vo.NotificationId;
import com.tastyhouse.application.notification.port.in.NotificationMarkAsReadCommand;
import com.tastyhouse.application.notification.port.in.NotificationMarkAsReadUseCase;

@Service
@Transactional
class NotificationMarkAsReadService implements NotificationMarkAsReadUseCase {

    private final NotificationService notificationService;

    public NotificationMarkAsReadService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void markAsRead(NotificationMarkAsReadCommand command) {
        NotificationId notificationId = NotificationId.of(command.notificationId());
        MemberId targetMemberId = MemberId.of(command.memberId());
        notificationService.markAsRead(notificationId, targetMemberId, LocalDateTime.now());
    }
}
