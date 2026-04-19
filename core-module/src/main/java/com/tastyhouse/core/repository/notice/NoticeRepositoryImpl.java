package com.tastyhouse.core.repository.notice;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.notice.dto.NoticeListItemDto;
import com.tastyhouse.core.entity.notice.dto.QNoticeListItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tastyhouse.core.entity.notice.QNotice.notice;

@Repository
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<NoticeListItemDto> findAllWithFilter(Pageable pageable) {
        Long total = queryFactory
            .select(notice.id.count())
            .from(notice)
            .where(notice.active.isTrue())
            .fetchOne();

        List<NoticeListItemDto> notices = queryFactory
            .select(new QNoticeListItemDto(
                notice.id,
                notice.title,
                notice.content,
                notice.createdAt
            ))
            .from(notice)
            .where(notice.active.isTrue())
            .orderBy(notice.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return new PageImpl<>(notices, pageable, total != null ? total : 0L);
    }
}
