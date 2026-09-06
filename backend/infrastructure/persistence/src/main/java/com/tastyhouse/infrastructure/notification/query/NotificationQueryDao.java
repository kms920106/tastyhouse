package com.tastyhouse.infrastructure.notification.query;

import com.tastyhouse.application.notification.port.out.NotificationQueryPort;
import com.tastyhouse.application.notification.port.out.NotificationListItemResult;
import com.querydsl.core.types.Projections;
import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import static com.tastyhouse.infrastructure.notification.persistence.QNotificationJpaEntity.notificationJpaEntity;

@Repository
public class NotificationQueryDao implements NotificationQueryPort {
    private final JPAQueryFactory queryFactory;

    public NotificationQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public PageResult<NotificationListItemResult> findNotificationsByMemberId(Long memberId, PageQuery pageQuery) {
        Long total = queryFactory
            .select(notificationJpaEntity.count())
            .from(notificationJpaEntity)
            .where(notificationJpaEntity.memberId.eq(memberId))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<NotificationListItemResult> content = queryFactory
            .select(Projections.constructor(NotificationListItemResult.class,
                notificationJpaEntity.id,
                notificationJpaEntity.type,
                notificationJpaEntity.title,
                notificationJpaEntity.body,
                notificationJpaEntity.targetType,
                notificationJpaEntity.targetId,
                notificationJpaEntity.read,
                notificationJpaEntity.createdAt
            ))
            .from(notificationJpaEntity)
            .where(notificationJpaEntity.memberId.eq(memberId))
            .orderBy(notificationJpaEntity.createdAt.desc(), notificationJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public long countUnreadByMemberId(Long memberId) {
        Long count = queryFactory
            .select(notificationJpaEntity.count())
            .from(notificationJpaEntity)
            .where(
                notificationJpaEntity.memberId.eq(memberId),
                notificationJpaEntity.read.isFalse()
            )
            .fetchOne();

        return count == null ? 0L : count;
    }
}
