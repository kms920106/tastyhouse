package com.tastyhouse.batch.productsoldout.adapter.in.scheduler;

import com.tastyhouse.batchapplication.productsoldout.port.in.ReleaseExpiredSoldOutUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 품절 자동해제 스케줄러.
 *
 * <p>{@code ReviewBlindScheduler} 패턴 그대로 <b>스케줄러는 로깅·예외격리만</b> 담당하고 잡 본문은
 * {@link ReleaseExpiredSoldOutUseCase}에 둔다.
 */
@Component
public class ProductSoldOutReleaseScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProductSoldOutReleaseScheduler.class);

    private final ReleaseExpiredSoldOutUseCase releaseExpiredSoldOutUseCase;

    public ProductSoldOutReleaseScheduler(
        ReleaseExpiredSoldOutUseCase releaseExpiredSoldOutUseCase
    ) {
        this.releaseExpiredSoldOutUseCase = releaseExpiredSoldOutUseCase;
    }

    /**
     * 10분 주기로 실행 — 다른 배치가 하루 1회 새벽에 도는 것과 성격이 다르다.
     *
     * <p>"익일 가게 오픈 시간까지 품절"이 <b>오픈 직후에 풀려야</b> 의미가 있고, 품절 기간 입력 단위가
     * 10분이라 그보다 촘촘하게 돌 필요가 없다. 하루 1회로 두면 오전에 오픈한 가게가 다음 날 새벽까지
     * 품절로 남는다.
     */
    @Scheduled(cron = "${product.sold-out-release.cron:0 */10 * * * *}")
    public void releaseExpiredSoldOut() {
        log.info("=== 품절 자동해제 스케줄러 시작 ===");

        try {
            releaseExpiredSoldOutUseCase.releaseExpiredSoldOut();
            log.info("=== 품절 자동해제 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("품절 자동해제 중 오류 발생", e);
        }
    }
}
