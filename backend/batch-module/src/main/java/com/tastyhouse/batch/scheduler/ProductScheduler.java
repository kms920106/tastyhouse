package com.tastyhouse.batch.scheduler;

//import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused") // 스케줄러 활성화(@Scheduled 주석 해제) 전까지 의도적으로 미사용 상태 유지
public class ProductScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProductScheduler.class);

    private final ProductSchedulerService productSchedulerService;

    public ProductScheduler(ProductSchedulerService productSchedulerService) {
        this.productSchedulerService = productSchedulerService;
    }

//    @Scheduled(fixedDelay = 10000)
    @SuppressWarnings("unused") // @Scheduled 활성화 전까지 의도적으로 미사용 상태 유지
    public void crawlAndSaveProductOptions() {
        try {
            productSchedulerService.crawlAndSaveProductOptions();
        } catch (Exception e) {
            log.error("상품 옵션 크롤링 중 오류 발생", e);
        }
    }
}
