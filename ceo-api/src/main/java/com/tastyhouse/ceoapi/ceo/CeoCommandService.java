package com.tastyhouse.ceoapi.ceo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.ceo.domain.model.Ceo;
import com.tastyhouse.domain.ceo.domain.repository.CeoRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 점주 계정 command 서비스.
 *
 * <p>domain write 포트({@link CeoRepository})만 주입해 생성을 수행한다. 조회는
 * {@link CeoQueryService}가 담당한다.
 *
 * <p>{@code Ceo}는 update 경로가 없는 insert 전용 애그리거트다. 비밀번호는 이미 인코딩된 값을
 * 호출 측(시드)에서 전달받는다 — 시드가 자체 정책(기본 비밀번호 차단)에 따라 인코딩 시점을
 * 통제하기 때문이다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CeoCommandService {

    private final CeoRepository ceoRepository;

    /**
     * 신규 점주 계정을 생성한다. password는 이미 인코딩된 값이어야 한다.
     * username 중복 시 예외를 던진다.
     */
    public void createCeo(String username, String encodedPassword, String name) {
        if (ceoRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.CEO_USERNAME_DUPLICATED);
        }

        Ceo ceo = Ceo.create(username, encodedPassword, name);

        ceoRepository.save(ceo);
    }
}
