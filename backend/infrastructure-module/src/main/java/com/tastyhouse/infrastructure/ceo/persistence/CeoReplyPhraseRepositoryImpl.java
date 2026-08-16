package com.tastyhouse.infrastructure.ceo.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.ceo.model.CeoReplyPhrase;
import com.tastyhouse.domain.ceo.repository.CeoReplyPhraseRepository;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.ceo.vo.CeoReplyPhraseId;

import static com.tastyhouse.infrastructure.ceo.persistence.QCeoReplyPhraseJpaEntity.ceoReplyPhraseJpaEntity;

/**
 * 자주 쓰는 문구 write 어댑터.
 */
@Repository
public class CeoReplyPhraseRepositoryImpl implements CeoReplyPhraseRepository {

    private final JPAQueryFactory queryFactory;
    private final CeoReplyPhraseJpaRepository ceoReplyPhraseJpaRepository;

    public CeoReplyPhraseRepositoryImpl(
        JPAQueryFactory queryFactory,
        CeoReplyPhraseJpaRepository ceoReplyPhraseJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.ceoReplyPhraseJpaRepository = ceoReplyPhraseJpaRepository;
    }

    @Override
    public Optional<CeoReplyPhrase> findById(CeoReplyPhraseId ceoReplyPhraseId) {
        return ceoReplyPhraseJpaRepository.findById(ceoReplyPhraseId.value())
            .map(CeoReplyPhraseMapper::toDomain);
    }

    @Override
    public List<CeoReplyPhrase> findAllByCeoId(CeoId ceoId) {
        return queryFactory
            .selectFrom(ceoReplyPhraseJpaEntity)
            .where(ceoReplyPhraseJpaEntity.ceoId.eq(ceoId.value()))
            .orderBy(ceoReplyPhraseJpaEntity.sort.asc(), ceoReplyPhraseJpaEntity.id.asc())
            .fetch()
            .stream()
            .map(CeoReplyPhraseMapper::toDomain)
            .toList();
    }

    @Override
    public long countByCeoId(CeoId ceoId) {
        Long count = queryFactory
            .select(ceoReplyPhraseJpaEntity.count())
            .from(ceoReplyPhraseJpaEntity)
            .where(ceoReplyPhraseJpaEntity.ceoId.eq(ceoId.value()))
            .fetchOne();
        return count == null ? 0L : count;
    }

    @Override
    public CeoReplyPhrase save(CeoReplyPhrase ceoReplyPhrase) {
        if (ceoReplyPhrase.getId() == null) {
            CeoReplyPhraseJpaEntity saved =
                ceoReplyPhraseJpaRepository.save(CeoReplyPhraseMapper.toEntity(ceoReplyPhrase));
            return CeoReplyPhraseMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        CeoReplyPhraseJpaEntity entity = ceoReplyPhraseJpaRepository.findById(ceoReplyPhrase.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 자주 쓰는 문구입니다: " + ceoReplyPhrase.getId()));
        CeoReplyPhraseMapper.applyChanges(entity, ceoReplyPhrase);
        return CeoReplyPhraseMapper.toDomain(entity);
    }

    @Override
    public void delete(CeoReplyPhrase ceoReplyPhrase) {
        ceoReplyPhraseJpaRepository.deleteById(ceoReplyPhrase.getId());
    }
}
