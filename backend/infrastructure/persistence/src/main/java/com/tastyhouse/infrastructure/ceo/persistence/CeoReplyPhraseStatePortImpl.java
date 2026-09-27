package com.tastyhouse.infrastructure.ceo.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.ceo.port.out.write.CeoReplyPhraseState;
import com.tastyhouse.application.ceo.port.out.write.CeoReplyPhraseStatePort;

import static com.tastyhouse.infrastructure.ceo.persistence.QCeoReplyPhraseJpaEntity.ceoReplyPhraseJpaEntity;

@Repository
public class CeoReplyPhraseStatePortImpl implements CeoReplyPhraseStatePort {
    private final JPAQueryFactory queryFactory;
    private final CeoReplyPhraseJpaRepository ceoReplyPhraseJpaRepository;

    public CeoReplyPhraseStatePortImpl(
        JPAQueryFactory queryFactory,
        CeoReplyPhraseJpaRepository ceoReplyPhraseJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.ceoReplyPhraseJpaRepository = ceoReplyPhraseJpaRepository;
    }

    @Override
    public Optional<CeoReplyPhraseState> findById(Long id) {
        return ceoReplyPhraseJpaRepository.findById(id)
            .map(CeoReplyPhraseMapper::toState);
    }

    @Override
    public List<CeoReplyPhraseState> findAllByCeoId(Long ceoId) {
        return queryFactory
            .selectFrom(ceoReplyPhraseJpaEntity)
            .where(ceoReplyPhraseJpaEntity.ceoId.eq(ceoId))
            .orderBy(ceoReplyPhraseJpaEntity.sort.asc(), ceoReplyPhraseJpaEntity.id.asc())
            .fetch()
            .stream()
            .map(CeoReplyPhraseMapper::toState)
            .toList();
    }

    @Override
    public long countByCeoId(Long ceoId) {
        Long count = queryFactory
            .select(ceoReplyPhraseJpaEntity.count())
            .from(ceoReplyPhraseJpaEntity)
            .where(ceoReplyPhraseJpaEntity.ceoId.eq(ceoId))
            .fetchOne();
        return count == null ? 0L : count;
    }

    @Override
    public CeoReplyPhraseState save(CeoReplyPhraseState state) {
        if (state.id() == null) {
            CeoReplyPhraseJpaEntity saved =
                ceoReplyPhraseJpaRepository.save(CeoReplyPhraseMapper.toEntity(state));
            return CeoReplyPhraseMapper.toState(saved);
        }

        CeoReplyPhraseJpaEntity entity = ceoReplyPhraseJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 자주 쓰는 문구입니다: " + state.id()));
        CeoReplyPhraseMapper.applyChanges(entity, state);
        return CeoReplyPhraseMapper.toState(entity);
    }

    @Override
    public void delete(Long id) {
        ceoReplyPhraseJpaRepository.deleteById(id);
    }
}
