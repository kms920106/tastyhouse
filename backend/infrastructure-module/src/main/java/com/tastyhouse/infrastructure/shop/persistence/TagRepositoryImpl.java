package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.Tag;
import com.tastyhouse.domain.shop.repository.TagRepository;

import static com.tastyhouse.infrastructure.shop.persistence.QTagJpaEntity.tagJpaEntity;

/**
 * 태그 write 어댑터.
 *
 * <p>목록 조회({@code findAllTags})는 같은 모듈의
 * {@link com.tastyhouse.infrastructure.shop.query.ShopChoiceQueryDao}로 이관했고,
 * {@code findTagNamesByIds}는 review 도메인이 자기 {@code ReviewQueryDao}에 같은 조회를 갖게 되어
 * 소비자가 사라져 제거했다(공통 지침 패턴 4).
 */
@Repository
public class TagRepositoryImpl implements TagRepository {

    private final JPAQueryFactory queryFactory;
    private final TagJpaRepository tagJpaRepository;

    public TagRepositoryImpl(JPAQueryFactory queryFactory, TagJpaRepository tagJpaRepository) {
        this.queryFactory = queryFactory;
        this.tagJpaRepository = tagJpaRepository;
    }

    @Override
    public Optional<Tag> findByTagName(String tagName) {
        TagJpaEntity result = queryFactory
            .selectFrom(tagJpaEntity)
            .where(tagJpaEntity.tagName.eq(tagName))
            .fetchOne();
        return Optional.ofNullable(result).map(TagMapper::toDomain);
    }

    @Override
    public Tag save(Tag tag) {
        if (tag.getId() == null) {
            TagJpaEntity saved = tagJpaRepository.save(TagMapper.toEntity(tag));
            return TagMapper.toDomain(saved);
        }

        // update 경로 없음(Tag는 insert-only) — 존재 시에도 재조회만 수행
        TagJpaEntity entity = tagJpaRepository.findById(tag.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 태그입니다: " + tag.getId()));
        return TagMapper.toDomain(entity);
    }

    @Override
    public void deleteById(Long id) {
        tagJpaRepository.deleteById(id);
    }
}
