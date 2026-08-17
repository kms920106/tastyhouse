package com.tastyhouse.batch.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.service.ReviewBlindRequestService;

/**
 * 게시중단 만료 재노출 배치 서비스.
 *
 * <p>재노출 규칙 자체(상태 전이 가드·리뷰 존재 재확인·인덱스 동기화)는 도메인 서비스
 * ({@link ReviewBlindRequestService#expire})가 갖고, 이 서비스는 대상 조회와 건별 집계만 담당한다
 * (공통 지침 패턴 1 — 도메인 서비스는 {@code @Transactional}을 갖지 않는다).
 *
 * <p><b>이 클래스에는 {@code @Transactional}이 없다</b> — 건별 트랜잭션 경계는
 * {@link ReviewBlindExpirationExecutor}가 갖는다. 여기에 걸면 전체가 한 트랜잭션이 되어 한 건의 실패가
 * 전체를 되돌리므로 건별 격리라는 요구사항과 어긋난다({@code ReservationCommandService}가 재시도 루프를
 * 트랜잭션 밖에 두는 것과 같은 구조).
 *
 * <p>실패 요약은 예외가 아니라 <b>로그</b>로 남긴다 — 예외를 던지면 스케줄러가 잡아 삼키므로 성공 건수까지
 * 함께 잃는다. 건별 실패는 executor가 이미 개별적으로 로깅한다.
 */
@Service
public class ReviewBlindSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(ReviewBlindSchedulerService.class);

    private final ReviewBlindRequestService reviewBlindRequestService;
    private final ReviewBlindExpirationExecutor reviewBlindExpirationExecutor;

    public ReviewBlindSchedulerService(
        ReviewBlindRequestService reviewBlindRequestService,
        ReviewBlindExpirationExecutor reviewBlindExpirationExecutor
    ) {
        this.reviewBlindRequestService = reviewBlindRequestService;
        this.reviewBlindExpirationExecutor = reviewBlindExpirationExecutor;
    }

    /**
     * 재노출 기한이 지난 게시중단 건을 모두 재노출한다.
     *
     * <p>조회는 한 번만 하고, 전이는 executor를 통해 건별 독립 트랜잭션으로 처리한다.
     */
    public void expireBlindedReviews() {
        LocalDateTime now = LocalDateTime.now();
        List<ReviewBlindRequest> expirable = reviewBlindRequestService.findExpirableBlinds(now);

        if (expirable.isEmpty()) {
            log.info("게시중단 만료 대상 없음: now={}", now);
            return;
        }

        int succeeded = 0;
        int failed = 0;
        for (ReviewBlindRequest request : expirable) {
            if (reviewBlindExpirationExecutor.expire(request)) {
                succeeded++;
            } else {
                failed++;
            }
        }

        log.info("게시중단 만료 재노출 완료: now={}, 대상 {} 건, 성공 {} 건, 실패 {} 건",
            now, expirable.size(), succeeded, failed);
    }
}
