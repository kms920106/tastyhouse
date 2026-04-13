package com.tastyhouse.webapi.review;

import com.tastyhouse.core.entity.order.OrderItem;
import com.tastyhouse.core.entity.place.Tag;
import com.tastyhouse.core.entity.product.Product;
import com.tastyhouse.core.entity.review.Review;
import com.tastyhouse.core.entity.review.ReviewComment;
import com.tastyhouse.core.entity.review.ReviewImage;
import com.tastyhouse.core.entity.review.ReviewReply;
import com.tastyhouse.core.entity.review.ReviewTag;
import com.tastyhouse.core.entity.review.dto.BestReviewListItemDto;
import com.tastyhouse.core.entity.review.dto.LatestReviewListItemDto;
import com.tastyhouse.core.entity.review.dto.MyReviewListItemDto;
import com.tastyhouse.core.entity.review.dto.ReviewDetailDto;
import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.exception.AccessDeniedException;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.service.OrderCoreService;
import com.tastyhouse.core.service.ProductCoreService;
import com.tastyhouse.core.service.ReviewCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.review.request.ReviewCreateRequest;
import com.tastyhouse.webapi.review.request.ReviewType;
import com.tastyhouse.webapi.review.request.ReviewUpdateRequest;
import com.tastyhouse.webapi.member.response.MyReviewListItemResponse;
import com.tastyhouse.webapi.review.response.BestReviewListItem;
import com.tastyhouse.webapi.review.response.CommentListResponse;
import com.tastyhouse.webapi.review.response.CommentResponse;
import com.tastyhouse.webapi.review.response.LatestReviewListItem;
import com.tastyhouse.webapi.review.response.ReplyResponse;
import com.tastyhouse.webapi.review.response.ReviewDetailResponse;
import com.tastyhouse.webapi.review.response.ReviewLikeStatusResponse;
import com.tastyhouse.webapi.review.response.ReviewProductResponse;
import com.tastyhouse.webapi.review.response.ReviewResponse;
import com.tastyhouse.webapi.review.response.ReviewWriteInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final FileService fileService;
    private final ReviewCoreService reviewCoreService;
    private final ProductCoreService productCoreService;
    private final OrderCoreService orderCoreService;

    @Transactional(readOnly = true)
    public PageResult<BestReviewListItem> searchBestReviewList(PageRequest pageRequest) {
        return reviewCoreService.findBestReviewsWithPagination(
            pageRequest.page(), pageRequest.size()
        ).map(this::convertToBestReviewListItem);
    }

    private BestReviewListItem convertToBestReviewListItem(BestReviewListItemDto dto) {
        return BestReviewListItem.from(
            dto.id(),
            fileService.getUrlByPath(dto.imageUrl()),
            dto.stationName(),
            dto.totalRating(),
            dto.content()
        );
    }

    @Transactional(readOnly = true)
    public PageResult<LatestReviewListItem> searchLatestReviewList(
        PageRequest pageRequest,
        ReviewType type,
        Long memberId
    ) {
        PageResult<LatestReviewListItemDto> coreResult;

        if (type == ReviewType.FOLLOWING && memberId != null) {
            coreResult = reviewCoreService.findLatestReviewsByFollowingWithPagination(
                memberId,
                pageRequest.page(),
                pageRequest.size()
            );
        } else {
            coreResult = reviewCoreService.findLatestReviewsWithPagination(
                pageRequest.page(),
                pageRequest.size()
            );
        }

        return coreResult.map(this::convertToLatestReviewListItem);
    }

    private LatestReviewListItem convertToLatestReviewListItem(LatestReviewListItemDto dto) {
        List<String> imageUrls = dto.imageUrls() == null ? List.of() :
            dto.imageUrls().stream()
                .map(fileService::getUrlByPath)
                .toList();

        return LatestReviewListItem.from(
            dto.id(), imageUrls, dto.stationName(), dto.totalRating(), dto.content(),
            dto.memberId(), dto.memberNickname(),
            fileService.getUrlByPath(dto.memberProfileImageUrl()),
            dto.createdAt(), dto.likeCount(), dto.commentCount()
        );
    }

    @Transactional(readOnly = true)
    public Optional<ReviewDetailResponse> findReviewDetail(Long reviewId) {
        return reviewCoreService.findReviewDetail(reviewId)
            .map(this::convertToReviewDetailResponse);
    }

    private ReviewDetailResponse convertToReviewDetailResponse(ReviewDetailDto dto) {
        List<String> imageUrls = dto.imageUrls() == null ? List.of() :
            dto.imageUrls().stream()
                .map(fileService::getUrlByPath)
                .toList();

        return ReviewDetailResponse.from(
            dto.id(),
            dto.placeId(),
            dto.placeName(),
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
        boolean isLiked = reviewCoreService.isLikedByMember(reviewId, memberId);
        return ReviewLikeStatusResponse.from(isLiked);
    }

    @Transactional
    public boolean toggleReviewLike(Long reviewId, Long memberId) {
        return reviewCoreService.toggleReviewLike(reviewId, memberId);
    }

    @Transactional
    public CommentResponse createComment(Long reviewId, Long memberId, String content) {
        ReviewComment comment = reviewCoreService.createComment(reviewId, memberId, content);
        Member member = reviewCoreService.findMemberById(memberId);
        return convertToCommentResponse(comment, member, List.of());
    }

    @Transactional
    public ReplyResponse createReply(Long commentId, Long memberId, Long replyToMemberId, String content) {
        ReviewReply reply = reviewCoreService.createReply(commentId, memberId, replyToMemberId, content);
        Member member = reviewCoreService.findMemberById(memberId);
        Member replyToMember = replyToMemberId != null ? reviewCoreService.findMembersByIds(List.of(replyToMemberId)).get(replyToMemberId) : null;
        return convertToReplyResponse(reply, member, replyToMember);
    }

    @Transactional(readOnly = true)
    public CommentListResponse searchCommentsWithReplies(Long reviewId) {
        List<ReviewComment> comments = reviewCoreService.findCommentsByReviewId(reviewId);

        if (comments.isEmpty()) {
            return CommentListResponse.from(List.of(), 0);
        }

        List<Long> commentIds = comments.stream()
            .map(ReviewComment::getId)
            .toList();

        List<ReviewReply> allReplies = reviewCoreService.findRepliesByCommentIds(commentIds);

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

        Map<Long, Member> memberMap = reviewCoreService.findMembersByIds(memberIds);

        List<CommentResponse> commentResponses = comments.stream()
            .map(comment -> {
                Member member = memberMap.get(comment.getMemberId());
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
        return CommentListResponse.from(
            commentResponses,
            totalCount
        );
    }

    private CommentResponse convertToCommentResponse(ReviewComment comment, Member member, List<ReplyResponse> replies) {
        String memberProfileImageUrl = null;
        if (member != null && member.getProfileImageFileId() != null) {
            memberProfileImageUrl = fileService.getFileUrl(member.getProfileImageFileId());
        }

        return CommentResponse.from(
            comment.getId(),
            comment.getReviewId(),
            comment.getMemberId(),
            member != null ? member.getNickname() : null,
            memberProfileImageUrl,
            comment.getContent(),
            comment.getCreatedAt(),
            replies
        );
    }

    private ReplyResponse convertToReplyResponse(ReviewReply reply, Member member, Member replyToMember) {
        String memberProfileImageUrl = null;
        if (member != null && member.getProfileImageFileId() != null) {
            memberProfileImageUrl = fileService.getFileUrl(member.getProfileImageFileId());
        }

        return ReplyResponse.from(
            reply.getId(),
            reply.getCommentId(),
            reply.getMemberId(),
            member != null ? member.getNickname() : null,
            memberProfileImageUrl,
            reply.getReplyToMemberId(),
            replyToMember != null ? replyToMember.getNickname() : null,
            reply.getContent(),
            reply.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Optional<ReviewProductResponse> findReviewProduct(Long reviewId) {
        Optional<ReviewDetailDto> reviewDetailOpt = reviewCoreService.findReviewDetail(reviewId);
        if (reviewDetailOpt.isEmpty()) {
            return Optional.empty();
        }

        ReviewDetailDto reviewDetail = reviewDetailOpt.get();
        Review review = reviewCoreService.findById(reviewId);

        List<String> reviewImageUrls = reviewDetail.imageUrls() == null ? List.of() :
            reviewDetail.imageUrls().stream()
                .map(fileService::getUrlByPath)
                .toList();
        String reviewMemberProfileImageUrl = fileService.getUrlByPath(reviewDetail.memberProfileImageUrl());

        return productCoreService.findProductById(review.getProductId())
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
                    null,
                    null,
                    null,
                    null,
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
        return productCoreService.getFirstImageUrl(productId);
    }

    @Transactional(readOnly = true)
    public ReviewWriteInfoResponse getReviewWriteInfo(Long orderItemId, Long memberId) {
        OrderItem orderItem = orderCoreService.findOrderItemById(orderItemId);

        Product product = productCoreService.findProductById(orderItem.getProductId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND));

        Integer price = product.getDiscountPrice() != null
                ? product.getDiscountPrice()
                : product.getOriginalPrice();

        boolean isReviewed = reviewCoreService.isReviewedByOrderAndProduct(
                orderItem.getOrderId(), orderItem.getProductId(), memberId);

        return ReviewWriteInfoResponse.from(
            product.getId(),
            product.getName(),
            getFirstImageUrl(product.getId()),
            price,
            orderItem.getOrderId(),
            isReviewed
        );
    }

    @Transactional
    public ReviewResponse createReview(Long memberId, ReviewCreateRequest request) {
        Long orderId = null;
        if (request.orderItemId() != null) {
            OrderItem orderItem = orderCoreService.findOrderItemById(request.orderItemId());

            orderId = orderItem.getOrderId();

            if (reviewCoreService.isReviewedByOrderAndProduct(orderId, request.productId(), memberId)) {
                throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
            }
        }

        Product product = productCoreService.findProductById(request.productId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND));

        double totalRating = (request.tasteRating() + request.amountRating() + request.priceRating()) / 3.0;

        Review review =
            Review.of(
                product.getPlaceId(),
                product.getId(),
                memberId,
                request.content(),
                Math.round(totalRating * 10.0) / 10.0,
                request.tasteRating().doubleValue(),
                request.amountRating().doubleValue(),
                request.priceRating().doubleValue(),
                null,
                null,
                null,
                null,
                orderId
            );

        Review savedReview = reviewCoreService.saveReview(review);

        List<Long> savedUploadedFileIds = saveReviewImages(savedReview.getId(), request.uploadedFileIds());
        List<String> tags = saveReviewTags(savedReview.getId(), request.tags());

        return ReviewResponse.from(
            savedReview.getId(),
            savedReview.getProductId(),
            savedReview.getTasteRating(),
            savedReview.getAmountRating(),
            savedReview.getPriceRating(),
            savedReview.getTotalRating(),
            savedReview.getContent(),
            savedUploadedFileIds,
            tags,
            savedReview.getCreatedAt()
        );
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long memberId, ReviewUpdateRequest request) {
        Review review = reviewCoreService.findReviewByIdAndMemberId(reviewId, memberId)
                .orElseThrow(() -> new AccessDeniedException(ErrorCode.REVIEW_ACCESS_DENIED));

        double totalRating = (request.tasteRating() + request.amountRating() + request.priceRating()) / 3.0;

        review.updateContent(
                request.content(),
                Math.round(totalRating * 10.0) / 10.0,
                request.tasteRating().doubleValue(),
                request.amountRating().doubleValue(),
                request.priceRating().doubleValue(),
                null, null, null, null
        );

        reviewCoreService.deleteReviewImages(reviewId);
        reviewCoreService.deleteReviewTags(reviewId);

        List<Long> savedUploadedFileIds = saveReviewImages(reviewId, request.uploadedFileIds());
        List<String> tags = saveReviewTags(reviewId, request.tags());

        return ReviewResponse.from(
            review.getId(),
            review.getProductId(),
            review.getTasteRating(),
            review.getAmountRating(),
            review.getPriceRating(),
            review.getTotalRating(),
            review.getContent(),
            savedUploadedFileIds,
            tags,
            review.getCreatedAt()
        );
    }

    @Transactional
    public void deleteReview(Long reviewId, Long memberId) {
        reviewCoreService.findReviewByIdAndMemberId(reviewId, memberId)
                .orElseThrow(() -> new AccessDeniedException(ErrorCode.REVIEW_ACCESS_DENIED));

        reviewCoreService.deleteReviewImages(reviewId);
        reviewCoreService.deleteReviewTags(reviewId);
        reviewCoreService.deleteReview(reviewId);
    }

    private List<Long> saveReviewImages(Long reviewId, List<Long> uploadedFileIds) {
        if (uploadedFileIds == null || uploadedFileIds.isEmpty()) {
            return List.of();
        }
        List<ReviewImage> images = new ArrayList<>();
        for (int i = 0; i < uploadedFileIds.size(); i++) {
            Long fileId = uploadedFileIds.get(i);
            if (fileId == null) {
                throw new EntityNotFoundException(ErrorCode.FILE_NOT_FOUND);
            }
            images.add(
                ReviewImage.of(
                    reviewId,
                    fileId,
                    i + 1
                )
            );
        }
        reviewCoreService.saveReviewImages(images);
        return uploadedFileIds;
    }

    @Transactional(readOnly = true)
    public PageResult<MyReviewListItemResponse> findMemberReviews(Long memberId, PageRequest pageRequest) {
        PageResult<MyReviewListItemDto> coreResult = reviewCoreService.findReviewsByMemberId(
            memberId, pageRequest.page(), pageRequest.size()
        );
        return coreResult.map(MyReviewListItemResponse::from);
    }

    private List<String> saveReviewTags(Long reviewId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }
        List<ReviewTag> reviewTags = tagNames.stream()
                .map(tagName -> {
                    Tag tag = reviewCoreService.findOrCreateTag(tagName);
                    return new ReviewTag(reviewId, tag.getId());
                })
                .toList();
        reviewCoreService.saveReviewTags(reviewTags);
        return tagNames;
    }
}
