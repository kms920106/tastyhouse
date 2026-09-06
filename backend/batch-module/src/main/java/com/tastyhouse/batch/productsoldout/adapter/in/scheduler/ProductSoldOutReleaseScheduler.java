package com.tastyhouse.batch.productsoldout.adapter.in.scheduler;

import com.tastyhouse.application.productsoldout.port.in.ReleaseExpiredSoldOutUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProductSoldOutReleaseScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProductSoldOutReleaseScheduler.class);

    private final ReleaseExpiredSoldOutUseCase releaseExpiredSoldOutUseCase;

    public ProductSoldOutReleaseScheduler(
        ReleaseExpiredSoldOutUseCase releaseExpiredSoldOutUseCase
    ) {
        this.releaseExpiredSoldOutUseCase = releaseExpiredSoldOutUseCase;
    }

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
