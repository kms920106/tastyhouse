package com.tastyhouse.application.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.notification.model.Notification;
import com.tastyhouse.domain.notification.model.NotificationTargetType;
import com.tastyhouse.domain.notification.model.NotificationType;
import com.tastyhouse.domain.notification.vo.NotificationId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.notification.port.out.write.NotificationPersistencePort;

@Service
public class NotificationService {

    private final NotificationPersistencePort notificationPersistencePort;

    public NotificationService(NotificationPersistencePort notificationPersistencePort) {
        this.notificationPersistencePort = notificationPersistencePort;
    }

    public Long notify(
        MemberId memberId,
        NotificationType type,
        String title,
        String body,
        NotificationTargetType targetType,
        Long targetId
    ) {
        Notification saved = notificationPersistencePort.save(
            Notification.of(memberId, type, title, body, targetType, targetId)
        );
        return saved.getId();
    }

    public Long notifyReviewOwnerReply(MemberId reviewerMemberId, ReviewId reviewId, String shopName) {
        return notify(
            reviewerMemberId,
            NotificationType.REVIEW_OWNER_REPLY,
            NotificationMessage.reviewOwnerReplyTitle(),
            NotificationMessage.reviewOwnerReplyBody(shopName),
            NotificationTargetType.REVIEW,
            reviewId.value()
        );
    }

    public Long notifyReviewBlindApproved(MemberId reviewerMemberId, ReviewId reviewId, LocalDateTime blindUntil) {
        return notify(
            reviewerMemberId,
            NotificationType.REVIEW_BLIND_APPROVED,
            NotificationMessage.reviewBlindApprovedTitle(),
            NotificationMessage.reviewBlindApprovedBody(blindUntil),
            NotificationTargetType.REVIEW,
            reviewId.value()
        );
    }

    public void markAsRead(NotificationId notificationId, MemberId memberId, LocalDateTime readAt) {
        Notification notification = notificationPersistencePort.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notification.markAsRead(readAt);
        notificationPersistencePort.save(notification);
    }

    public void markAllAsRead(MemberId memberId, LocalDateTime readAt) {
        List<Notification> unread = notificationPersistencePort.findUnreadByMemberId(memberId);
        if (unread.isEmpty()) {
            return;
        }

        unread.forEach(notification -> notification.markAsRead(readAt));
        notificationPersistencePort.saveAll(unread);
    }
}
