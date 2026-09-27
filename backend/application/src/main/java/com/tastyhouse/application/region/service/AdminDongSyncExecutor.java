package com.tastyhouse.application.region.service;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.application.region.port.out.write.AdminDongRepository;
import com.tastyhouse.application.region.port.out.write.AdminDongSyncResult;
import com.tastyhouse.application.shared.marker.BatchApp;

@Component
@BatchApp
public class AdminDongSyncExecutor {

    private final AdminDongRepository adminDongRepository;

    public AdminDongSyncExecutor(AdminDongRepository adminDongRepository) {
        this.adminDongRepository = adminDongRepository;
    }

    @Transactional
    public AdminDongSyncResult synchronizeInTx(List<AdminDong> adminDongs) {
        return adminDongRepository.synchronize(adminDongs);
    }
}
