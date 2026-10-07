package com.tastyhouse.application.review.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.review.model.ReviewListTab;
import com.tastyhouse.domain.review.model.ReviewOwnerReply;
import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.application.review.port.in.ShopReviewListQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewSortSpec;
import com.tastyhouse.application.review.port.out.ShopReviewDisplaySettingOwnerQueryPort;
import com.tastyhouse.application.review.port.out.ShopReviewListItemViewResult;
import com.tastyhouse.application.review.port.out.ShopReviewManagementListItemResult;
import com.tastyhouse.application.review.port.out.ShopReviewManagementQueryPort;
import com.tastyhouse.application.review.port.out.ShopReviewManagementSearchCondition;
import com.tastyhouse.application.review.port.out.ShopReviewReplyWindow;
import com.tastyhouse.application.review.port.out.ShopReviewTabFilter;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional(readOnly = true)
class ShopReviewListQueryService implements ShopReviewListQueryUseCase {

    private final ShopReviewManagementQueryPort shopReviewManagementQueryPort;
    private final ShopReviewDisplaySettingOwnerQueryPort shopReviewDisplaySettingOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopReviewListQueryService(
        ShopReviewManagementQueryPort shopReviewManagementQueryPort,
        ShopReviewDisplaySettingOwnerQueryPort shopReviewDisplaySettingOwnerQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopReviewManagementQueryPort = shopReviewManagementQueryPort;
        this.shopReviewDisplaySettingOwnerQueryPort = shopReviewDisplaySettingOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public PageResult<ShopReviewListItemViewResult> getReviews(
        Long ceoId,
        Long shopId,
        String tab,
        LocalDate startDate,
        LocalDate endDate,
        Integer rating,
        String orderMethod,
        Boolean hasImage,
        String sortType,
        int page,
        int size
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        validateDateRange(startDate, endDate);

        ShopReviewTabFilter tabFilter = ShopReviewTabFilters.of(tab == null ? ReviewListTab.ALL : ReviewListTab.from(tab));
        String orderMethodFilter = orderMethod == null ? null : OrderMethod.from(orderMethod).name();

        ShopReviewManagementSearchCondition condition = ShopReviewManagementSearchCondition.of(
            shopId,
            tabFilter,
            startDate,
            endDate,
            rating,
            orderMethodFilter,
            hasImage,
            resolveSortType(shopId, sortType)
        );
        PageQuery pageQuery = PageQuery.of(page, size);

        return shopReviewManagementQueryPort.findShopReviews(condition, pageQuery)
            .map(result -> result.withDescriptions(
                orderMethodDisplayName(result.orderMethod()),
                blindStatusDescription(result.blindRequestStatus())
            ))
            .map(this::toListItemViewResult);
    }

    private ReviewSortSpec resolveSortType(Long shopId, String sortType) {
        if (sortType != null) {
            return ReviewSortSpecs.of(ReviewSortType.from(sortType));
        }
        ReviewSortType storedSortType = shopReviewDisplaySettingOwnerQueryPort.findSortTypeByShopId(shopId)
            .map(ReviewSortType::valueOf)
            .orElse(ReviewSortType.LATEST);
        return ReviewSortSpecs.of(storedSortType);
    }

    private static String orderMethodDisplayName(String orderMethod) {
        return orderMethod == null ? null : OrderMethod.valueOf(orderMethod).getDisplayName();
    }

    private static String blindStatusDescription(String status) {
        return status == null ? null : ReviewBlindStatus.valueOf(status).getDescription();
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ApplicationException(CeoErrorCode.REVIEW_DATE_RANGE_INVALID);
        }
    }

    private ShopReviewListItemViewResult toListItemViewResult(ShopReviewManagementListItemResult result) {
        return new ShopReviewListItemViewResult(result, toReplyWindow(result.createdAt()));
    }

    private ShopReviewReplyWindow toReplyWindow(LocalDateTime reviewCreatedAt) {
        LocalDate replyDeadline = reviewCreatedAt.toLocalDate().plusDays(ReviewOwnerReply.REPLY_PERIOD_DAYS);
        return new ShopReviewReplyWindow(replyDeadline, !LocalDate.now().isAfter(replyDeadline));
    }
}
