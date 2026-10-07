package com.tastyhouse.application.ceo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.application.ceo.port.in.CeoCreateCommand;
import com.tastyhouse.application.ceo.port.in.CeoCreateUseCase;
import com.tastyhouse.application.ceo.port.out.write.CeoPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;

@Service
@Transactional
class CeoCreateService implements CeoCreateUseCase {

    private final CeoPersistencePort ceoPersistencePort;

    public CeoCreateService(CeoPersistencePort ceoPersistencePort) {
        this.ceoPersistencePort = ceoPersistencePort;
    }

    @Override
    public void createCeo(CeoCreateCommand command) {
        String username = command.username();
        String encodedPassword = command.encodedPassword();
        String name = command.name();

        if (ceoPersistencePort.existsByUsername(username)) {
            throw new ApplicationException(CeoErrorCode.CEO_USERNAME_DUPLICATED);
        }

        Ceo ceo = Ceo.create(username, encodedPassword, name);

        ceoPersistencePort.save(ceo);
    }
}
