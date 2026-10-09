package com.tastyhouse.application.region.service;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.application.region.port.out.write.AdminDongSavePort;
import com.tastyhouse.application.region.port.out.write.AdminDongSyncResult;

@Component
public class AdminDongSyncExecutor {

    private final AdminDongSavePort adminDongSavePort;

    public AdminDongSyncExecutor(AdminDongSavePort adminDongSavePort) {
        this.adminDongSavePort = adminDongSavePort;
    }

    @Transactional
    public AdminDongSyncResult synchronizeInTx(List<AdminDong> adminDongs) {
        return adminDongSavePort.synchronize(adminDongs);
    }
}
