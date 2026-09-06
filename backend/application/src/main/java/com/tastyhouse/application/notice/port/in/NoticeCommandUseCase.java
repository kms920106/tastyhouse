package com.tastyhouse.application.notice.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface NoticeCommandUseCase {

    Long createNotice(NoticeCreateCommand command);

    void updateNotice(NoticeUpdateCommand command);

    void deleteNotice(NoticeDeleteCommand command);
}
