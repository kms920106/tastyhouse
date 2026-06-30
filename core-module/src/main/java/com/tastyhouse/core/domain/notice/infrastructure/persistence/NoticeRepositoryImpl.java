package com.tastyhouse.core.domain.notice.infrastructure.persistence;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.notice.application.dto.NoticeListItemDto;
import com.tastyhouse.core.domain.notice.application.dto.NoticeSearchCondition;
import com.tastyhouse.core.domain.notice.application.dto.QNoticeListItemDto;
import com.tastyhouse.core.domain.notice.domain.model.Notice;
import com.tastyhouse.core.domain.notice.domain.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.tastyhouse.core.domain.notice.domain.model.QNotice.notice;

@Repository
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepository {

    private final JPAQueryFactory queryFactory;
    private final NoticeJpaRepository noticeJpaRepository;

    @Override
    public Page<NoticeListItemDto> findVisibleNotices(Pageable pageable) {
        Long total = queryFactory
            .select(notice.id.count())
            .from(notice)
            .where(notice.deleted.isFalse(), notice.visible.isTrue())
            .fetchOne();

        List<NoticeListItemDto> notices = queryFactory
            .select(new QNoticeListItemDto(
                notice.id,
                notice.title,
                notice.content,
                notice.visible,
                notice.createdAt
            ))
            .from(notice)
            .where(notice.deleted.isFalse(), notice.visible.isTrue())
            .orderBy(notice.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return new PageImpl<>(notices, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<NoticeListItemDto> findAllNotices(NoticeSearchCondition condition, Pageable pageable) {
        Long total = queryFactory
            .select(notice.id.count())
            .from(notice)
            .where(
                titleContains(condition.title()),
                contentContains(condition.content()),
                visibleEq(condition.visible()),
                notice.deleted.isFalse()
            )
            .fetchOne();

        List<NoticeListItemDto> notices = queryFactory
            .select(new QNoticeListItemDto(
                notice.id,
                notice.title,
                notice.content,
                notice.visible,
                notice.createdAt
            ))
            .from(notice)
            .where(
                titleContains(condition.title()),
                contentContains(condition.content()),
                visibleEq(condition.visible()),
                notice.deleted.isFalse()
            )
            .orderBy(notice.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return new PageImpl<>(notices, pageable, total != null ? total : 0L);
    }

    @Override
    public Optional<Notice> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(queryFactory
            .selectFrom(notice)
            .where(notice.id.eq(id), notice.deleted.isFalse())
            .fetchOne());
    }

    @Override
    public Notice save(Notice notice) {
        return noticeJpaRepository.save(notice);
    }

    private BooleanExpression titleContains(String title) {
        return StringUtils.hasText(title) ? notice.title.containsIgnoreCase(title) : null;
    }

    private BooleanExpression contentContains(String content) {
        return StringUtils.hasText(content) ? notice.content.containsIgnoreCase(content) : null;
    }

    private BooleanExpression visibleEq(Boolean visible) {
        return visible != null ? notice.visible.eq(visible) : null;
    }
}
