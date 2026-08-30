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

/**
 * 공지사항 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로
 * write 포트({@code NoticeRepository})와 역할이 겹치지 않는다. 소비 모듈(web-api/admin-api)의
 * {@code NoticeQueryService}는 이 DAO가 아니라 그 계약인 {@link NoticeQueryPort}를 주입하므로,
 * api 모듈은 QueryDSL도 이 어댑터의 존재도 알지 않는다.
 *
 * <p>도메인당 DAO 1개 원칙에 따라 소비자별 메서드를 이 한 클래스에 둔다. 메서드명에는 admin 마커를
 * 붙이지 않고 순수 동작명을 쓴다({@code findAllNotices}는 비노출 포함 전체, {@code findVisibleNotices}는
 * 노출분만).
 */
@Repository
public class NoticeQueryDao implements NoticeQueryPort, NoticeManagementQueryPort {

    private final JPAQueryFactory queryFactory;

    public NoticeQueryDao(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 관리 목록 조회 — 비노출 공지를 포함하며 title/content 부분일치·visible 필터를 적용한다.
     */
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

    /**
     * 관리 상세 조회 — 비노출 공지도 조회된다.
     */
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

    /**
     * 회원 노출 목록 조회 — 노출(visible=true) 공지만 조회한다.
     */
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
