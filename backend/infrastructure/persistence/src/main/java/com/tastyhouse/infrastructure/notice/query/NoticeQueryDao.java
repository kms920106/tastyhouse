package com.tastyhouse.infrastructure.notice.query;

import com.tastyhouse.application.notice.port.out.NoticeDetailResult;
import com.tastyhouse.application.notice.port.out.NoticeManagementQueryPort;
import com.tastyhouse.application.notice.port.out.NoticeQueryPort;
import com.tastyhouse.application.notice.port.out.NoticeListItemResult;
import com.tastyhouse.application.notice.port.out.NoticeManagementListItemResult;
import com.tastyhouse.application.notice.port.out.NoticeSearchCondition;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.tastyhouse.infrastructure.notice.persistence.QNoticeJpaEntity.noticeJpaEntity;

@Repository
public class NoticeQueryDao implements NoticeQueryPort, NoticeManagementQueryPort {
    private final JPAQueryFactory queryFactory;

    public NoticeQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public PageResult<NoticeManagementListItemResult> findAllNotices(NoticeSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(noticeJpaEntity.id.count())
            .from(noticeJpaEntity)
            .where(
                titleContains(condition.title()),
                contentContains(condition.content()),
                visibleEq(condition.visible()),
                noticeJpaEntity.deleted.isFalse()
            )
            .fetchOne();

        List<NoticeManagementListItemResult> notices = queryFactory
            .select(Projections.constructor(NoticeManagementListItemResult.class,
                noticeJpaEntity.id,
                noticeJpaEntity.title,
                noticeJpaEntity.content,
                noticeJpaEntity.visible,
                noticeJpaEntity.createdAt
            ))
            .from(noticeJpaEntity)
            .where(
                titleContains(condition.title()),
                contentContains(condition.content()),
                visibleEq(condition.visible()),
                noticeJpaEntity.deleted.isFalse()
            )
            .orderBy(noticeJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(notices, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<NoticeDetailResult> findDetailById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        NoticeDetailResult detail = queryFactory
            .select(Projections.constructor(NoticeDetailResult.class,
                noticeJpaEntity.id,
                noticeJpaEntity.title,
                noticeJpaEntity.content,
                noticeJpaEntity.visible,
                noticeJpaEntity.createdAt,
                noticeJpaEntity.updatedAt
            ))
            .from(noticeJpaEntity)
            .where(noticeJpaEntity.id.eq(id), noticeJpaEntity.deleted.isFalse())
            .fetchOne();

        return Optional.ofNullable(detail);
    }

    @Override
    public PageResult<NoticeListItemResult> findVisibleNotices(PageQuery pageQuery) {
        Long total = queryFactory
            .select(noticeJpaEntity.id.count())
            .from(noticeJpaEntity)
            .where(noticeJpaEntity.deleted.isFalse(), noticeJpaEntity.visible.isTrue())
            .fetchOne();

        List<NoticeListItemResult> notices = queryFactory
            .select(Projections.constructor(NoticeListItemResult.class,
                noticeJpaEntity.id,
                noticeJpaEntity.title,
                noticeJpaEntity.content,
                noticeJpaEntity.createdAt
            ))
            .from(noticeJpaEntity)
            .where(noticeJpaEntity.deleted.isFalse(), noticeJpaEntity.visible.isTrue())
            .orderBy(noticeJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(notices, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    private BooleanExpression titleContains(String title) {
        return StringUtils.hasText(title) ? noticeJpaEntity.title.containsIgnoreCase(title) : null;
    }

    private BooleanExpression contentContains(String content) {
        return StringUtils.hasText(content) ? noticeJpaEntity.content.containsIgnoreCase(content) : null;
    }

    private BooleanExpression visibleEq(Boolean visible) {
        return visible != null ? noticeJpaEntity.visible.eq(visible) : null;
    }
}
