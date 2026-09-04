package com.tastyhouse.application.shop.port.in;

import java.time.LocalDate;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 위생 인증 뱃지 등록 command.
 *
 * <p>{@code badgeType}은 경계에서 문자열로 받고 서비스가 {@code HygieneBadgeType}으로 승격한다.
 */
public record ShopHygieneBadgeCreateCommand(
    Long shopId,
    String badgeType,
    LocalDate certifiedDate,
    String lastInspectionMonth
) {
    public ShopHygieneBadgeCreateCommand {
        if (shopId == null || badgeType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
