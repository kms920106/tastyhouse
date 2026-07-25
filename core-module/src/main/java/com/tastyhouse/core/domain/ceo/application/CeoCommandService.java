package com.tastyhouse.core.domain.ceo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.ceo.domain.model.Ceo;
import com.tastyhouse.core.domain.ceo.domain.repository.CeoRepository;
import com.tastyhouse.core.domain.ceo.domain.vo.CeoId;
import com.tastyhouse.core.domain.ceo.application.dto.command.CeoCreateCommand;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class CeoCommandService {

    private final CeoRepository ceoRepository;

    /**
     * 신규 점주 계정을 생성한다. password는 이미 인코딩된 값이어야 한다.
     * username 중복 시 예외를 던진다.
     */
    public CeoId createCeo(CeoCreateCommand command) {
        if (ceoRepository.existsByUsername(command.username())) {
            throw new BusinessException(ErrorCode.CEO_USERNAME_DUPLICATED);
        }

        Ceo ceo = Ceo.create(
            command.username(),
            command.encodedPassword(),
            command.name()
        );

        return ceoRepository.save(ceo).getCeoId();
    }
}
