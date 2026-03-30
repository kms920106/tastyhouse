package com.tastyhouse.core.repository.place;

import java.util.Optional;
import com.tastyhouse.core.entity.place.PlaceBookmark;

public interface PlaceBookmarkRepository {

    Optional<PlaceBookmark> findByPlaceIdAndMemberId(Long placeId, Long memberId);

    boolean existsByPlaceIdAndMemberId(Long placeId, Long memberId);

    void deleteByPlaceIdAndMemberId(Long placeId, Long memberId);
}
