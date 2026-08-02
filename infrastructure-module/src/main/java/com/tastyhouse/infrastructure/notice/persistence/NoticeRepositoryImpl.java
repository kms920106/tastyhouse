package com.tastyhouse.infrastructure.notice.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.notice.domain.model.Notice;
import com.tastyhouse.domain.notice.domain.repository.NoticeRepository;
import com.tastyhouse.domain.notice.domain.vo.NoticeId;

import static com.tastyhouse.infrastructure.notice.persistence.QNoticeJpaEntity.noticeJpaEntity;

/**
 * 공지사항 write 어댑터.
 *
 * <p>도메인 모델 단건 로드와 저장만 담당한다. 표현 목적 조회는 같은 모듈의
 * {@code notice/query/NoticeQueryDao}로 분리되어 있다.
 */
@Repository
public class NoticeRepositoryImpl implements NoticeRepository {

    private final JPAQueryFactory queryFactory;
    private final NoticeJpaRepository noticeJpaRepository;

    public NoticeRepositoryImpl(JPAQueryFactory queryFactory, NoticeJpaRepository noticeJpaRepository) {
        this.queryFactory = queryFactory;
        this.noticeJpaRepository = noticeJpaRepository;
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
}
