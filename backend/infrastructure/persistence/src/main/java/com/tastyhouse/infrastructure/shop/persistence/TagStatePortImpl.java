package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.TagState;
import com.tastyhouse.application.shop.port.out.write.TagStatePort;

import static com.tastyhouse.infrastructure.shop.persistence.QTagJpaEntity.tagJpaEntity;

@Repository
public class TagStatePortImpl implements TagStatePort {
    private final JPAQueryFactory queryFactory;
    private final TagJpaRepository tagJpaRepository;

    public TagStatePortImpl(JPAQueryFactory queryFactory, TagJpaRepository tagJpaRepository) {
        this.queryFactory = queryFactory;
        this.tagJpaRepository = tagJpaRepository;
    }

    @Override
    public Optional<TagState> findByTagName(String tagName) {
        TagJpaEntity result = queryFactory
            .selectFrom(tagJpaEntity)
            .where(tagJpaEntity.tagName.eq(tagName))
            .fetchOne();
        return Optional.ofNullable(result).map(TagMapper::toState);
    }

    @Override
    public TagState save(TagState tag) {
        if (tag.id() == null) {
            TagJpaEntity saved = tagJpaRepository.save(TagMapper.toEntity(tag));
            return TagMapper.toState(saved);
        }

        TagJpaEntity entity = tagJpaRepository.findById(tag.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 태그입니다: " + tag.id()));
        return TagMapper.toState(entity);
    }

    @Override
    public void deleteById(Long id) {
        tagJpaRepository.deleteById(id);
    }
}
