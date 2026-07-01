package com.tastyhouse.webapi.review;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import com.tastyhouse.core.domain.order.application.OrderQueryService;
import com.tastyhouse.core.domain.order.domain.model.OrderProduct;
import com.tastyhouse.core.domain.product.application.ProductQueryService;
import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.review.application.ReviewCommandService;
import com.tastyhouse.core.domain.review.application.ReviewQueryService;
import com.tastyhouse.core.domain.review.application.dto.command.CreateReviewCommand;
import com.tastyhouse.core.domain.review.application.dto.command.CreateReviewCommentCommand;
import com.tastyhouse.core.domain.review.application.dto.command.CreateReviewReplyCommand;
import com.tastyhouse.core.domain.review.application.dto.command.DeleteReviewCommand;
import com.tastyhouse.core.domain.review.application.dto.command.ToggleReviewLikeCommand;
import com.tastyhouse.core.domain.review.application.dto.command.UpdateReviewCommand;
import com.tastyhouse.core.domain.review.application.dto.result.BestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.LatestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewDetailResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewResult;
import com.tastyhouse.core.domain.review.domain.model.Review;
import com.tastyhouse.core.domain.review.domain.model.ReviewComment;
import com.tastyhouse.core.domain.review.domain.model.ReviewReply;
import com.tastyhouse.core.domain.review.domain.model.ReviewType;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.common.PageResponse;
import com.tastyhouse.webapi.review.request.ReviewCreateRequest;
import com.tastyhouse.webapi.review.request.ReviewUpdateRequest;
import com.tastyhouse.webapi.review.response.BestReviewListItemResponse;
import com.tastyhouse.webapi.review.response.CommentListResponse;
import com.tastyhouse.webapi.review.response.CommentResponse;
import com.tastyhouse.webapi.review.response.LatestReviewListItemResponse;
import com.tastyhouse.webapi.review.response.MemberReviewListItemResponse;
import com.tastyhouse.webapi.review.response.ReplyResponse;
import com.tastyhouse.webapi.review.response.ReviewDetailResponse;
import com.tastyhouse.webapi.review.response.ReviewLikeStatusResponse;
import com.tastyhouse.webapi.review.response.ReviewProductResponse;
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
    public PageResponse<BestReviewListItemResponse> searchBestReviewList(int page, int size) {
        return PageResponse.from(reviewQueryService.findBestReviewsWithPagination(page, size))
            .map(this::convertToBestReviewListItemResponse);
    }

    private BestReviewListItemResponse convertToBestReviewListItemResponse(BestReviewListItemResult dto) {
        return BestReviewListItemResponse.from(
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
    public PageResponse<LatestReviewListItemResponse> searchLatestReviewList(
        int page,
        int size,
        ReviewType type,
        Long memberId
    ) {
        if (type == ReviewType.FOLLOWING && memberId != null) {
            return PageResponse.from(reviewQueryService.findLatestReviewsByFollowingWithPagination(memberId, page, size))
                .map(this::convertToLatestReviewListItemResponse);
        }
        return PageResponse.from(reviewQueryService.findLatestReviewsWithPagination(page, size))
            .map(this::convertToLatestReviewListItemResponse);
    }

    private LatestReviewListItemResponse convertToLatestReviewListItemResponse(LatestReviewListItemResult dto) {
        List<String> imageUrls = dto.imageUrls() == null ? List.of() :
            dto.imageUrls().stream()
                .map(fileService::getUrlByPath)
                .toList();

        return LatestReviewListItemResponse.from(
            dto.id(), imageUrls, dto.stationName(), dto.totalRating(), dto.content(),
            dto.memberId(), dto.memberNickname(),
            fileService.getUrlByPath(dto.memberProfileImageUrl()),
            dto.createdAt(), dto.likeCount(), dto.commentCount()
        );
    }

    @Transactional(readOnly = true)
    public Optional<ReviewDetailResponse> findReviewDetail(Long reviewId) {
        return reviewQueryService.findReviewDetail(reviewId)
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
            dto.memberId(),
            dto.memberNickname(),
            fileService.getUrlByPath(dto.memberProfileImageUrl()),
            dto.createdAt(),
            imageUrls,
            dto.tagNames()
        );
    }

    @Transactional(readOnly = true)
    public ReviewLikeStatusResponse isLiked(Long reviewId, Long memberId) {
        boolean isLiked = reviewQueryService.isLikedByMember(reviewId, memberId);
        return ReviewLikeStatusResponse.from(isLiked);
    }

    @Transactional
    public boolean toggleReviewLike(Long reviewId, Long memberId) {
        return reviewCommandService.toggleReviewLike(new ToggleReviewLikeCommand(reviewId, memberId));
    }

    @Transactional
    public CommentResponse createComment(Long reviewId, Long memberId, String content) {
        ReviewComment comment = reviewCommandService.createComment(
            new CreateReviewCommentCommand(reviewId, memberId, content)
        );
        MemberWithProfileImageResult member = reviewQueryService.findMemberWithProfileImagesByIds(List.of(memberId)).get(memberId);
        return convertToCommentResponse(comment, member, List.of());
    }

    @Transactional
    public ReplyResponse createReply(Long commentId, Long memberId, Long replyToMemberId, String content) {
        ReviewReply reply = reviewCommandService.createReply(
            new CreateReviewReplyCommand(commentId, memberId, replyToMemberId, content)
        );
        List<Long> ids = replyToMemberId != null ? List.of(memberId, replyToMemberId) : List.of(memberId);
        Map<Long, MemberWithProfileImageResult> memberMap = reviewQueryService.findMemberWithProfileImagesByIds(ids);
        return convertToReplyResponse(reply, memberMap.get(memberId), replyToMemberId != null ? memberMap.get(replyToMemberId) : null);
    }

    @Transactional(readOnly = true)
    public CommentListResponse searchCommentsWithReplies(Long reviewId) {
        List<ReviewComment> comments = reviewQueryService.findCommentsByReviewId(reviewId);

        if (comments.isEmpty()) {
            return CommentListResponse.from(List.of(), 0);
        }

        List<Long> commentIds = comments.stream()
            .map(ReviewComment::getId)
            .toList();

        List<ReviewReply> allReplies = reviewQueryService.findRepliesByCommentIds(commentIds);

        Map<Long, List<ReviewReply>> repliesByCommentId = allReplies.stream()
            .collect(Collectors.groupingBy(ReviewReply::getCommentId));

        List<Long> memberIds = new ArrayList<>();
        comments.forEach(c -> memberIds.add(c.getMemberId()));
        allReplies.forEach(r -> {
            memberIds.add(r.getMemberId());
            if (r.getReplyToMemberId() != null) {
                memberIds.add(r.getReplyToMemberId());
            }
        });

        Map<Long, MemberWithProfileImageResult> memberMap = reviewQueryService.findMemberWithProfileImagesByIds(memberIds);

        List<CommentResponse> commentResponses = comments.stream()
            .map(comment -> {
                MemberWithProfileImageResult member = memberMap.get(comment.getMemberId());
                List<ReviewReply> replies = repliesByCommentId.getOrDefault(comment.getId(), List.of());
                List<ReplyResponse> replyResponses = replies.stream()
                    .map(reply -> convertToReplyResponse(
                        reply,
                        memberMap.get(reply.getMemberId()),
                        reply.getReplyToMemberId() != null ? memberMap.get(reply.getReplyToMemberId()) : null
                    ))
                    .toList();
                return convertToCommentResponse(comment, member, replyResponses);
            })
            .toList();

        int totalCount = comments.size() + allReplies.size();
        return CommentListResponse.from(commentResponses, totalCount);
    }

    private CommentResponse convertToCommentResponse(ReviewComment comment, MemberWithProfileImageResult member, List<ReplyResponse> replies) {
        return CommentResponse.from(
            comment.getId(),
            comment.getReviewId(),
            comment.getMemberId(),
            member != null ? member.nickname() : null,
            member != null ? fileService.getUrlByPath(member.profileImageFilePath()) : null,
            comment.getContent(),
            comment.getCreatedAt(),
            replies
        );
    }

    private ReplyResponse convertToReplyResponse(ReviewReply reply, MemberWithProfileImageResult member, MemberWithProfileImageResult replyToMember) {
        return ReplyResponse.from(
            reply.getId(),
            reply.getCommentId(),
            reply.getMemberId(),
            member != null ? member.nickname() : null,
            member != null ? fileService.getUrlByPath(member.profileImageFilePath()) : null,
            reply.getReplyToMemberId(),
            replyToMember != null ? replyToMember.nickname() : null,
            reply.getContent(),
            reply.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Optional<ReviewProductResponse> findReviewProduct(Long reviewId) {
        Optional<ReviewDetailResult> reviewDetailOpt = reviewQueryService.findReviewDetail(reviewId);
        if (reviewDetailOpt.isEmpty()) {
            return Optional.empty();
        }

        ReviewDetailResult reviewDetail = reviewDetailOpt.get();
        Review review = reviewQueryService.findById(reviewId);

        List<String> reviewImageUrls = reviewDetail.imageUrls() == null ? List.of() :
            reviewDetail.imageUrls().stream()
                .map(fileService::getUrlByPath)
                .toList();
        String reviewMemberProfileImageUrl = fileService.getUrlByPath(reviewDetail.memberProfileImageUrl());

        return productQueryService.findProductById(review.getProductId())
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
                    reviewDetail.memberId(),
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
                    reviewDetail.memberId(),
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
        OrderProduct orderProduct = orderQueryService.findOrderProductById(orderProductId);

        Product product = productQueryService.findProductById(orderProduct.getProductId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND));

        Integer price = product.getDiscountPrice() != null
                ? product.getDiscountPrice()
                : product.getOriginalPrice();

        boolean isReviewed = reviewQueryService.isReviewedByOrderAndProduct(
            orderProduct.getOrderId(), orderProduct.getProductId(), memberId
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
            OrderProduct orderProduct = orderQueryService.findOrderProductById(request.orderProductId());
            orderId = orderProduct.getOrderId();
        }

        Product product = productQueryService.findProductById(request.productId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND));

        ReviewResult result = reviewCommandService.createReview(new CreateReviewCommand(
            product.getShopId(),
            product.getId(),
            memberId,
            request.orderProductId(),
            orderId,
            request.tasteRating(),
            request.amountRating(),
            request.priceRating(),
            request.content(),
            request.uploadedFileIds(),
            request.tags()
        ));

        return ReviewResponse.from(
            result.id(),
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
        ReviewResult result = reviewCommandService.updateReview(new UpdateReviewCommand(
            reviewId,
            memberId,
            request.tasteRating(),
            request.amountRating(),
            request.priceRating(),
            request.content(),
            request.uploadedFileIds(),
            request.tags()
        ));

        return ReviewResponse.from(
            result.id(),
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
        Review review = reviewQueryService.findById(reviewId);
        reviewCommandService.deleteReview(new DeleteReviewCommand(reviewId, memberId, review.getProductId()));
    }

    @Transactional(readOnly = true)
    public PageResponse<MemberReviewListItemResponse> findMemberReviews(Long memberId, int page, int size) {
        return PageResponse.from(reviewQueryService.findReviewsByMemberId(memberId, page, size))
            .map(dto -> MemberReviewListItemResponse.from(
                dto.id(),
                fileService.getUrlByPath(dto.imageUrl())
            ));
    }
}
