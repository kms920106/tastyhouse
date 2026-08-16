package com.tastyhouse.domain.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.notification.model.Notification;
import com.tastyhouse.domain.notification.model.NotificationTargetType;
import com.tastyhouse.domain.notification.model.NotificationType;
import com.tastyhouse.domain.notification.repository.NotificationRepository;
import com.tastyhouse.domain.notification.vo.NotificationId;
import com.tastyhouse.domain.review.vo.ReviewId;

/**
 * 인앱 알림 적재·읽음 처리 불변식(도메인 서비스).
 *
 * <p><b>알림 문구는 소비처가 아니라 {@link NotificationMessage}가 소유한다.</b> 유형별 적재 메서드
 * ({@link #notifyReviewOwnerReply})를 두어 리스너가 title/body를 직접 조립하지 않게 한다 — 소비처가
 * 늘어도 같은 알림의 표현이 갈리지 않는다. 범용 {@link #notify}는 문구가 이미 정해진 경우를 위한 하위 API다.
 *
 * <p><b>읽음 처리의 소유권 검증을 여기서 한다</b>({@link #markAsRead}) — 남의 알림 식별자를 넣어
 * 읽음 처리하는 IDOR을 막기 위함이고, 불일치는 403이 아니라 {@code NOTIFICATION_NOT_FOUND}(404)로
 * 응답한다. 403은 "그 알림이 존재한다"는 사실을 알려주기 때문이다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code NotificationDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는 api 모듈
 * CommandService(또는 AFTER_COMMIT 리스너)가 선언한다.
 */
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * 알림 1건을 적재한다.
     *
     * @param targetType 이동 대상 유형. 이동 대상이 없으면 {@code targetId}와 함께 null
     * @return 생성된 알림 식별자
     */
    public Long notify(
        MemberId memberId,
        NotificationType type,
        String title,
        String body,
        NotificationTargetType targetType,
        Long targetId
    ) {
        Notification saved = notificationRepository.save(
            Notification.of(memberId, type, title, body, targetType, targetId)
        );
        return saved.getId();
    }

    /**
     * 사장님 답변 등록 알림을 적재한다 — 리뷰 작성자에게 보내고 누르면 그 리뷰 상세로 이동한다.
     *
     * @param shopName 문구에 넣을 가게명. 조회 실패로 비어 있으면 가게명 없는 문구로 대체된다
     * @return 생성된 알림 식별자
     */
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

    /**
     * 단건 읽음 처리(멱등).
     *
     * <p>수신자가 아니면 {@code NOTIFICATION_NOT_FOUND}(404)다 — 존재 여부를 숨기기 위해 403을 쓰지 않는다.
     *
     * @param readAt 읽은 시각. 도메인이 {@code LocalDateTime.now()}를 직접 호출하지 않도록 호출부가 넘긴다
     */
    public void markAsRead(NotificationId notificationId, MemberId memberId, LocalDateTime readAt) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notification.markAsRead(readAt);
        notificationRepository.save(notification);
    }

    /**
     * 해당 회원의 미읽음 알림을 모두 읽음 처리한다. 미읽음이 없으면 아무것도 하지 않는다(멱등).
     */
    public void markAllAsRead(MemberId memberId, LocalDateTime readAt) {
        List<Notification> unread = notificationRepository.findUnreadByMemberId(memberId);
        if (unread.isEmpty()) {
            return;
        }

        unread.forEach(notification -> notification.markAsRead(readAt));
        notificationRepository.saveAll(unread);
    }
}
