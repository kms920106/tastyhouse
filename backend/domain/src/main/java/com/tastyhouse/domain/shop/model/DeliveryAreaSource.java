package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 배달가능지역 행의 등록 출처.
 *
 * <p>배달지역을 지도 도형으로 그리는 기능이 생기면서, 같은 {@code SHOP_DELIVERY_AREA} 테이블에 성격이
 * 다른 두 종류의 행이 섞이게 됐다. 폴리곤을 다시 저장하면 <b>도형에서 파생된 행만</b> 지우고 다시 넣어야
 * 하는데, 출처를 구분하지 않으면 점주가 손으로 추가한 행까지 함께 날아간다.
 *
 * <ul>
 *   <li>{@code MANUAL} — 행정동을 직접 선택했거나 반경으로 일괄 추가한 행. 폴리곤 저장이 건드리지 않는다.
 *   <li>{@code POLYGON} — 저장된 도형을 환산해 파생된 행. 폴리곤 저장 시 전량 삭제 후 재삽입된다.
 * </ul>
 *
 * <p>{@code uk_shop_delivery_area (shop_id, admin_dong_id)}가 있어 같은 동이 두 출처로 이중 등록될 수
 * 없다. 환산 결과에 이미 {@code MANUAL}로 등록된 동이 있으면 <b>건너뛴다</b> — 출처를 덮어쓰면 폴리곤을
 * 지웠을 때 점주가 직접 넣은 동까지 사라진다.
 */
public enum DeliveryAreaSource {

    /** 행정동 직접 선택·반경 일괄 적용. */
    MANUAL,

    /** 지도 도형 환산으로 파생. */
    POLYGON;

    /**
     * 저장된 문자열을 enum으로 승격한다. 알 수 없는 값은 {@code SHOP_DELIVERY_AREA_POLYGON_INVALID}로
     * 막는다 — 컬럼이 네이티브 {@code ENUM}이 아니라 {@code VARCHAR(20)}이라 DB가 값을 강제하지 않으므로,
     * 승격 지점에서 검증한다.
     */
    public static DeliveryAreaSource from(String value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID);
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID);
        }
    }
}
