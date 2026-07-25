package com.tastyhouse.core.domain.ceo.application;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.ceo.domain.model.Ceo;
import com.tastyhouse.core.domain.ceo.domain.repository.CeoRepository;
import com.tastyhouse.core.domain.ceo.domain.vo.CeoId;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CeoQueryService {

    private final CeoRepository ceoRepository;

    public Optional<Ceo> findByUsername(String username) {
        return ceoRepository.findByUsername(username);
    }

    public Ceo getById(CeoId ceoId) {
        return ceoRepository.findById(ceoId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CEO_NOT_FOUND));
    }

    public boolean existsByUsername(String username) {
        return ceoRepository.existsByUsername(username);
    }
}
