package com.tastyhouse.adminapplication.ceo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.out.CeoListItemResult;
import com.tastyhouse.application.ceo.port.out.CeoQueryPort;
import com.tastyhouse.adminapplication.ceo.response.CeoListItemResponse;
import com.tastyhouse.adminapplication.ceo.port.in.CeoQueryUseCase;

/**
 * admin용 점주(ceo) 조회 서비스.
 *
 * <p>읽기 포트({@link CeoQueryPort})만 주입해 조회하고 Response를 조립한다. 점주 계정의
 * 생성·수정은 ceo-api가 담당하므로 이 모듈에는 CommandService를 두지 않는다.
 *
 * <p>가게에 점주를 배정하는 Select 드롭다운을 채우기 위해 전체 점주 목록을 반환한다. core enum은
 * HTTP 경계로 노출하지 않으므로 Response 조립 시 {@code name()} 문자열로 되돌린다.
 */
@Service
@Transactional(readOnly = true)
public class CeoQueryService implements CeoQueryUseCase {

    private final CeoQueryPort ceoQueryPort;

    public CeoQueryService(CeoQueryPort ceoQueryPort) {
        this.ceoQueryPort = ceoQueryPort;
    }

    @Override
    public List<CeoListItemResponse> getCeos() {
        return ceoQueryPort.findAllCeos().stream()
            .map(this::toCeoListItemResponse)
            .toList();
    }

    private CeoListItemResponse toCeoListItemResponse(CeoListItemResult dto) {
        return CeoListItemResponse.of(
            dto.id(),
            dto.name(),
            dto.businessRegistrationNumber(),
            dto.status() != null ? dto.status().name() : null
        );
    }
}
