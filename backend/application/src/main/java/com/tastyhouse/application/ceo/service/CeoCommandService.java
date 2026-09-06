package com.tastyhouse.application.ceo.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.in.CeoCommandUseCase;
import com.tastyhouse.application.ceo.port.in.CeoCreateCommand;
import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.domain.ceo.repository.CeoRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

@Service
@CeoApp
@Transactional
public class CeoCommandService implements CeoCommandUseCase {

    private final CeoRepository ceoRepository;

    public CeoCommandService(CeoRepository ceoRepository) {
        this.ceoRepository = ceoRepository;
    }

    @Override
    public void createCeo(CeoCreateCommand command) {
        String username = command.username();
        String encodedPassword = command.encodedPassword();
        String name = command.name();

        if (ceoRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.CEO_USERNAME_DUPLICATED);
        }

        Ceo ceo = Ceo.create(username, encodedPassword, name);

        ceoRepository.save(ceo);
    }
}
