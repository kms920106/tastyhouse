package com.tastyhouse.webapi.review;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.order.domain.model.OrderProduct;
import com.tastyhouse.core.domain.order.domain.vo.OrderProductId;
import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.vo.ProductId;
import com.tastyhouse.core.domain.review.domain.model.Review;
import com.tastyhouse.core.domain.review.domain.model.ReviewComment;
import com.tastyhouse.core.domain.review.domain.model.ReviewReply;
import com.tastyhouse.core.domain.review.domain.vo.ReviewCommentId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;
import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import com.tastyhouse.core.domain.order.application.OrderQueryService;
import com.tastyhouse.core.domain.product.application.ProductQueryService;
import com.tastyhouse.core.domain.review.application.ReviewCommandService;
import com.tastyhouse.core.domain.review.application.ReviewQueryService;
import com.tastyhouse.core.domain.review.application.dto.command.ReviewCommentCreateCommand;
import com.tastyhouse.core.domain.review.application.dto.command.ReviewCreateCommand;
import com.tastyhouse.core.domain.review.application.dto.command.ReviewDeleteCommand;
import com.tastyhouse.core.domain.review.application.dto.command.ReviewReplyCreateCommand;
import com.tastyhouse.core.domain.review.application.dto.command.ReviewUpdateCommand;
import com.tastyhouse.core.domain.review.application.dto.command.ToggleReviewLikeCommand;
import com.tastyhouse.core.domain.review.application.dto.result.BestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.LatestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewDetailResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewResult;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.webapi.file.FileService;
import com.tastyhouse.webapi.common.PaginationResponse;
import com.tastyhouse.webapi.review.request.ReviewCreateRequest;
import com.tastyhouse.webapi.review.request.ReviewUpdateRequest;
import com.tastyhouse.webapi.review.response.ReviewBestListItemResponse;
import com.tastyhouse.webapi.review.response.ReviewCommentListResponse;
import com.tastyhouse.webapi.review.response.ReviewCommentResponse;
import com.tastyhouse.webapi.review.response.ReviewDetailResponse;
import com.tastyhouse.webapi.review.response.ReviewLatestListItemResponse;
import com.tastyhouse.webapi.review.response.ReviewLikeStatusResponse;
import com.tastyhouse.webapi.review.response.ReviewMemberListItemResponse;
import com.tastyhouse.webapi.review.response.ReviewProductResponse;
import com.tastyhouse.webapi.review.response.ReviewReplyResponse;
import com.tastyhouse.webapi.review.response.ReviewResponse;
import com.tastyhouse.webapi.review.response.ReviewWriteInfoResponse;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;
    private final ProductQueryService productQueryService;
    private final OrderQueryService orderQueryService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public PaginationResponse<ReviewBestListItemResponse> searchBestReviewList(int page, int size) {
        PageResult<ReviewBestListItemResponse> pageResult = reviewQueryService.findBestReviewsWithPagination(page, size)
            .map(this::convertToBestReviewListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    private ReviewBestListItemResponse convertToBestReviewListItemResponse(BestReviewListItemResult dto) {
        return ReviewBestListItemResponse.from(
            dto.id(),
            fileService.getUrlByPath(dto.imageUrl()),
            dto.stationName(),
            dto.shopName(),
            dto.productName(),
            dto.totalRating(),
            dto.content()
        );
    }

    @Transactional(readOnly = true)
    public PaginationResponse<ReviewLatestListItemResponse> searchLatestReviewList(
        int page,
        int size,
        String type,
        Long memberId
    ) {
        PageResult<ReviewLatestListItemResponse> pageResult;
        if (ReviewListType.from(type) == ReviewListType.FOLLOWING && memberId != null) {
            pageResult = reviewQueryService.findLatestReviewsByFollowingWithPagination(MemberId.of(memberId), page, size)
                .map(this::convertToLatestReviewListItemResponse);
        } else {
            pageResult = reviewQueryService.findLatestReviewsWithPagination(page, size)
                .map(this::convertToLatestReviewListItemResponse);
        }
        return PaginationResponse.from(pageResult);
    }

    private ReviewLatestListItemResponse convertToLatestReviewListItemResponse(LatestReviewListItemResult dto) {
        List<String> imageUrls = dto.imageUrls() == null ? List.of() :
            dto.imageUrls().stream()
                .map(fileService::getUrlByPath)
                .toList();

        return ReviewLatestListItemResponse.from(
            dto.id(), imageUrls, dto.stationName(), dto.totalRating(), dto.content(),
            dto.memberId().value(), dto.memberNickname(),
            fileService.getUrlByPath(dto.memberProfileImageUrl()),
            dto.createdAt(), dto.likeCount(), dto.commentCount()
        );
    }

    @Transactional(readOnly = true)
    public Optional<ReviewDetailResponse> findReviewDetail(Long reviewId) {
        return reviewQueryService.findReviewDetail(ReviewId.of(reviewId))
            .map(this::convertToReviewDetailResponse);
    }

    private ReviewDetailResponse convertToReviewDetailResponse(ReviewDetailResult dto) {
        List<String> imageUrls = dto.imageUrls() == null ? List.of() :
            dto.imageUrls().stream()
                .map(fileService::getUrlByPath)
                .toList();

        return ReviewDetailResponse.from(
            dto.id(),
            dto.shopId(),
            dto.shopName(),
            dto.stationName(),
            dto.content(),
            dto.totalRating(),
            dto.tasteRating(),
            dto.amountRating(),
            dto.priceRating(),
            dto.atmosphereRating(),
            dto.kindnessRating(),
            dto.hygieneRating(),
            dto.willRevisit(),
            dto.memberId().value(),
            dto.memberNickname(),
            fileService.getUrlByPath(dto.memberProfileImageUrl()),
            dto.createdAt(),
            imageUrls,
            dto.tagNames()
        );
    }

    @Transactional(readOnly = true)
    public ReviewLikeStatusResponse isLiked(Long reviewId, Long memberId) {
        boolean isLiked = reviewQueryService.isLikedByMember(ReviewId.of(reviewId), MemberId.of(memberId));
        return ReviewLikeStatusResponse.from(isLiked);
    }

    @Transactional
    public boolean toggleReviewLike(Long reviewId, Long memberId) {
        ToggleReviewLikeCommand command = ToggleReviewLikeCommand.of(ReviewId.of(reviewId), MemberId.of(memberId));
        return reviewCommandService.toggleReviewLike(command);
    }

    @Transactional
    public ReviewCommentResponse createComment(Long reviewId, Long memberId, String content) {
        ReviewCommentCreateCommand command = ReviewCommentCreateCommand.of(ReviewId.of(reviewId), MemberId.of(memberId), content);
        ReviewComment comment = reviewCommandService.createComment(command);
        MemberWithProfileImageResult member = reviewQueryService.findMemberWithProfileImagesByIds(List.of(memberId)).get(memberId);
        return convertToCommentResponse(comment, member, List.of());
    }

    @Transactional
    public ReviewReplyResponse createReply(Long commentId, Long memberId, Long replyToMemberId, String content) {
        ReviewReplyCreateCommand command = ReviewReplyCreateCommand.of(
            ReviewCommentId.of(commentId),
            MemberId.of(memberId),
            replyToMemberId == null ? null : MemberId.of(replyToMemberId),
            content
        );
        ReviewReply reply = reviewCommandService.createReply(command);
        List<Long> ids = replyToMemberId != null ? List.of(memberId, replyToMemberId) : List.of(memberId);
        Map<Long, MemberWithProfileImageResult> memberMap = reviewQueryService.findMemberWithProfileImagesByIds(ids);
        return convertToReplyResponse(reply, memberMap.get(memberId), replyToMemberId != null ? memberMap.get(replyToMemberId) : null);
    }

    @Transactional(readOnly = true)
    public ReviewCommentListResponse searchCommentsWithReplies(Long reviewId) {
        List<ReviewComment> comments = reviewQueryService.findCommentsByReviewId(ReviewId.of(reviewId));

        if (comments.isEmpty()) {
            return ReviewCommentListResponse.from(List.of(), 0);
        }

        List<ReviewCommentId> commentIds = comments.stream()
            .map(ReviewComment::getReviewCommentId)
            .toList();

        List<ReviewReply> allReplies = reviewQueryService.findRepliesByCommentIds(commentIds);

        Map<Long, List<ReviewReply>> repliesByCommentId = allReplies.stream()
            .collect(Collectors.groupingBy(ReviewReply::getCommentId));

        List<Long> memberIds = new ArrayList<>();
        comments.forEach(c -> memberIds.add(c.getMemberId().value()));
        allReplies.forEach(r -> {
            memberIds.add(r.getMemberId().value());
            if (r.getReplyToMemberId() != null) {
                memberIds.add(r.getReplyToMemberId().value());
            }
        });

        Map<Long, MemberWithProfileImageResult> memberMap = reviewQueryService.findMemberWithProfileImagesByIds(memberIds);

        List<ReviewCommentResponse> commentResponses = comments.stream()
            .map(comment -> {
                MemberWithProfileImageResult member = memberMap.get(comment.getMemberId().value());
                List<ReviewReply> replies = repliesByCommentId.getOrDefault(comment.getId(), List.of());
                List<ReviewReplyResponse> replyResponses = replies.stream()
                    .map(reply -> convertToReplyResponse(
                        reply,
                        memberMap.get(reply.getMemberId().value()),
                        reply.getReplyToMemberId() != null ? memberMap.get(reply.getReplyToMemberId().value()) : null
                    ))
                    .toList();
                return convertToCommentResponse(comment, member, replyResponses);
            })
            .toList();

        int totalCount = comments.size() + allReplies.size();
        return ReviewCommentListResponse.from(commentResponses, totalCount);
    }

    private ReviewCommentResponse convertToCommentResponse(ReviewComment comment, MemberWithProfileImageResult member, List<ReviewReplyResponse> replies) {
        return ReviewCommentResponse.from(
            comment.getId(),
            comment.getReviewId(),
            comment.getMemberId().value(),
            member != null ? member.nickname() : null,
            member != null ? fileService.getUrlByPath(member.profileImageFilePath()) : null,
            comment.getContent(),
            comment.getCreatedAt(),
            replies
        );
    }

    private ReviewReplyResponse convertToReplyResponse(ReviewReply reply, MemberWithProfileImageResult member, MemberWithProfileImageResult replyToMember) {
        return ReviewReplyResponse.from(
            reply.getId(),
            reply.getCommentId(),
            reply.getMemberId().value(),
            member != null ? member.nickname() : null,
            member != null ? fileService.getUrlByPath(member.profileImageFilePath()) : null,
            reply.getReplyToMemberId() != null ? reply.getReplyToMemberId().value() : null,
            replyToMember != null ? replyToMember.nickname() : null,
            reply.getContent(),
            reply.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Optional<ReviewProductResponse> findReviewProduct(Long reviewId) {
        Optional<ReviewDetailResult> reviewDetailOpt = reviewQueryService.findReviewDetail(ReviewId.of(reviewId));
        if (reviewDetailOpt.isEmpty()) {
            return Optional.empty();
        }

        ReviewDetailResult reviewDetail = reviewDetailOpt.get();
        Review review = reviewQueryService.findById(ReviewId.of(reviewId));

        List<String> reviewImageUrls = reviewDetail.imageUrls() == null ? List.of() :
            reviewDetail.imageUrls().stream()
                .map(fileService::getUrlByPath)
                .toList();
        String reviewMemberProfileImageUrl = fileService.getUrlByPath(reviewDetail.memberProfileImageUrl());

        return productQueryService.findProductById(ProductId.of(review.getProductId()))
            .map(product -> {
                Integer price = product.getDiscountPrice() != null
                    ? product.getDiscountPrice()
                    : product.getOriginalPrice();

                return ReviewProductResponse.from(
                    product.getId(),
                    product.getName(),
                    getFirstImageUrl(product.getId()),
                    price,
                    reviewDetail.id(),
                    reviewDetail.content(),
                    reviewDetail.totalRating(),
                    reviewDetail.tasteRating(),
                    reviewDetail.amountRating(),
                    reviewDetail.priceRating(),
                    reviewDetail.atmosphereRating(),
                    reviewDetail.kindnessRating(),
                    reviewDetail.hygieneRating(),
                    reviewDetail.willRevisit(),
                    reviewDetail.memberId().value(),
                    reviewDetail.memberNickname(),
                    reviewMemberProfileImageUrl,
                    reviewDetail.createdAt(),
                    reviewImageUrls,
                    reviewDetail.tagNames()
                );
            })
            .or(() -> Optional.of(
                ReviewProductResponse.from(
                    null, null, null, null,
                    reviewDetail.id(),
                    reviewDetail.content(),
                    reviewDetail.totalRating(),
                    reviewDetail.tasteRating(),
                    reviewDetail.amountRating(),
                    reviewDetail.priceRating(),
                    reviewDetail.atmosphereRating(),
                    reviewDetail.kindnessRating(),
                    reviewDetail.hygieneRating(),
                    reviewDetail.willRevisit(),
                    reviewDetail.memberId().value(),
                    reviewDetail.memberNickname(),
                    reviewMemberProfileImageUrl,
                    reviewDetail.createdAt(),
                    reviewImageUrls,
                    reviewDetail.tagNames()
                )
            ));
    }

    private String getFirstImageUrl(Long productId) {
        return fileService.getUrlByPath(productQueryService.getFirstImageFilePath(productId));
    }

    @Transactional(readOnly = true)
    public ReviewWriteInfoResponse getReviewWriteInfo(Long orderProductId, Long memberId) {
        OrderProduct orderProduct = orderQueryService.findOrderProductById(OrderProductId.of(orderProductId));

        Product product = productQueryService.findProductById(ProductId.of(orderProduct.getProductId()))
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND));

        Integer price = product.getDiscountPrice() != null
                ? product.getDiscountPrice()
                : product.getOriginalPrice();

        boolean isReviewed = reviewQueryService.isReviewedByOrderAndProduct(
            orderProduct.getOrderId(), orderProduct.getProductId(), MemberId.of(memberId)
        );

        return ReviewWriteInfoResponse.from(
            product.getId(),
            product.getName(),
            getFirstImageUrl(product.getId()),
            price,
            orderProduct.getOrderId(),
            isReviewed
        );
    }

    @Transactional
    public ReviewResponse createReview(Long memberId, ReviewCreateRequest request) {
        Long orderId = null;
        if (request.orderProductId() != null) {
            OrderProduct orderProduct = orderQueryService.findOrderProductById(OrderProductId.of(request.orderProductId()));
            orderId = orderProduct.getOrderId();
        }

        Product product = productQueryService.findProductById(ProductId.of(request.productId()))
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND));

        ReviewCreateCommand command = ReviewCreateCommand.of(
            product.getShopId(),
            product.getId(),
            MemberId.of(memberId),
            request.orderProductId(),
            orderId,
            request.tasteRating(),
            request.amountRating(),
            request.priceRating(),
            request.content(),
            request.uploadedFileIds(),
            request.tags()
        );
        ReviewResult result = reviewCommandService.createReview(command);

        return ReviewResponse.from(
            result.id().value(),
            result.productId(),
            result.tasteRating(),
            result.amountRating(),
            result.priceRating(),
            result.totalRating(),
            result.content(),
            result.uploadedFileIds(),
            result.tags(),
            result.createdAt()
        );
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long memberId, ReviewUpdateRequest request) {
        ReviewUpdateCommand command = ReviewUpdateCommand.of(
            ReviewId.of(reviewId),
            MemberId.of(memberId),
            request.tasteRating(),
            request.amountRating(),
            request.priceRating(),
            request.content(),
            request.uploadedFileIds(),
            request.tags()
        );
        ReviewResult result = reviewCommandService.updateReview(command);

        return ReviewResponse.from(
            result.id().value(),
            result.productId(),
            result.tasteRating(),
            result.amountRating(),
            result.priceRating(),
            result.totalRating(),
            result.content(),
            result.uploadedFileIds(),
            result.tags(),
            result.createdAt()
        );
    }

    @Transactional
    public void deleteReview(Long reviewId, Long memberId) {
        Review review = reviewQueryService.findById(ReviewId.of(reviewId));
        ReviewDeleteCommand command = ReviewDeleteCommand.of(ReviewId.of(reviewId), MemberId.of(memberId), review.getProductId());
        reviewCommandService.deleteReview(command);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<ReviewMemberListItemResponse> findMemberReviews(Long memberId, int page, int size) {
        PageResult<ReviewMemberListItemResponse> pageResult = reviewQueryService.findReviewsByMemberId(MemberId.of(memberId), page, size)
            .map(dto -> ReviewMemberListItemResponse.from(
                dto.id(),
                fileService.getUrlByPath(dto.imageUrl())
            ));
        return PaginationResponse.from(pageResult);
    }
}
