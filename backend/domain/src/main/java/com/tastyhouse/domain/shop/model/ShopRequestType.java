package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 점주가 낸 "관리자 처리를 기다리는 요청"의 유형.
 *
 * <p>ceo-api 전수 조사 결과 이 성격을 갖는 신청은 2종(요청 유형 3개)뿐이며 나머지 mutation은 모두 즉시
 * 반영이다. 관리자 지시(라이더 가이드 수정요청)·손님 신청(예약)처럼 <b>방향이 반대</b>인 것과, 점주 접점이
 * 없는 것(제휴·버그 신고)은 이 enum에 담지 않는다.
 *
 * <p>{@code attachmentLabel}은 요청 첨부의 명칭이고, {@code contractAmending}은 그 요청이 승인되면
 * 전자계약서가 수정되는지다. 이 저장소에는 전자계약서 테이블이 없어 "계약서 사본 확인" 요구사항을
 * 이 두 값으로 일반화했다 — 계약 부속서류 유형이 실제로 생기면 상수 추가만으로 확장된다.
 */
public enum ShopRequestType {

    TRADEMARK_CHANGE("상표 변경 요청", "요청 상표 이미지", false),
    THUMBNAIL_CHANGE("대표이미지 변경 요청", "요청 대표이미지", false),
    DELIVERY_AREA_ADJUSTMENT("배달지역 조정 신청", "정보제공 동의서", true),
    REVIEW_BLIND("리뷰 게시중단 요청", "요청 사유", false),
    STORE_PRICE_VERIFICATION("매장 가격 인증 요청", "매장 가격표 이미지", false);

    private final String description;
    private final String attachmentLabel; // nullable — 첨부가 없는 유형이 생기면 null
    private final boolean contractAmending;

    ShopRequestType(String description, String attachmentLabel, boolean contractAmending) {
        this.description = description;
        this.attachmentLabel = attachmentLabel;
        this.contractAmending = contractAmending;
    }

    public static ShopRequestType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SHOP_REQUEST_TYPE_UNKNOWN,
                ErrorCode.SHOP_REQUEST_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }

    public String getDescription() {
        return this.description;
    }

    public String getAttachmentLabel() {
        return this.attachmentLabel;
    }

    public boolean isContractAmending() {
        return this.contractAmending;
    }
}
