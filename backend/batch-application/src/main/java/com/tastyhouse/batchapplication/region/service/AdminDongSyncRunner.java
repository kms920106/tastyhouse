package com.tastyhouse.batchapplication.region.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 행정동 마스터 동기화를 기동 직후 1회 수행하는 수동 트리거.
 *
 * <p>정기 실행은 batch-module의 {@code AdminDongScheduler}(매월 1일)가
 * 담당하지만, 마스터가 비어 있는 최초 투입이나
 * 행정구역 개편을 즉시 반영해야 할 때 다음 cron까지 기다릴 수 없다. 그때 이 러너를 켜서 한 번 돌린다.
 *
 * <pre>
 * cd backend &amp;&amp; java -jar batch-module/build/libs/batch-module-0.0.1-SNAPSHOT.jar \
 *     --region.admin-dong.sync-on-startup=true
 * </pre>
 *
 * <p><b>기본값은 꺼짐이다.</b> 항상 켜 두면 배치 앱을 재기동할 때마다 30MB를 내려받아 전국 마스터를
 * 다시 쓰게 되는데, 그 비용이 재기동마다 반복될 이유가 없다.
 */
@Component
@ConditionalOnProperty(name = "region.admin-dong.sync-on-startup", havingValue = "true")
public class AdminDongSyncRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminDongSyncRunner.class);

    private final AdminDongSchedulerService adminDongSchedulerService;

    public AdminDongSyncRunner(AdminDongSchedulerService adminDongSchedulerService) {
        this.adminDongSchedulerService = adminDongSchedulerService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== 행정동 마스터 동기화 수동 실행 시작(sync-on-startup) ===");
        adminDongSchedulerService.synchronizeAdminDongs();
        log.info("=== 행정동 마스터 동기화 수동 실행 완료 ===");
    }
}
