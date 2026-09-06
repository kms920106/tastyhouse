package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 원산지 표시 입력 방식.
 *
 * <p>원산지는 가게 전체에 대해 한 번 작성하며, 점주가 직접 문장으로 적거나(프랜차이즈가 아닌 개인
 * 가게) 본사가 제공하는 페이지 URL을 걸어 둔다(프랜차이즈 가맹점). 두 방식은 <b>상호 배타</b>여서
 * {@code ShopOriginInfo}가 한쪽 필드만 보유하고 반대편은 null로 정리한다.
 *
 * <p><b>상수 이름 자체가 DB 저장값이다</b>({@code EnumType.STRING}) — 이름을 바꾸지 않는다.
 */
public enum OriginSourceType {

    /** 직접 입력 — 점주가 원산지 문장을 직접 작성한다. */
    DIRECT("직접 입력"),

    /** 본사 제공 URL — 가맹본사가 운영하는 원산지 안내 페이지를 연결한다. */
    FRANCHISE_URL("본사 제공 URL");

    private final String description;

    OriginSourceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static OriginSourceType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.SHOP_ORIGIN_SOURCE_TYPE_UNKNOWN,
                ErrorCode.SHOP_ORIGIN_SOURCE_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
