package com.tastyhouse.core.domain.event.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.event.domain.model.EventAnnouncement;
import com.tastyhouse.core.domain.event.domain.repository.EventAnnouncementRepository;
import com.tastyhouse.core.domain.event.domain.vo.EventId;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.event.domain.model.QEventAnnouncement.eventAnnouncement;

@Repository
@RequiredArgsConstructor
public class EventAnnouncementRepositoryImpl implements EventAnnouncementRepository {

    private final JPAQueryFactory queryFactory;
    private final EventAnnouncementJpaRepository eventAnnouncementJpaRepository;

    @Override
    public PageResult<EventAnnouncement> findAllOrderByAnnouncedAtDesc(PageQuery pageQuery) {
        List<EventAnnouncement> content = queryFactory
            .selectFrom(eventAnnouncement)
            .orderBy(eventAnnouncement.announcedAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(eventAnnouncement.count())
            .from(eventAnnouncement);

        Long total = countQuery.fetchOne();
        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<EventAnnouncement> findByEventId(EventId eventId) {
        return Optional.ofNullable(queryFactory
            .selectFrom(eventAnnouncement)
            .where(eventAnnouncement.eventId.eq(eventId.value()))
            .fetchOne());
    }

    @Override
    public boolean existsByEventId(EventId eventId) {
        Integer result = queryFactory
            .selectOne()
            .from(eventAnnouncement)
            .where(eventAnnouncement.eventId.eq(eventId.value()))
            .fetchFirst();
        return result != null;
    }

    @Override
    public EventAnnouncement save(EventAnnouncement newEventAnnouncement) {
        return eventAnnouncementJpaRepository.save(newEventAnnouncement);
    }
}
