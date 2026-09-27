package com.tastyhouse.application.notice.port.out.write;

import java.util.Optional;

public interface NoticeStatePort {
    Optional<NoticeState> findById(Long id);

    NoticeState save(NoticeState state);
}
