package com.tastyhouse.application.region.service;

import com.tastyhouse.application.shared.marker.BatchApp;
import com.tastyhouse.application.region.port.in.SynchronizeAdminDongsUseCase;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tastyhouse.application.region.port.out.AdminDongBoundaryPort;
import com.tastyhouse.application.region.port.out.AdminDongBoundarySource;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongSyncResult;

@Service
@BatchApp
public class AdminDongSchedulerService implements SynchronizeAdminDongsUseCase {

    private static final Logger log = LoggerFactory.getLogger(AdminDongSchedulerService.class);

    private final AdminDongBoundaryPort adminDongBoundaryPort;
    private final AdminDongSyncExecutor adminDongSyncExecutor;

    public AdminDongSchedulerService(
        AdminDongBoundaryPort adminDongBoundaryPort,
        AdminDongSyncExecutor adminDongSyncExecutor
    ) {
        this.adminDongBoundaryPort = adminDongBoundaryPort;
        this.adminDongSyncExecutor = adminDongSyncExecutor;
    }

    @Override
    public void synchronizeAdminDongs() {

        List<AdminDongBoundarySource> sourceRows = adminDongBoundaryPort.fetchAll();

        List<AdminDong> adminDongs = sourceRows.stream()
            .map(AdminDongSchedulerService::toAdminDong)
            .toList();

        AdminDongSyncResult result = adminDongSyncExecutor.synchronizeInTx(adminDongs);
        log.info("행정동 마스터 동기화 결과: 신규 {}건, 갱신 {}건, 폐지 {}건 (반영 총 {}건)",
            result.inserted(), result.updated(), result.deactivated(), result.appliedCount());
    }

    private static AdminDong toAdminDong(AdminDongBoundarySource source) {
        return AdminDong.of(
            source.code(),
            source.sidoName(),
            source.sigunguName(),
            source.dongName(),
            true,
            source.center(),
            source.boundary()
        );
    }
}
