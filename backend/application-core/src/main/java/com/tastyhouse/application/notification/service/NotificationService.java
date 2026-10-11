package com.tastyhouse.application.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.notification.model.Notification;
import com.tastyhouse.domain.notification.model.NotificationTargetType;
import com.tastyhouse.domain.notification.model.NotificationType;
import com.tastyhouse.domain.notification.vo.NotificationId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.notification.port.out.write.NotificationLoadPort;
import com.tastyhouse.application.notification.port.out.write.NotificationSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
public class NotificationService {

    private final NotificationLoadPort notificationLoadPort;
    private final NotificationSavePort notificationSavePort;

    public NotificationService(NotificationLoadPort notificationLoadPort, NotificationSavePort notificationSavePort) {
        this.notificationLoadPort = notificationLoadPort;
        this.notificationSavePort = notificationSavePort;
    }

    public Long notify(
        MemberId memberId,
        NotificationType type,
        String title,
        String body,
        NotificationTargetType targetType,
        Long targetId
    ) {
        Notification saved = notificationSavePort.save(
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
        Notification notification = notificationLoadPort.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getMemberId().equals(memberId)) {
            throw new ApplicationException(ApplicationErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notification.markAsRead(readAt);
        notificationSavePort.save(notification);
    }

    public void markAllAsRead(MemberId memberId, LocalDateTime readAt) {
        List<Notification> unread = notificationLoadPort.findUnreadByMemberId(memberId);
        if (unread.isEmpty()) {
            return;
        }

        unread.forEach(notification -> notification.markAsRead(readAt));
        notificationSavePort.saveAll(unread);
    }
}
