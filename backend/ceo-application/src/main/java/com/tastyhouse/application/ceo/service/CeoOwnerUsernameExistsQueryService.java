package com.tastyhouse.application.ceo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.in.CeoOwnerUsernameExistsQueryUseCase;
import com.tastyhouse.application.ceo.port.out.CeoOwnerQueryPort;

@Service
@Transactional(readOnly = true)
class CeoOwnerUsernameExistsQueryService implements CeoOwnerUsernameExistsQueryUseCase {

    private final CeoOwnerQueryPort ceoOwnerQueryPort;

    public CeoOwnerUsernameExistsQueryService(CeoOwnerQueryPort ceoOwnerQueryPort) {
        this.ceoOwnerQueryPort = ceoOwnerQueryPort;
    }

    @Override
    public boolean existsByUsername(String username) {
        return ceoOwnerQueryPort.existsByUsername(username);
    }
}
