package com.tastyhouse.application.ceo.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.application.ceo.port.in.CeoOwnerQueryUseCase;
import com.tastyhouse.application.ceo.port.out.write.CeoPersistencePort;
import com.tastyhouse.application.shared.marker.CeoApp;

@Service
@CeoApp
@Transactional(readOnly = true)
public class CeoOwnerQueryService implements CeoOwnerQueryUseCase {

    private final CeoPersistencePort ceoPersistencePort;

    public CeoOwnerQueryService(CeoPersistencePort ceoPersistencePort) {
        this.ceoPersistencePort = ceoPersistencePort;
    }

    public Optional<Ceo> findByUsername(String username) {
        return ceoPersistencePort.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return ceoPersistencePort.existsByUsername(username);
    }
}
