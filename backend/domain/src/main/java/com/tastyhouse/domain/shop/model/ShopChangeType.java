package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum ShopChangeType {
    BUSINESS_HOUR(ShopChangeCategory.OPERATION, "영업시간"),
    BREAK_TIME(ShopChangeCategory.OPERATION, "휴게시간"),
    HOLIDAY_CLOSURE(ShopChangeCategory.OPERATION, "공휴일 휴무 설정"),
    CLOSED_DAY(ShopChangeCategory.OPERATION, "휴무일"),
    TEMPORARY_CLOSURE(ShopChangeCategory.OPERATION, "임시휴무"),
    PHONE_NUMBER(ShopChangeCategory.OPERATION, "가게 전화번호"),
    REPRESENTATIVE_PHONE(ShopChangeCategory.OPERATION, "대표번호 지정"),
    SHOP_VISIBILITY(ShopChangeCategory.OPERATION, "가게 노출상태"),
    ORDER_SUSPENSION(ShopChangeCategory.OPERATION, "영업 임시중지"),

    DELIVERY_TIP_TIER(ShopChangeCategory.DELIVERY, "주문금액별 배달팁"),
    DELIVERY_TIP_DISTANCE(ShopChangeCategory.DELIVERY, "거리별 배달팁"),
    DELIVERY_TIP_REGION(ShopChangeCategory.DELIVERY, "지역 할증 배달팁"),
    DELIVERY_TIP_SCHEDULE(ShopChangeCategory.DELIVERY, "시간 할증 배달팁"),
    DELIVERY_TIP_HOLIDAY(ShopChangeCategory.DELIVERY, "공휴일 배달팁"),
    DELIVERY_AREA(ShopChangeCategory.DELIVERY, "배달가능지역"),
    DELIVERY_AREA_RADIUS(ShopChangeCategory.DELIVERY, "배달반경"),
    DELIVERY_AREA_POLYGON(ShopChangeCategory.DELIVERY, "배달영역(지도)"),
    DELIVERY_AREA_ADJUSTMENT(ShopChangeCategory.DELIVERY, "배달지역 조정 신청"),
    MIN_ORDER_AMOUNT(ShopChangeCategory.DELIVERY, "최소주문금액"),
    SCHEDULED_ORDER(ShopChangeCategory.DELIVERY, "예약주문 설정"),

    INTRODUCTION(ShopChangeCategory.SHOP_INFO, "사장님 한마디"),
    CONVENIENCE_INFO(ShopChangeCategory.SHOP_INFO, "편의정보 수정"),
    AMENITY(ShopChangeCategory.SHOP_INFO, "편의시설"),
    CONTENT_BOARD(ShopChangeCategory.SHOP_INFO, "가게 소식"),
    NOTICE(ShopChangeCategory.SHOP_INFO, "사장님 공지"),
    ORIGIN_INFO(ShopChangeCategory.SHOP_INFO, "원산지 표시"),

    TRADEMARK_CHANGE_REQUEST(ShopChangeCategory.IMAGE, "상표 변경요청"),
    THUMBNAIL_CHANGE_REQUEST(ShopChangeCategory.IMAGE, "대표이미지 변경요청"),

    RIDER_VISIT_GUIDE(ShopChangeCategory.RIDER, "라이더 방문안내"),
    RIDER_PICKUP_LOCATION(ShopChangeCategory.RIDER, "픽업 위치");

    private final ShopChangeCategory category;
    private final String description;

    ShopChangeType(ShopChangeCategory category, String description) {
        this.category = category;
        this.description = description;
    }

    public ShopChangeCategory getCategory() {
        return this.category;
    }

    public String getDescription() {
        return this.description;
    }

    public static ShopChangeType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SHOP_CHANGE_TYPE_UNKNOWN,
                ErrorCode.SHOP_CHANGE_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
