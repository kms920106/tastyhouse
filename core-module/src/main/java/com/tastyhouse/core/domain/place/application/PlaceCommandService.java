package com.tastyhouse.core.domain.place.application;

import com.tastyhouse.core.domain.place.domain.model.PlaceBookmark;
import com.tastyhouse.core.domain.place.domain.repository.PlaceBookmarkRepository;
import com.tastyhouse.core.domain.place.infrastructure.persistence.PlaceBookmarkJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PlaceCommandService {

    private final PlaceQueryService placeQueryService;
    private final PlaceBookmarkRepository placeBookmarkRepository;
    private final PlaceBookmarkJpaRepository placeBookmarkJpaRepository;

    public boolean toggleBookmark(Long placeId, Long memberId) {
        if (placeBookmarkRepository.existsByPlaceIdAndMemberId(placeId, memberId)) {
            placeBookmarkRepository.deleteByPlaceIdAndMemberId(placeId, memberId);
            return false;
        } else {
            placeQueryService.findPlaceById(placeId);
            placeBookmarkJpaRepository.save(new PlaceBookmark(placeId, memberId));
            return true;
        }
    }
}
