package com.tastyhouse.adminapplication.event.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 당첨자 발표 공지 수정 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand(id)}로 주입한다.
 */
public record EventAnnouncementUpdateCommand(
    Long eventId,
    String name,
    String content,
    LocalDateTime announcedAt
) {
    public EventAnnouncementUpdateCommand {
        if (eventId == null || name == null || content == null || announcedAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
