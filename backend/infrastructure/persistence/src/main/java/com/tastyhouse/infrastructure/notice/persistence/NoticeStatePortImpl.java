package com.tastyhouse.infrastructure.notice.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.notice.port.out.write.NoticeState;
import com.tastyhouse.application.notice.port.out.write.NoticeStatePort;

import static com.tastyhouse.infrastructure.notice.persistence.QNoticeJpaEntity.noticeJpaEntity;

@Repository
public class NoticeStatePortImpl implements NoticeStatePort {
    private final JPAQueryFactory queryFactory;
    private final NoticeJpaRepository noticeJpaRepository;

    public NoticeStatePortImpl(JPAQueryFactory queryFactory, NoticeJpaRepository noticeJpaRepository) {
        this.queryFactory = queryFactory;
        this.noticeJpaRepository = noticeJpaRepository;
    }

    @Override
    public Optional<NoticeState> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        NoticeJpaEntity entity = queryFactory
            .selectFrom(noticeJpaEntity)
            .where(noticeJpaEntity.id.eq(id), noticeJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(NoticeMapper::toState);
    }

    @Override
    public NoticeState save(NoticeState state) {
        if (state.id() == null) {
            NoticeJpaEntity saved = noticeJpaRepository.save(NoticeMapper.toEntity(state));
            return NoticeMapper.toState(saved);
        }

        NoticeJpaEntity entity = noticeJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 공지사항입니다: " + state.id()));
        NoticeMapper.applyChanges(entity, state);
        return NoticeMapper.toState(entity);
    }
}
