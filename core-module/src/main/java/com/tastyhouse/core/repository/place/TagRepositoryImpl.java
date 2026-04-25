package com.tastyhouse.core.repository.place;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.place.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.tastyhouse.core.entity.place.QTag.tag;

@Repository
@RequiredArgsConstructor
public class TagRepositoryImpl implements TagRepository {

    private final JPAQueryFactory queryFactory;
    private final TagJpaRepository tagJpaRepository;

    @Override
    public List<String> findTagNamesByIds(List<Long> tagIds) {
        return queryFactory
            .select(tag.tagName)
            .from(tag)
            .where(tag.id.in(tagIds))
            .fetch();
    }

    @Override
    public Optional<Tag> findByTagName(String tagName) {
        Tag result = queryFactory
            .selectFrom(tag)
            .where(tag.tagName.eq(tagName))
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public Tag save(Tag tag) {
        return tagJpaRepository.save(tag);
    }
}
