package com.tastyhouse.ceoapplication.shop.service;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoRing;
import com.tastyhouse.application.shop.port.out.GeoPointView;
import com.tastyhouse.ceoapplication.shop.port.in.GeoPointCommand;

/**
 * HTTP 경계의 좌표 DTO와 도메인 기하 타입 사이의 승격·강등.
 *
 * <p><b>왜 별도 클래스인가</b>: 컨트롤러는 도메인 타입을 알 수 없고({@code controllersShouldBeDomainFree}),
 * Request/Response record도 도메인을 참조할 수 없다({@code requestResponseRecordsShouldBeDomainAndInfraFree}).
 * 그래서 승격은 서비스 계층이 해야 하는데, 도형을 다루는 서비스가 넷(도형 저장·도형 미리보기·반경 미리보기
 * ·반경 적용)이라 각자 private 매퍼를 두면 같은 변환이 네 벌로 복제된다.
 *
 * <p><b>챕터 09</b> — 강등 대상이 {@code GeoPointResponse}(표현 계약)에서 {@code GeoPointView}(조회 결과)로
 * 바뀌었다. api 모듈은 {@code domain.shared.geo..}를 알 수 없어 표현 계약이 도메인 기하 타입을 직접 받을
 * 수 없으므로, 이 매퍼가 좌표까지 풀어 넘기고 Response는 그것을 1:1로 옮긴다.
 *
 * <p>도형 형식 위반은 {@code SHOP_DELIVERY_AREA_POLYGON_INVALID}(400)로 번역한다 — 도메인 기하 타입의
 * 생성자가 던지는 {@link IllegalArgumentException}이 그대로 올라가면 500이 된다.
 */
final class ShopDeliveryAreaGeoMapper {

    private ShopDeliveryAreaGeoMapper() {
    }

    /** command의 링 배열을 도메인 도형으로 승격한다. */
    static GeoPolygon toPolygon(List<List<GeoPointCommand>> rings) {
        if (rings == null || rings.isEmpty()) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID);
        }

        try {
            return GeoPolygon.of(rings.stream()
                .map(ShopDeliveryAreaGeoMapper::toRing)
                .toList());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID,
                ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID.getDefaultMessage() + ": " + e.getMessage()
            );
        }
    }

    /** 도메인 도형을 조회 결과의 링 배열로 강등한다. */
    static List<List<GeoPointView>> toRingViews(GeoPolygon polygon) {
        return polygon.rings().stream()
            .map(ShopDeliveryAreaGeoMapper::toPointViews)
            .toList();
    }

    /** 링 하나를 좌표 목록으로 강등한다(반경 원 등 단일 링 표현용). */
    static List<GeoPointView> toPointViews(GeoRing ring) {
        return ring.points().stream()
            .map(ShopDeliveryAreaGeoMapper::toPointView)
            .toList();
    }

    private static GeoRing toRing(List<GeoPointCommand> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("링의 좌표 목록이 비어 있습니다.");
        }

        return GeoRing.of(points.stream()
            .map(point -> GeoPoint.of(point.latitude(), point.longitude()))
            .toList());
    }

    private static GeoPointView toPointView(GeoPoint point) {
        return new GeoPointView(point.latitude(), point.longitude());
    }
}
