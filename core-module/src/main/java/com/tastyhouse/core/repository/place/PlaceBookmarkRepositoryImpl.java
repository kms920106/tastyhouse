package com.tastyhouse.core.repository.place;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.place.PlaceBookmark;
import com.tastyhouse.core.entity.place.QPlaceBookmark;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PlaceBookmarkRepositoryImpl implements PlaceBookmarkRepository {

    private final JPAQueryFactory queryFactory;
    private final PlaceBookmarkJpaRepository placeBookmarkJpaRepository;

    @Override
    public Optional<PlaceBookmark> findByPlaceIdAndMemberId(Long placeId, Long memberId) {
        QPlaceBookmark bookmark = QPlaceBookmark.placeBookmark;
        PlaceBookmark result = queryFactory
            .selectFrom(bookmark)
            .where(bookmark.placeId.eq(placeId), bookmark.memberId.eq(memberId))
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public boolean existsByPlaceIdAndMemberId(Long placeId, Long memberId) {
        QPlaceBookmark bookmark = QPlaceBookmark.placeBookmark;
        return queryFactory
            .selectOne()
            .from(bookmark)
            .where(bookmark.placeId.eq(placeId), bookmark.memberId.eq(memberId))
            .fetchFirst() != null;
    }

    @Override
    public void deleteByPlaceIdAndMemberId(Long placeId, Long memberId) {
        QPlaceBookmark bookmark = QPlaceBookmark.placeBookmark;
        queryFactory
            .delete(bookmark)
            .where(bookmark.placeId.eq(placeId), bookmark.memberId.eq(memberId))
            .execute();
    }

    @Override
    public PlaceBookmark save(PlaceBookmark bookmark) {
        return placeBookmarkJpaRepository.save(bookmark);
    }
}
