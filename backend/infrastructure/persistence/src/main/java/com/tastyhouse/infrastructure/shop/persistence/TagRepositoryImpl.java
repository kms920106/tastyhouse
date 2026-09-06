package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.Tag;
import com.tastyhouse.domain.shop.repository.TagRepository;

import static com.tastyhouse.infrastructure.shop.persistence.QTagJpaEntity.tagJpaEntity;

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

        TagJpaEntity entity = tagJpaRepository.findById(tag.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 태그입니다: " + tag.getId()));
        return TagMapper.toDomain(entity);
    }

    @Override
    public void deleteById(Long id) {
        tagJpaRepository.deleteById(id);
    }
}
