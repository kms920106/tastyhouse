package com.tastyhouse.application.notification.port.out.write;

import java.util.List;
import java.util.Optional;

public interface NotificationStatePort {
    Optional<NotificationState> findById(Long id);

    List<NotificationState> findUnreadByMemberId(Long memberId);

    NotificationState save(NotificationState state);

    List<NotificationState> saveAll(List<NotificationState> states);
}
