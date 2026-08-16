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
import com.tastyhouse.domain.review.event.ReviewOwnerReplyCreatedEvent;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;

/**
 * 사장님 답변 등록 이벤트를 받아 리뷰 작성자에게 인앱 알림을 적재하는 크로스커팅 리스너.
 *
 * <p>infrastructure-module에 두는 이유: 답변 등록은 지금은 ceo-api 경로뿐이지만, 알림 적재는 답변을
 * 등록한 주체가 아니라 "답변이 등록됐다"는 사실에 반응해야 한다. 특정 api 모듈에 두면 다른 모듈이 같은
 * 이벤트를 발행할 때 알림이 조용히 누락되므로, 모든 실행 모듈이 공통으로 의존하는 이 모듈이 소유한다.
 *
 * <p><b>동기가 아니라 AFTER_COMMIT인 이유</b>: "후처리가 실패하면 원본도 없던 일이 되어야 하는가?"에
 * <b>아니오</b>다. 알림 적재가 실패해도 답변 등록 자체는 유효하므로, 「크로스 컨텍스트 후처리」 판정표대로
 * 리스너로 간다(인증코드 발송이 동기인 것과 반대 방향이며, 그 판정표가 두 경우를 가른다).
 *
 * <p>{@code AFTER_COMMIT}은 등록 트랜잭션이 끝난 뒤이므로 {@code REQUIRES_NEW}를 함께 달아야 적재가
 * 커밋된다. 이것이 없으면 리스너가 조용히 아무것도 남기지 않는다.
 *
 * <p>{@code @Async}가 함께 필요한 이유: {@code AFTER_COMMIT}만으로는 호출 스레드에서 동기 실행되어
 * 여기서 난 예외가 답변 등록 API로 전파된다. 그러면 DB에는 답변이 남았는데 화면은 실패로 뜨는, 위
 * "알림이 실패해도 답변 등록은 유효하다"와 정면으로 어긋나는 상태가 된다. 형제 리스너
 * {@code ProductMenuReviewEventListener}와 같은 3종 세트({@code @Async} + {@code AFTER_COMMIT} +
 * {@code REQUIRES_NEW})를 유지한다.
 *
 * <p>알림 문구 자체는 도메인 서비스({@link NotificationService})와 그 문구 소유자가 갖고, 이 리스너는
 * 이벤트 수신·트랜잭션 경계·문구 조립에 필요한 가게명 조회만 담당한다.
 */
@Component
public class ReviewOwnerReplyEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReviewOwnerReplyEventListener.class);

    private final NotificationService notificationService;
    private final ShopQueryDao shopQueryDao;

    public ReviewOwnerReplyEventListener(NotificationService notificationService, ShopQueryDao shopQueryDao) {
        this.notificationService = notificationService;
        this.shopQueryDao = shopQueryDao;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReviewOwnerReplyCreatedEvent event) {
        String shopName = shopQueryDao.findShopName(event.shopId().value()).orElse(null);

        Long notificationId = notificationService.notifyReviewOwnerReply(
            event.reviewerMemberId(),
            event.reviewId(),
            shopName
        );

        log.info("사장님 답변 알림 적재 완료 — notificationId={}, reviewId={}, memberId={}, shopId={}, ownerReplyId={}",
            notificationId,
            event.reviewId().value(),
            event.reviewerMemberId().value(),
            event.shopId().value(),
            event.ownerReplyId().value()
        );
    }
}
