package com.tastyhouse.application.notice.port.in;

public interface NoticeCreateUseCase {

    Long createNotice(NoticeCreateCommand command);
}
