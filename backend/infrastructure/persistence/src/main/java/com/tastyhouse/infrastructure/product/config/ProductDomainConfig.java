package com.tastyhouse.infrastructure.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.product.port.ProductReviewStatisticsPort;
import com.tastyhouse.domain.product.port.ShopRequestIndexSyncPort;
import com.tastyhouse.domain.product.port.StorePriceVerificationPort;
import com.tastyhouse.domain.product.repository.ProductBbqRepository;
import com.tastyhouse.domain.product.repository.ProductCategoryRepository;
import com.tastyhouse.domain.product.repository.ProductCommonOptionGroupLinkRepository;
import com.tastyhouse.domain.product.repository.ProductExposureHourRepository;
import com.tastyhouse.domain.product.repository.ProductFeedbackReadRepository;
import com.tastyhouse.domain.product.repository.ProductFeedbackRepository;
import com.tastyhouse.domain.product.repository.ProductImageChangeRequestRepository;
import com.tastyhouse.domain.product.repository.ProductRepresentativeRequestRepository;
import com.tastyhouse.domain.product.repository.ProductVegetarianRequestRepository;
import com.tastyhouse.domain.product.repository.ProductCommonOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductCommonOptionRepository;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupLinkRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductAllergenRepository;
import com.tastyhouse.domain.product.repository.ProductNutritionRepository;
import com.tastyhouse.domain.product.repository.ProductPriceRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.repository.ProductShopLinkRepository;
import com.tastyhouse.domain.product.repository.StorePriceVerificationRepository;
import com.tastyhouse.domain.product.service.CupDepositPolicy;
import com.tastyhouse.domain.product.service.OrderProductValidationService;
import com.tastyhouse.domain.product.service.ProductAvailabilityService;
import com.tastyhouse.domain.product.service.ProductDeletionService;
import com.tastyhouse.domain.product.service.ProductExposureCalculator;
import com.tastyhouse.domain.product.service.ProductExposureService;
import com.tastyhouse.domain.product.service.ProductFeedbackService;
import com.tastyhouse.domain.product.service.ProductImageApprovalService;
import com.tastyhouse.domain.product.service.ProductNutritionService;
import com.tastyhouse.domain.product.service.ProductPriceService;
import com.tastyhouse.domain.product.service.ProductRepresentativeApprovalService;
import com.tastyhouse.domain.product.service.ProductVegetarianApprovalService;
import com.tastyhouse.domain.product.repository.ProductOptionGroupMergeHistoryRepository;
import com.tastyhouse.domain.product.service.ProductOptionGroupLinkService;
import com.tastyhouse.domain.product.service.ProductOptionGroupMergeService;
import com.tastyhouse.domain.product.service.ProductShopLinkService;
import com.tastyhouse.domain.product.service.ProductSortService;
import com.tastyhouse.domain.product.service.ProductRegistrationService;
import com.tastyhouse.domain.product.service.ProductReviewStatsService;
import com.tastyhouse.domain.product.service.StorePriceBadgePolicy;
import com.tastyhouse.domain.product.service.StorePriceVerificationService;

@Configuration(proxyBeanMethods = false)
public class ProductDomainConfig {
    @Bean
    public ProductRegistrationService productRegistrationService(
        ProductRepository productRepository,
        ProductCategoryRepository productCategoryRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductImageRepository productImageRepository,
        ProductBbqRepository productBbqRepository,
        ProductOptionGroupLinkRepository productOptionGroupLinkRepository,
        ProductShopLinkRepository productShopLinkRepository
    ) {
        return new ProductRegistrationService(
            productRepository,
            productCategoryRepository,
            productOptionGroupRepository,
            productOptionRepository,
            productImageRepository,
            productBbqRepository,
            productOptionGroupLinkRepository,
            productShopLinkRepository
        );
    }

    @Bean
    public ProductReviewStatsService productReviewStatsService(
        ProductRepository productRepository,
        ProductReviewStatisticsPort productReviewStatisticsPort
    ) {
        return new ProductReviewStatsService(productRepository, productReviewStatisticsPort);
    }

    @Bean
    public OrderProductValidationService orderProductValidationService(
        ProductRepository productRepository,
        ProductPriceRepository productPriceRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductImageRepository productImageRepository,
        ProductOptionGroupLinkRepository productOptionGroupLinkRepository,
        ProductExposureHourRepository productExposureHourRepository,
        ProductExposureCalculator productExposureCalculator,
        CupDepositPolicy cupDepositPolicy
    ) {
        return new OrderProductValidationService(
            productRepository,
            productPriceRepository,
            productOptionGroupRepository,
            productOptionRepository,
            productImageRepository,
            productOptionGroupLinkRepository,
            productExposureHourRepository,
            productExposureCalculator,
            cupDepositPolicy
        );
    }

    @Bean
    public ProductAvailabilityService productAvailabilityService(
        ProductRepository productRepository,
        ProductOptionRepository productOptionRepository,
        ProductCommonOptionRepository productCommonOptionRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductCommonOptionGroupRepository productCommonOptionGroupRepository,
        ProductOptionGroupLinkRepository productOptionGroupLinkRepository,
        ProductCommonOptionGroupLinkRepository productCommonOptionGroupLinkRepository
    ) {
        return new ProductAvailabilityService(
            productRepository,
            productOptionRepository,
            productCommonOptionRepository,
            productOptionGroupRepository,
            productCommonOptionGroupRepository,
            productOptionGroupLinkRepository,
            productCommonOptionGroupLinkRepository
        );
    }

    @Bean
    public ProductDeletionService productDeletionService(ProductRepository productRepository) {
        return new ProductDeletionService(productRepository);
    }

    @Bean
    public ProductSortService productSortService(
        ProductRepository productRepository,
        ProductCategoryRepository productCategoryRepository
    ) {
        return new ProductSortService(productRepository, productCategoryRepository);
    }

    @Bean
    public ProductOptionGroupLinkService productOptionGroupLinkService(
        ProductOptionGroupLinkRepository productOptionGroupLinkRepository,
        ProductRepository productRepository
    ) {
        return new ProductOptionGroupLinkService(productOptionGroupLinkRepository, productRepository);
    }

    @Bean
    public ProductOptionGroupMergeService productOptionGroupMergeService(
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductOptionGroupLinkRepository productOptionGroupLinkRepository,
        ProductOptionGroupLinkService productOptionGroupLinkService,
        ProductOptionGroupMergeHistoryRepository productOptionGroupMergeHistoryRepository
    ) {
        return new ProductOptionGroupMergeService(
            productOptionGroupRepository,
            productOptionRepository,
            productOptionGroupLinkRepository,
            productOptionGroupLinkService,
            productOptionGroupMergeHistoryRepository
        );
    }

    @Bean
    public ProductExposureCalculator productExposureCalculator() {
        return new ProductExposureCalculator();
    }

    @Bean
    public CupDepositPolicy cupDepositPolicy() {
        return new CupDepositPolicy();
    }

    @Bean
    public ProductExposureService productExposureService(
        ProductRepository productRepository,
        ProductExposureHourRepository productExposureHourRepository,
        ProductExposureCalculator productExposureCalculator
    ) {
        return new ProductExposureService(
            productRepository,
            productExposureHourRepository,
            productExposureCalculator
        );
    }

    @Bean
    public ProductImageApprovalService productImageApprovalService(
        ProductImageChangeRequestRepository productImageChangeRequestRepository,
        ProductImageRepository productImageRepository,
        ProductRepository productRepository
    ) {
        return new ProductImageApprovalService(
            productImageChangeRequestRepository,
            productImageRepository,
            productRepository
        );
    }

    @Bean
    public ProductNutritionService productNutritionService(
        ProductNutritionRepository productNutritionRepository,
        ProductAllergenRepository productAllergenRepository,
        ProductRepository productRepository
    ) {
        return new ProductNutritionService(
            productNutritionRepository,
            productAllergenRepository,
            productRepository
        );
    }

    @Bean
    public ProductVegetarianApprovalService productVegetarianApprovalService(
        ProductVegetarianRequestRepository productVegetarianRequestRepository,
        ProductRepository productRepository
    ) {
        return new ProductVegetarianApprovalService(
            productVegetarianRequestRepository,
            productRepository
        );
    }

    @Bean
    public ProductRepresentativeApprovalService productRepresentativeApprovalService(
        ProductRepresentativeRequestRepository productRepresentativeRequestRepository,
        ProductRepository productRepository,
        ProductImageRepository productImageRepository
    ) {
        return new ProductRepresentativeApprovalService(
            productRepresentativeRequestRepository,
            productRepository,
            productImageRepository
        );
    }

    @Bean
    public ProductPriceService productPriceService(
        ProductPriceRepository productPriceRepository,
        ProductRepository productRepository,
        StorePriceVerificationPort storePriceVerificationPort
    ) {
        return new ProductPriceService(
            productPriceRepository,
            productRepository,
            storePriceVerificationPort
        );
    }

    @Bean
    public StorePriceVerificationService storePriceVerificationService(
        StorePriceVerificationRepository storePriceVerificationRepository,
        ProductPriceRepository productPriceRepository,
        ProductRepository productRepository,
        StorePriceVerificationPort storePriceVerificationPort,
        ShopRequestIndexSyncPort shopRequestIndexSyncPort
    ) {
        return new StorePriceVerificationService(
            storePriceVerificationRepository,
            productPriceRepository,
            productRepository,
            storePriceVerificationPort,
            shopRequestIndexSyncPort
        );
    }

    @Bean
    public StorePriceBadgePolicy storePriceBadgePolicy() {
        return new StorePriceBadgePolicy();
    }

    @Bean
    public ProductFeedbackService productFeedbackService(
        ProductRepository productRepository,
        ProductFeedbackRepository productFeedbackRepository,
        ProductFeedbackReadRepository productFeedbackReadRepository
    ) {
        return new ProductFeedbackService(
            productRepository,
            productFeedbackRepository,
            productFeedbackReadRepository
        );
    }

    @Bean
    public ProductShopLinkService productShopLinkService(
        ProductRepository productRepository,
        ProductShopLinkRepository productShopLinkRepository,
        ProductCategoryRepository productCategoryRepository
    ) {
        return new ProductShopLinkService(
            productRepository,
            productShopLinkRepository,
            productCategoryRepository
        );
    }
}
