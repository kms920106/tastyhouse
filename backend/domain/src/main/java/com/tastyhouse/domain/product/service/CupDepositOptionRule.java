package com.tastyhouse.domain.product.service;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;

/**
 * 일회용컵 보증금 옵션·옵션그룹의 <b>설정 규칙</b> 단일 진실원.
 *
 * <p>보증금은 결제 금액에 직접 들어가고 비과세·정산 제외 항목으로 분리 저장되므로, "어떤 옵션이
 * 보증금인가"의 판정이 흔들리면 금액이 조용히 틀어진다. 그래서 규칙을 도메인 한 곳에 모으고
 * ceo·admin 두 진입 경로가 같은 판정을 호출하게 한다.
 *
 * <p>상태도 협력자도 없는 순수 정적 유틸이다({@link ProductOptionSelectionRule}과 같은 형태).
 */
public final class CupDepositOptionRule {

    /** 보증금 옵션그룹은 항상 이 선택 제약을 갖는다 — 서버가 강제하며 클라이언트 값은 대조만 한다. */
    public static final int DEPOSIT_MIN_SELECT = 0;
    public static final int DEPOSIT_MAX_SELECT = 1;

    private CupDepositOptionRule() {
    }

    /**
     * 보증금 옵션그룹의 선택 제약을 검증한다.
     *
     * <p><b>필수 선택 불가</b>: 강제하면 개인컵을 가져온 손님이 주문 자체를 할 수 없다.
     *
     * <p><b>선택 개수는 {@code minSelect=0, maxSelect=1, multipleSelect=false}로 고정</b>이다. 클라이언트가
     * 다른 값을 보내면 <b>무시하지 않고 거부</b>한다 — 조용히 덮어쓰면 점주는 자기가 설정한 값이 저장된
     * 줄 알고 화면에서 다른 값을 보게 된다.
     */
    public static void validateDepositGroupSelectRange(
        ProductOptionGroupType groupType,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect
    ) {
        if (groupType == null || !groupType.isCupDeposit()) {
            return;
        }

        if (required) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_DEPOSIT_CANNOT_BE_REQUIRED);
        }
        boolean fixedRange = !multipleSelect
            && minSelect != null && minSelect == DEPOSIT_MIN_SELECT
            && maxSelect != null && maxSelect == DEPOSIT_MAX_SELECT;
        if (!fixedRange) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_DEPOSIT_SELECT_FIXED);
        }
    }

    /**
     * 옵션의 보증금 관련 값이 소속 그룹의 유형과 맞는지 검증한다.
     *
     * <p>보증금 그룹의 옵션은 두 종류뿐이다.
     * <ul>
     *   <li><b>보증금 옵션</b> — {@code cupCount}가 있고 {@code additionalPrice}는 0이다. 추가금과 섞으면
     *       비과세 분리가 무너져 "이 주문의 보증금이 얼마였나"를 사후에 되짚을 수 없다.</li>
     *   <li><b>개인컵 옵션</b> — 컵을 주지 않으므로 {@code cupCount}가 없고, 대신
     *       {@code personalCupDiscountAmount}를 갖는다(보증금이 아니라 <b>상품 할인 축</b>).</li>
     * </ul>
     *
     * <p>일반 그룹의 옵션은 두 값을 모두 가질 수 없다.
     */
    public static void validateOptionValues(
        ProductOptionGroup group,
        Integer additionalPrice,
        Integer cupCount,
        Integer personalCupDiscountAmount,
        CupDepositPolicy cupDepositPolicy
    ) {
        boolean personalCup = personalCupDiscountAmount != null && personalCupDiscountAmount > 0;

        if (!group.isCupDeposit()) {
            if (cupCount != null) {
                throw new BusinessException(ErrorCode.PRODUCT_OPTION_CUP_COUNT_NOT_ALLOWED);
            }
            if (personalCup) {
                throw new BusinessException(ErrorCode.PRODUCT_OPTION_PERSONAL_CUP_NOT_IN_DEPOSIT_GROUP);
            }
            return;
        }

        if (additionalPrice != null && additionalPrice != 0) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_DEPOSIT_ADDITIONAL_PRICE_NOT_ALLOWED);
        }

        // 개인컵 옵션은 컵을 주지 않으므로 컵 개수가 없는 것이 정상이다.
        if (personalCup) {
            if (cupCount != null) {
                throw new BusinessException(ErrorCode.PRODUCT_OPTION_CUP_COUNT_NOT_ALLOWED);
            }
            return;
        }

        if (cupCount == null) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_CUP_COUNT_REQUIRED);
        }
        cupDepositPolicy.validateCupCount(cupCount);
    }
}
