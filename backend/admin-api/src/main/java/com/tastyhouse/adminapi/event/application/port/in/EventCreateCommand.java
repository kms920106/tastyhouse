package com.tastyhouse.adminapi.event.application.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 이벤트 등록 command.
 *
 * <p>{@code thumbnailImageFileId}·{@code bannerImageFileId}는 둘 다 {@code Long}이라 순서가 뒤바뀌어도
 * 컴파일되고, {@code startAt}·{@code endAt}도 마찬가지다. 조립은 반드시 이름 있는 접근자로 한다.
 */
public record EventCreateCommand(
    String name,
    String description,
    String subtitle,
    Long thumbnailImageFileId,
    Long bannerImageFileId,
    String contentHtml,
    String status,
    LocalDateTime startAt,
    LocalDateTime endAt
) {
    public EventCreateCommand {
        if (name == null || status == null || startAt == null || endAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
