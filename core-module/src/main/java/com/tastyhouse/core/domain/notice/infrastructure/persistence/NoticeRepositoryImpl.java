package com.tastyhouse.core.domain.notice.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.notice.application.dto.NoticeListItemDto;
import com.tastyhouse.core.domain.notice.application.dto.QNoticeListItemDto;
import com.tastyhouse.core.domain.notice.domain.model.Notice;
import com.tastyhouse.core.domain.notice.domain.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.tastyhouse.core.domain.notice.domain.model.QNotice.notice;

@Repository
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepository {

    private final JPAQueryFactory queryFactory;
    private final NoticeJpaRepository noticeJpaRepository;

    @Override
    public Page<NoticeListItemDto> findAllWithFilter(Pageable pageable) {
        Long total = queryFactory
            .select(notice.id.count())
            .from(notice)
            .where(notice.visible.isTrue())
            .fetchOne();

        List<NoticeListItemDto> notices = queryFactory
            .select(new QNoticeListItemDto(
                notice.id,
                notice.title,
                notice.content,
                notice.createdAt
            ))
            .from(notice)
            .where(notice.visible.isTrue())
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
        return noticeJpaRepository.findById(id);
    }

    @Override
    public Notice save(Notice notice) {
        return noticeJpaRepository.save(notice);
    }

    @Override
    public void deleteById(Long id) {
        noticeJpaRepository.deleteById(id);
    }
}
