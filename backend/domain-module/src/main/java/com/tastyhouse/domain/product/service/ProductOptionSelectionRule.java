package com.tastyhouse.domain.product.service;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;

/**
 * 옵션그룹의 선택 제약(잔여 개수·0원 옵션)을 판정하는 <b>단일 진실원</b>.
 *
 * <p><b>왜 도메인에 두는가</b>: 같은 제약을 두 경로가 각자 구현하고 있었고 하한이 서로 달랐다 —
 * 일괄 숨김({@code ProductAvailabilityService})은 {@code minSelect}가 0·null이어도 1개를 남겼지만,
 * 개별 삭제({@code ceo-api}의 {@code ProductOptionCommandService})는 {@code minSelect <= 0}이면
 * 검사를 통째로 건너뛰어 <b>옵션이 0개인 옵션그룹</b>을 만들 수 있었다. 그런 그룹이 붙은 메뉴는
 * 주문 자체가 불가능해진다. 판정을 여기 한 곳에 모아 두 경로가 같은 식을 쓰게 한다.
 *
 * <p>{@code @Service} 없는 순수 정적 유틸이다 — 상태도 협력자도 없어 빈으로 만들 이유가 없다.
 */
public final class ProductOptionSelectionRule {

    private ProductOptionSelectionRule() {
    }

    /**
     * 이 그룹에 <b>남겨야 하는 판매중 옵션의 최소 개수</b>.
     *
     * <pre>max(minSelect ?: 0, maxSelect ?: 0, 1)</pre>
     *
     * <p>{@code maxSelect}가 하한에 들어가는 이유: 손님이 최대 N개를 고를 수 있다고 약속해 놓고
     * 고를 수 있는 옵션이 N개 미만이면 그 약속이 지켜지지 않는다. 마지막 항 {@code 1}은
     * "옵션이 하나도 없는 그룹"을 어떤 설정에서도 만들지 못하게 하는 절대 하한이다.
     */
    public static int minRemaining(Integer minSelect, Integer maxSelect) {
        int min = minSelect != null ? minSelect : 0;
        int max = maxSelect != null ? maxSelect : 0;
        return Math.max(Math.max(min, max), 1);
    }

    /** {@link #minRemaining(Integer, Integer)}와 같되 그룹을 통째로 받는다. */
    public static int minRemaining(ProductOptionGroup group) {
        return group == null ? 1 : minRemaining(group.getMinSelect(), group.getMaxSelect());
    }

    /** 선택 가능 = 품절 아님 + 노출 중. 손님이 지금 고를 수 있는 옵션의 정의다. */
    public static boolean selectable(ProductOption option) {
        return !option.isSoldOut() && option.isVisible();
    }

    /**
     * 그룹에서 {@code target} 하나를 선택 불가로 만든 뒤에도 하한을 채울 수 있는지 검증한다.
     *
     * <p>대상이 이미 선택 불가 상태면 개수가 줄지 않으므로 통과시킨다(멱등).
     *
     * @param groupOptions 그룹의 <b>전체</b> 옵션(대상 포함)
     */
    public static void validateRemainingAfterBlocking(
        ProductOptionGroup group,
        ProductOption target,
        List<ProductOption> groupOptions
    ) {
        if (!selectable(target)) {
            return;
        }

        long remaining = groupOptions.stream()
            .filter(option -> !option.getId().equals(target.getId()))
            .filter(ProductOptionSelectionRule::selectable)
            .count();

        int required = minRemaining(group);
        if (remaining < required) {
            // 하한을 끌어올린 주체가 minSelect인지 maxSelect인지에 따라 사유를 나눈다 — 점주에게
            // "최소 선택 개수 때문"과 "최대 선택 개수 때문"은 고칠 방법이 다른 별개의 안내다.
            throw new BusinessException(violationCodeOf(group, remaining));
        }
    }

    /**
     * 필수 옵션그룹은 <b>0원 옵션을 1개 이상</b> 포함해야 한다.
     *
     * <p>근거: 전자상거래법의 순차공개 가격책정 금지 — 필수 그룹의 모든 옵션에 추가금이 붙으면
     * 손님이 메뉴판에서 본 가격으로는 그 메뉴를 살 수 없다.
     *
     * <p>{@code required=false}면 통과한다. 옵션이 0건인 그룹도 통과시킨다 — 그룹은 옵션보다 먼저
     * 만들어지므로 등록 직후 상태를 이 규칙으로 막으면 옵션그룹을 아예 만들 수 없다.
     *
     * @param options 판정 대상 옵션 목록(변경이 <b>적용된 뒤</b>의 상태여야 한다)
     */
    public static void validateZeroPriceOption(ProductOptionGroup group, List<ProductOption> options) {
        if (!group.isRequired() || options.isEmpty()) {
            return;
        }

        boolean hasZeroPrice = options.stream()
            .filter(ProductOptionSelectionRule::selectable)
            .anyMatch(option -> option.getAdditionalPrice() == null || option.getAdditionalPrice() == 0);
        if (!hasZeroPrice) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_REQUIRES_ZERO_PRICE_OPTION);
        }
    }

    /**
     * 하한 위반의 사유 코드. {@code minSelect}만으로도 이미 위반이면 기존 코드를 유지해
     * 프론트가 분기하던 wire 계약을 바꾸지 않고, {@code maxSelect} 때문에 새로 걸리는 경우에만
     * 신규 코드를 쓴다.
     */
    private static ErrorCode violationCodeOf(ProductOptionGroup group, long remaining) {
        int minSelect = group != null && group.getMinSelect() != null ? group.getMinSelect() : 0;
        if (remaining < Math.max(minSelect, 1)) {
            return ErrorCode.PRODUCT_OPTION_MIN_SELECT_VIOLATION;
        }
        return ErrorCode.PRODUCT_OPTION_MAX_SELECT_VIOLATION;
    }
}
