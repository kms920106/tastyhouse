package com.tastyhouse.webapi.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.webapi.file.FileService;
import com.tastyhouse.webapi.member.response.MyReviewCountResponse;
import com.tastyhouse.webapi.member.response.MyReviewListItemResponse;
import com.tastyhouse.webapi.review.ReviewQueryService;

@Service
@RequiredArgsConstructor
public class MemberReviewService {

    private final ReviewQueryService reviewQueryService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public PageResult<MyReviewListItemResponse> getMyReviews(Long memberId, int page, int size) {
        return reviewQueryService.findMyReviews(memberId, page, size)
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
