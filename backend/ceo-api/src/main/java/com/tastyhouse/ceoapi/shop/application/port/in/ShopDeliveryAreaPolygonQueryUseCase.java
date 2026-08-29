package com.tastyhouse.ceoapi.shop.application.port.in;

import java.util.List;

import com.tastyhouse.ceoapi.shop.adapter.in.web.request.GeoPointRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaPolygonPreviewResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaPolygonResponse;

/**
 * 가게 배달지역 도형 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopDeliveryAreaPolygonQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ShopDeliveryAreaPolygonQueryUseCase {

    ShopDeliveryAreaPolygonResponse getPolygon(Long ceoId, Long shopId);

    ShopDeliveryAreaPolygonPreviewResponse previewPolygon(Long ceoId, Long shopId, List<List<GeoPointRequest>> rings);
}
