package com.tastyhouse.webapi.member.service;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.entity.review.dto.MyReviewListItemDto;
import com.tastyhouse.core.service.ReviewCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.member.response.MyReviewCountResponse;
import com.tastyhouse.webapi.member.response.MyReviewListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberReviewService {

    private final ReviewCoreService reviewCoreService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public PageResult<MyReviewListItemResponse> getMyReviews(Long memberId, int page, int size) {
        PageResult<MyReviewListItemDto> coreResult = reviewCoreService.findMyReviews(
            memberId,
            page,
            size
        );
        return coreResult.map(dto -> MyReviewListItemResponse.from(
            dto.id(),
            fileService.getUrlByPath(dto.imageUrl()))
        );
    }

    @Transactional(readOnly = true)
    public MyReviewCountResponse getMyReviewCount(Long memberId) {
        long count = reviewCoreService.countVisibleReviewsByMemberId(memberId);
        return MyReviewCountResponse.from(count);
    }
}
