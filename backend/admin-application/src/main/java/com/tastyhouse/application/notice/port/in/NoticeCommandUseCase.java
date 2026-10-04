package com.tastyhouse.application.notice.port.in;

public interface NoticeCommandUseCase {

    Long createNotice(NoticeCreateCommand command);

    void updateNotice(NoticeUpdateCommand command);

    void deleteNotice(NoticeDeleteCommand command);
}
