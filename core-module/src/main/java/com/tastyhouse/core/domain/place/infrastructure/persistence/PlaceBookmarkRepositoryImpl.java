package com.tastyhouse.core.domain.place.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.place.domain.model.PlaceBookmark;
import com.tastyhouse.core.domain.place.domain.repository.PlaceBookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.tastyhouse.core.domain.place.domain.model.QPlaceBookmark.placeBookmark;

@Repository
@RequiredArgsConstructor
public class PlaceBookmarkRepositoryImpl implements PlaceBookmarkRepository {

    private final JPAQueryFactory queryFactory;
    private final PlaceBookmarkJpaRepository placeBookmarkJpaRepository;

    @Override
    public Optional<PlaceBookmark> findByPlaceIdAndMemberId(Long placeId, Long memberId) {
        PlaceBookmark result = queryFactory
            .selectFrom(placeBookmark)
            .where(placeBookmark.placeId.eq(placeId), placeBookmark.memberId.eq(memberId))
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public boolean existsByPlaceIdAndMemberId(Long placeId, Long memberId) {
        return queryFactory
            .selectOne()
            .from(placeBookmark)
            .where(placeBookmark.placeId.eq(placeId), placeBookmark.memberId.eq(memberId))
            .fetchFirst() != null;
    }

    @Override
    public void deleteByPlaceIdAndMemberId(Long placeId, Long memberId) {
        queryFactory
            .delete(placeBookmark)
            .where(placeBookmark.placeId.eq(placeId), placeBookmark.memberId.eq(memberId))
            .execute();
    }
}
