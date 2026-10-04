package com.tastyhouse.application.ceo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.application.ceo.port.in.CeoCommandUseCase;
import com.tastyhouse.application.ceo.port.in.CeoCreateCommand;
import com.tastyhouse.application.ceo.port.out.write.CeoPersistencePort;

@Service
@Transactional
class CeoCommandService implements CeoCommandUseCase {

    private final CeoPersistencePort ceoPersistencePort;

    public CeoCommandService(CeoPersistencePort ceoPersistencePort) {
        this.ceoPersistencePort = ceoPersistencePort;
    }

    @Override
    public void createCeo(CeoCreateCommand command) {
        String username = command.username();
        String encodedPassword = command.encodedPassword();
        String name = command.name();

        if (ceoPersistencePort.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.CEO_USERNAME_DUPLICATED);
        }

        Ceo ceo = Ceo.create(username, encodedPassword, name);

        ceoPersistencePort.save(ceo);
    }
}
