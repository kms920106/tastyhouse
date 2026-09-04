package com.tastyhouse.adminapplication.event.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 이벤트 수정 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand(id)}로 주입한다.
 *
 * <p>{@code eventId}·{@code thumbnailImageFileId}·{@code bannerImageFileId}가 모두 {@code Long}이라
 * 순서가 뒤바뀌어도 컴파일된다. 조립은 반드시 이름 있는 접근자로 한다.
 */
public record EventUpdateCommand(
    Long eventId,
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
    public EventUpdateCommand {
        if (eventId == null || name == null || status == null || startAt == null || endAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
