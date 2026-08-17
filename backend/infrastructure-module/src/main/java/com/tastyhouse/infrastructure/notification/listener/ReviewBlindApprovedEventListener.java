package com.tastyhouse.infrastructure.notification.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.notification.service.NotificationService;
import com.tastyhouse.domain.review.event.ReviewBlindApprovedEvent;

/**
 * 리뷰 게시중단 승인 이벤트를 받아 리뷰 작성자에게 인앱 알림을 적재하는 크로스커팅 리스너.
 *
 * <p>infrastructure-module에 두는 이유: 승인은 지금은 admin-api 경로뿐이지만, 알림 적재는 승인한 주체가
 * 아니라 "게시중단이 승인됐다"는 사실에 반응해야 한다. 특정 api 모듈에 두면 다른 모듈이 같은 이벤트를
 * 발행할 때 알림이 조용히 누락되므로, 모든 실행 모듈이 공통으로 의존하는 이 모듈이 소유한다.
 *
 * <p><b>동기가 아니라 AFTER_COMMIT인 이유</b>: "후처리가 실패하면 원본도 없던 일이 되어야 하는가?"에
 * <b>아니오</b>다. 알림 적재가 실패해도 게시중단 승인 자체는 유효하므로 「크로스 컨텍스트 후처리」
 * 판정표대로 리스너로 간다(인증코드 발송이 동기인 것과 반대 방향이며, 그 판정표가 두 경우를 가른다).
 *
 * <p>{@code AFTER_COMMIT} + {@code REQUIRES_NEW} + {@code @Async} <b>3종 세트를 반드시 함께 단다</b>
 * ({@code ReviewOwnerReplyEventListener}와 동형).
 * <ul>
 *   <li>{@code AFTER_COMMIT}만: 호출 스레드에서 동기 실행되어 알림 실패가 승인 API로 전파된다 —
 *       DB에는 게시중단이 반영됐는데 화면은 실패로 뜬다.</li>
 *   <li>{@code REQUIRES_NEW} 누락: 커밋될 트랜잭션이 없어 리스너가 조용히 아무것도 남기지 않는다.</li>
 * </ul>
 *
 * <p>알림 문구 자체는 도메인 서비스({@link NotificationService})와 그 문구 소유자가 갖는다 — 이 리스너는
 * 이벤트 수신과 트랜잭션 경계만 담당한다. 형제 리스너와 달리 가게명 조회가 없는 이유는 게시중단 안내
 * 문구가 가게명을 노출하지 않기 때문이다(고객에게 필요한 정보는 재노출 예정일이지 어느 가게가 요청했는지가
 * 아니며, 요청 주체를 알리면 리뷰 작성자와 점주 사이의 분쟁을 부추길 수 있다).
 */
@Component
public class ReviewBlindApprovedEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReviewBlindApprovedEventListener.class);

    private final NotificationService notificationService;

    public ReviewBlindApprovedEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReviewBlindApprovedEvent event) {
        Long notificationId = notificationService.notifyReviewBlindApproved(
            event.reviewerMemberId(),
            event.reviewId(),
            event.blindUntil()
        );

        log.info("게시중단 승인 알림 적재 완료 — notificationId={}, reviewId={}, memberId={}, blindRequestId={}, blindUntil={}",
            notificationId,
            event.reviewId().value(),
            event.reviewerMemberId().value(),
            event.blindRequestId().value(),
            event.blindUntil()
        );
    }
}
