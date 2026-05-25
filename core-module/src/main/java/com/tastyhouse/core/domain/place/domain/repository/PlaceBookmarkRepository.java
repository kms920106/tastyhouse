package com.tastyhouse.core.domain.place.domain.repository;

import com.tastyhouse.core.domain.place.domain.model.PlaceBookmark;

import java.util.Optional;

public interface PlaceBookmarkRepository {

    Optional<PlaceBookmark> findByPlaceIdAndMemberId(Long placeId, Long memberId);

    boolean existsByPlaceIdAndMemberId(Long placeId, Long memberId);

    void deleteByPlaceIdAndMemberId(Long placeId, Long memberId);
}
