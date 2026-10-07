package com.tastyhouse.application.ceo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.in.CeoOwnerUsernameExistsQueryUseCase;
import com.tastyhouse.application.ceo.port.out.write.CeoPersistencePort;

@Service
@Transactional(readOnly = true)
class CeoOwnerUsernameExistsQueryService implements CeoOwnerUsernameExistsQueryUseCase {

    private final CeoPersistencePort ceoPersistencePort;

    public CeoOwnerUsernameExistsQueryService(CeoPersistencePort ceoPersistencePort) {
        this.ceoPersistencePort = ceoPersistencePort;
    }

    @Override
    public boolean existsByUsername(String username) {
        return ceoPersistencePort.existsByUsername(username);
    }
}
