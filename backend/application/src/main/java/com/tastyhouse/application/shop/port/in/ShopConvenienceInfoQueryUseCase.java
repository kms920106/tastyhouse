package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.shop.port.out.ShopAmenityAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopConvenienceInfoResult;

/**
 * 가게 편의 정보 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopConvenienceInfoQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
@CeoApp
public interface ShopConvenienceInfoQueryUseCase {

    Optional<ShopConvenienceInfoResult> getConvenienceInfo(Long ceoId, Long shopId);

    List<ShopAmenityAssignmentResult> getAmenities(Long ceoId, Long shopId);
}
