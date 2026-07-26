package com.tastyhouse.adminapi.ceo;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.ceo.application.CeoQueryService;
import com.tastyhouse.core.domain.ceo.application.dto.result.CeoListItemResult;
import com.tastyhouse.adminapi.ceo.response.CeoListItemResponse;

/**
 * admin용 점주(ceo) 조회 중개 서비스. 가게에 점주를 배정하는 Select 드롭다운을 채우기 위해
 * 전체 점주 목록을 반환한다.
 */
@Service
@RequiredArgsConstructor
public class CeoService {

    private final CeoQueryService ceoQueryService;

    public List<CeoListItemResponse> getCeos() {
        return ceoQueryService.findAll().stream()
            .map(this::toCeoListItemResponse)
            .toList();
    }

    private CeoListItemResponse toCeoListItemResponse(CeoListItemResult dto) {
        return CeoListItemResponse.of(
            dto.id(),
            dto.name(),
            dto.businessRegistrationNumber(),
            dto.status().name()
        );
    }
}
