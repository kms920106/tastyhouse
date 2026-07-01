package com.tastyhouse.core.domain.event.domain.repository;

import com.tastyhouse.core.domain.event.domain.model.EventAnnouncement;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface EventAnnouncementRepository {

    PageResult<EventAnnouncement> findAllOrderByAnnouncedAtDesc(PageQuery pageQuery);
}
