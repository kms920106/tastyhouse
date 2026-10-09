package com.tastyhouse.application.notice.port.out.write;

import com.tastyhouse.domain.notice.model.Notice;

public interface NoticeSavePort {

    Notice save(Notice notice);
}
