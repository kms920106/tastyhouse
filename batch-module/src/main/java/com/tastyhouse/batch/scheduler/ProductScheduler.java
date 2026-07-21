package com.tastyhouse.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused") // 스케줄러 활성화(@Scheduled 주석 해제) 전까지 의도적으로 미사용 상태 유지
public class ProductScheduler {

    private final ProductSchedulerService productSchedulerService;

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
