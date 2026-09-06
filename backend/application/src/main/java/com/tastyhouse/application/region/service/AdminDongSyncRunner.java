package com.tastyhouse.application.region.service;

import com.tastyhouse.application.shared.marker.BatchApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@BatchApp
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
