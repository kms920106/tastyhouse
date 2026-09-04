package com.tastyhouse.batch.region.adapter.in.scheduler;

import com.tastyhouse.application.region.port.in.SynchronizeAdminDongsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 행정동 마스터 동기화 트리거.
 *
 * <p>행정구역 개편은 연 몇 회 수준이라 잦은 실행이 의미 없고, 원천도 그 주기로만 갱신된다. 매월 1일
 * 새벽에 한 번만 돌려 개편을 뒤늦게라도 따라잡게 한다(다른 배치와 겹치지 않는 04시대).
 */
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
