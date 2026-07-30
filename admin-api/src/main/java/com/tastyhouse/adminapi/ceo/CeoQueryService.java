package com.tastyhouse.adminapi.ceo;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.infrastructure.ceo.query.CeoListItemResult;
import com.tastyhouse.infrastructure.ceo.query.CeoQueryDao;
import com.tastyhouse.adminapi.ceo.response.CeoListItemResponse;

/**
 * admin용 점주(ceo) 조회 서비스.
 *
 * <p>infra read 어댑터({@link CeoQueryDao})만 주입해 조회하고 Response를 조립한다. 점주 계정의
 * 생성·수정은 ceo-api가 담당하므로 이 모듈에는 CommandService를 두지 않는다.
 *
 * <p>가게에 점주를 배정하는 Select 드롭다운을 채우기 위해 전체 점주 목록을 반환한다. core enum은
 * HTTP 경계로 노출하지 않으므로 Response 조립 시 {@code name()} 문자열로 되돌린다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CeoQueryService {

    private final CeoQueryDao ceoQueryDao;

    public List<CeoListItemResponse> getCeos() {
        return ceoQueryDao.findAllCeos().stream()
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
