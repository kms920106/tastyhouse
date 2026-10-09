package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.Tag;
import com.tastyhouse.application.shop.port.out.write.TagPersistencePort;

import static com.tastyhouse.infrastructure.persistence.shop.persistence.QTagJpaEntity.tagJpaEntity;

@Repository
class TagPersistenceAdapter implements TagPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final TagJpaRepository tagJpaRepository;

    public TagPersistenceAdapter(JPAQueryFactory queryFactory, TagJpaRepository tagJpaRepository) {
        this.queryFactory = queryFactory;
        this.tagJpaRepository = tagJpaRepository;
    }

    @Override
    public Optional<Tag> findByTagName(String tagName) {
        TagJpaEntity entity = queryFactory
            .selectFrom(tagJpaEntity)
            .where(tagJpaEntity.tagName.eq(tagName))
            .fetchOne();
        return Optional.ofNullable(entity).map(TagMapper::toDomain);
    }

    @Override
    public Tag save(Tag tag) {
        if (tag.getId() == null) {
            TagJpaEntity saved = tagJpaRepository.save(TagMapper.toEntity(tag));
            return TagMapper.toDomain(saved);
        }

        TagJpaEntity entity = tagJpaRepository.findById(tag.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 태그입니다: " + tag.getId()));
        return TagMapper.toDomain(entity);
    }

    @Override
    public void deleteById(Long id) {
        tagJpaRepository.deleteById(id);
    }
}
