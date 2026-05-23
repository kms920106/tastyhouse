package com.tastyhouse.core.domain.event.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.event.domain.model.EventAnnouncement;
import com.tastyhouse.core.domain.event.domain.repository.EventAnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tastyhouse.core.domain.event.domain.model.QEventAnnouncement.eventAnnouncement;

@Repository
@RequiredArgsConstructor
public class EventAnnouncementRepositoryImpl implements EventAnnouncementRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<EventAnnouncement> findAllOrderByAnnouncedAtDesc(Pageable pageable) {
        List<EventAnnouncement> content = queryFactory
            .selectFrom(eventAnnouncement)
            .orderBy(eventAnnouncement.announcedAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(eventAnnouncement.count())
            .from(eventAnnouncement);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
