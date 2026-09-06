package com.tastyhouse.application.admin.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.admin.model.Admin;
import com.tastyhouse.domain.admin.repository.AdminRepository;
import com.tastyhouse.application.admin.port.in.AdminQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class AdminQueryService implements AdminQueryUseCase {

    private final AdminRepository adminRepository;

    public AdminQueryService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Optional<Admin> findByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return adminRepository.existsByUsername(username);
    }
}
