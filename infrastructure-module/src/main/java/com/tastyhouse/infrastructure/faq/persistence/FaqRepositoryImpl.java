package com.tastyhouse.infrastructure.faq.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.faq.domain.model.Faq;
import com.tastyhouse.core.domain.faq.domain.repository.FaqRepository;
import com.tastyhouse.core.domain.faq.domain.vo.FaqId;

import static com.tastyhouse.infrastructure.faq.persistence.QFaqJpaEntity.faqJpaEntity;

/**
 * FAQ 항목 write 어댑터.
 *
 * <p>도메인 모델 단건 로드와 저장만 담당한다. 표현 목적 조회는 같은 모듈의
 * {@code faq/query/FaqQueryDao}로 분리되어 있다.
 */
@Repository
@RequiredArgsConstructor
public class FaqRepositoryImpl implements FaqRepository {

    private final JPAQueryFactory queryFactory;
    private final FaqJpaRepository faqJpaRepository;

    @Override
    public Optional<Faq> findById(FaqId faqId) {
        if (faqId == null) {
            return Optional.empty();
        }
        FaqJpaEntity entity = queryFactory
            .selectFrom(faqJpaEntity)
            .where(faqJpaEntity.id.eq(faqId.value()), faqJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(FaqMapper::toDomain);
    }

    @Override
    public Faq save(Faq faq) {
        if (faq.getId() == null) {
            FaqJpaEntity saved = faqJpaRepository.save(FaqMapper.toEntity(faq));
            return FaqMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        FaqJpaEntity entity = faqJpaRepository.findById(faq.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 FAQ입니다: " + faq.getId()));
        FaqMapper.applyChanges(entity, faq);
        return FaqMapper.toDomain(entity);
    }
}
