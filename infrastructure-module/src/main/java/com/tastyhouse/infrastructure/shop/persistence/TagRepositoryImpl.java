package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.shop.domain.model.Tag;
import com.tastyhouse.core.domain.shop.domain.repository.TagRepository;

import static com.tastyhouse.infrastructure.shop.persistence.QTagJpaEntity.tagJpaEntity;

@Repository
@RequiredArgsConstructor
public class TagRepositoryImpl implements TagRepository {

    private final JPAQueryFactory queryFactory;
    private final TagJpaRepository tagJpaRepository;

    @Override
    public List<String> findTagNamesByIds(List<Long> tagIds) {
        return queryFactory
            .select(tagJpaEntity.tagName)
            .from(tagJpaEntity)
            .where(tagJpaEntity.id.in(tagIds))
            .fetch();
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
    public List<Tag> findAllTags() {
        return queryFactory
            .selectFrom(tagJpaEntity)
            .orderBy(tagJpaEntity.id.desc())
            .fetch()
            .stream()
            .map(TagMapper::toDomain)
            .toList();
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
