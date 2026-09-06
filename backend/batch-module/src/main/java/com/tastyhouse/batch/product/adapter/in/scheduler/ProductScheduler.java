package com.tastyhouse.batch.product.adapter.in.scheduler;

import com.tastyhouse.application.product.port.in.SyncProductOptionsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused")
public class ProductScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProductScheduler.class);

    private final SyncProductOptionsUseCase syncProductOptionsUseCase;

    public ProductScheduler(SyncProductOptionsUseCase syncProductOptionsUseCase) {
        this.syncProductOptionsUseCase = syncProductOptionsUseCase;
    }

    @SuppressWarnings("unused")
    public void crawlAndSaveProductOptions() {
        try {
            syncProductOptionsUseCase.crawlAndSaveProductOptions();
        } catch (Exception e) {
            log.error("상품 옵션 크롤링 중 오류 발생", e);
        }
    }
}
