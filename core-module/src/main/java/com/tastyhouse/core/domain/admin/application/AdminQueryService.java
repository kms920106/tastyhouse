package com.tastyhouse.core.domain.admin.application;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.admin.domain.model.Admin;
import com.tastyhouse.core.domain.admin.domain.repository.AdminRepository;
import com.tastyhouse.core.domain.admin.domain.vo.AdminId;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminQueryService {

    private final AdminRepository adminRepository;

    public Optional<Admin> findByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    public Admin getById(AdminId adminId) {
        return adminRepository.findById(adminId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ADMIN_NOT_FOUND));
    }

    public Admin getById(Long adminId) {
        return getById(new AdminId(adminId));
    }

    public boolean existsByUsername(String username) {
        return adminRepository.existsByUsername(username);
    }
}
