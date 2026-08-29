package com.tastyhouse.ceoapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 원산지 표시 등록·수정 command(전체 교체).
 *
 * <p>{@code content}·{@code url}은 입력 방식({@code sourceType})에 따라 한쪽만 채워지므로 null 가드를
 * 걸지 않는다 — 필수 여부는 Request의 jakarta.validation과 도메인 서비스가 판정한다.
 */
public record ShopOriginInfoUpdateCommand(
    Long ceoId,
    Long shopId,
    String sourceType,
    String content,
    String url
) {
    public ShopOriginInfoUpdateCommand {
        if (ceoId == null || shopId == null || sourceType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
