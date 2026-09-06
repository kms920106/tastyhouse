package com.tastyhouse.application.region.service;

import com.tastyhouse.application.shared.marker.BatchApp;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.repository.AdminDongSyncResult;

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
