package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 알레르기 유발성분 (식품표시광고법이 표시를 요구하는 21종).
 *
 * <p>선언 순서는 법령·가이드가 열거하는 순서를 그대로 따른다 — 점주 화면의 체크박스 목록과 손님
 * 화면의 라벨 나열이 모두 이 순서를 쓰므로, 알파벳순으로 재배열하면 화면 순서가 법령 고지 순서와
 * 어긋난다.
 *
 * <p>{@code description}은 손님 화면에 그대로 노출되는 <b>한글 라벨</b>이다. 손님 응답이 코드가 아니라
 * 라벨 배열을 내려주는 이유는 화면이 코드→라벨 매핑표를 따로 들지 않게 하려는 것이다(매핑표가
 * 화면에 있으면 성분이 추가될 때 화면 배포가 필요해진다).
 *
 * <p><b>상수 이름 자체가 DB 저장값이다</b>({@code EnumType.STRING}) — 이름을 바꾸지 않는다.
 */
public enum AllergenType {

    MILK("우유"),
    EGG("난류"),
    BUCKWHEAT("메밀"),
    PEANUT("땅콩"),
    SOYBEAN("대두"),
    WHEAT("밀"),
    WALNUT("호두"),
    PEACH("복숭아"),
    TOMATO("토마토"),
    MACKEREL("고등어"),
    CRAB("게"),
    SHRIMP("새우"),
    SQUID("오징어"),
    OYSTER("굴"),
    ABALONE("전복"),
    MUSSEL("홍합"),
    SHELLFISH("조개류"),
    PORK("돼지고기"),
    CHICKEN("닭고기"),
    BEEF("쇠고기"),
    SULFITE("아황산류");

    private final String description;

    AllergenType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static AllergenType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.PRODUCT_ALLERGEN_TYPE_UNKNOWN,
                ErrorCode.PRODUCT_ALLERGEN_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
