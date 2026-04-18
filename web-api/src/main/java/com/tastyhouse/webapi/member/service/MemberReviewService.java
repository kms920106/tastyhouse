package com.tastyhouse.webapi.member.service;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.entity.review.dto.MyReviewListItemDto;
import com.tastyhouse.core.service.ReviewCoreService;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.member.response.MyReviewCountResponse;
import com.tastyhouse.webapi.member.response.MyReviewListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberReviewService {

    private final ReviewCoreService reviewCoreService;

    // 내가 작성한 리뷰 목록을 페이지네이션하여 조회
    @Transactional(readOnly = true)
    public PageResult<MyReviewListItemResponse> getMyReviews(Long memberId, PageRequest pageRequest) {
        PageResult<MyReviewListItemDto> coreResult = reviewCoreService.findMyReviews(
            memberId, pageRequest.page(), pageRequest.size()
        );
        return coreResult.map(MyReviewListItemResponse::from);
    }

    @Transactional(readOnly = true)
    public MyReviewCountResponse getMyReviewCount(Long memberId) {
        long count = reviewCoreService.countVisibleReviewsByMemberId(memberId);
        return MyReviewCountResponse.from(count);
    }
}
