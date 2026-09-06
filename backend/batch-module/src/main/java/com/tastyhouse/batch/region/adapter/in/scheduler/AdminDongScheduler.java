package com.tastyhouse.batch.region.adapter.in.scheduler;

import com.tastyhouse.application.region.port.in.SynchronizeAdminDongsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AdminDongScheduler {

    private static final Logger log = LoggerFactory.getLogger(AdminDongScheduler.class);

    private final SynchronizeAdminDongsUseCase synchronizeAdminDongsUseCase;

    public AdminDongScheduler(SynchronizeAdminDongsUseCase synchronizeAdminDongsUseCase) {
        this.synchronizeAdminDongsUseCase = synchronizeAdminDongsUseCase;
    }

    @Scheduled(cron = "0 0 4 1 * *")
    public void synchronizeAdminDongs() {
        log.info("=== 행정동 마스터 동기화 스케줄러 시작 ===");
        try {
            synchronizeAdminDongsUseCase.synchronizeAdminDongs();
            log.info("=== 행정동 마스터 동기화 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("행정동 마스터 동기화 중 오류 발생", e);
        }
    }
}
