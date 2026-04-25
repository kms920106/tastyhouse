package com.tastyhouse.core.repository.place;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.place.PlaceBookmark;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.tastyhouse.core.entity.place.QPlaceBookmark.placeBookmark;

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
