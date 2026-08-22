package com.tastyhouse.domain.product.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 옵션그룹 합치기 진입 경로.
 *
 * <p>{@code RECOMMENDED}는 서버가 동일성 서명으로 묶어 제안한 목록에서, {@code MANUAL}은 점주가
 * 직접 그룹을 골라 들어온 경로다. 이력에 남겨 두는 이유는 <b>합치기가 분리 불가</b>이기 때문이다 —
 * "추천을 그대로 눌렀다"와 "직접 골랐다"는 사후 문의 응대에서 성격이 다른 사실이다.
 */
public enum ProductOptionGroupMergeEntryType {

    RECOMMENDED,
    MANUAL;

    public static ProductOptionGroupMergeEntryType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_ENTRY_TYPE_UNKNOWN,
                ErrorCode.PRODUCT_OPTION_GROUP_MERGE_ENTRY_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
