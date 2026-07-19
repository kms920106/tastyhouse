package com.tastyhouse.infrastructure.notice.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.core.domain.notice.domain.model.Notice;
import com.tastyhouse.core.domain.notice.domain.repository.NoticeRepository;
import com.tastyhouse.core.domain.notice.domain.vo.NoticeId;
import com.tastyhouse.core.domain.notice.application.dto.NoticeSearchCondition;
import com.tastyhouse.core.domain.notice.application.dto.result.NoticeListItemResult;
import com.tastyhouse.core.domain.notice.application.dto.result.QNoticeListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.infrastructure.notice.persistence.QNoticeJpaEntity.noticeJpaEntity;

@Repository
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepository {

    private final JPAQueryFactory queryFactory;
    private final NoticeJpaRepository noticeJpaRepository;

    @Override
    public PageResult<NoticeListItemResult> findVisibleNotices(PageQuery pageQuery) {
        Long total = queryFactory
            .select(noticeJpaEntity.id.count())
            .from(noticeJpaEntity)
            .where(noticeJpaEntity.deleted.isFalse(), noticeJpaEntity.visible.isTrue())
            .fetchOne();

        List<NoticeListItemResult> notices = queryFactory
            .select(new QNoticeListItemResult(
                noticeJpaEntity.id,
                noticeJpaEntity.title,
                noticeJpaEntity.content,
                noticeJpaEntity.visible,
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

    @Override
    public PageResult<NoticeListItemResult> findAllNotices(NoticeSearchCondition condition, PageQuery pageQuery) {
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

        List<NoticeListItemResult> notices = queryFactory
            .select(new QNoticeListItemResult(
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
    public Optional<Notice> findById(NoticeId noticeId) {
        if (noticeId == null) {
            return Optional.empty();
        }
        NoticeJpaEntity entity = queryFactory
            .selectFrom(noticeJpaEntity)
            .where(noticeJpaEntity.id.eq(noticeId.value()), noticeJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(NoticeMapper::toDomain);
    }

    @Override
    public Notice save(Notice notice) {
        if (notice.getId() == null) {
            NoticeJpaEntity saved = noticeJpaRepository.save(NoticeMapper.toEntity(notice));
            return NoticeMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        NoticeJpaEntity entity = noticeJpaRepository.findById(notice.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 공지사항입니다: " + notice.getId()));
        NoticeMapper.applyChanges(entity, notice);
        return NoticeMapper.toDomain(entity);
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
