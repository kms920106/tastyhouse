package com.tastyhouse.webapi.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.review.application.ReviewQueryService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.common.PageResponse;
import com.tastyhouse.webapi.member.response.MyReviewCountResponse;
import com.tastyhouse.webapi.member.response.MyReviewListItemResponse;

@Service
@RequiredArgsConstructor
public class MemberReviewService {

    private final ReviewQueryService reviewQueryService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public PageResponse<MyReviewListItemResponse> getMyReviews(Long memberId, int page, int size) {
        return PageResponse.from(reviewQueryService.findMyReviews(memberId, page, size))
            .map(dto -> MyReviewListItemResponse.from(
                dto.id(),
                fileService.getUrlByPath(dto.imageUrl())
            ));
    }

    @Transactional(readOnly = true)
    public MyReviewCountResponse getMyReviewCount(Long memberId) {
        long count = reviewQueryService.countVisibleReviewsByMemberId(memberId);
        return MyReviewCountResponse.from(count);
    }
}
