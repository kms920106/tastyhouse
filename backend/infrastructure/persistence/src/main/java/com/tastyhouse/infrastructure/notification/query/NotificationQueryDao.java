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

/**
 * 알림함 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로 write
 * 포트({@code NotificationRepository})와 역할이 겹치지 않는다.
 */
@Repository
public class NotificationQueryDao implements NotificationQueryPort {

    private final JPAQueryFactory queryFactory;

    public NotificationQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 회원의 알림 목록 — 최신순.
     */
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

    /**
     * 회원의 미읽음 알림 개수 — 헤더 배지용.
     */
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
