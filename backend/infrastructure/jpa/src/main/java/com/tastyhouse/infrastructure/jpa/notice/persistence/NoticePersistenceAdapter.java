package com.tastyhouse.infrastructure.jpa.notice.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.notice.model.Notice;
import com.tastyhouse.domain.notice.vo.NoticeId;
import com.tastyhouse.application.notice.port.out.write.NoticeLoadPort;
import com.tastyhouse.application.notice.port.out.write.NoticeSavePort;

import static com.tastyhouse.infrastructure.jpa.notice.persistence.QNoticeJpaEntity.noticeJpaEntity;

@Repository
class NoticePersistenceAdapter implements NoticeLoadPort, NoticeSavePort {

    private final JPAQueryFactory queryFactory;
    private final NoticeJpaRepository noticeJpaRepository;

    public NoticePersistenceAdapter(JPAQueryFactory queryFactory, NoticeJpaRepository noticeJpaRepository) {
        this.queryFactory = queryFactory;
        this.noticeJpaRepository = noticeJpaRepository;
    }

    @Override
    public Optional<Notice> findActiveById(NoticeId noticeId) {
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

        NoticeJpaEntity entity = noticeJpaRepository.findById(notice.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 공지사항입니다: " + notice.getId()));
        NoticeMapper.applyChanges(entity, notice);
        return NoticeMapper.toDomain(entity);
    }
}
