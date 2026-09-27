package com.tastyhouse.application.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.product.service.CupDepositPolicy;
import com.tastyhouse.domain.product.service.ProductExposureCalculator;
import com.tastyhouse.domain.product.service.StorePriceBadgePolicy;
import com.tastyhouse.application.product.port.out.ProductReviewStatisticsPort;
import com.tastyhouse.application.product.port.out.ShopRequestIndexSyncPort;
import com.tastyhouse.application.product.port.out.StorePriceVerificationPort;
import com.tastyhouse.application.product.port.out.write.ProductAllergenRepository;
import com.tastyhouse.application.product.port.out.write.ProductBbqRepository;
import com.tastyhouse.application.product.port.out.write.ProductCategoryRepository;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupLinkRepository;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupRepository;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionRepository;
import com.tastyhouse.application.product.port.out.write.ProductExposureHourRepository;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackReadRepository;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackRepository;
import com.tastyhouse.application.product.port.out.write.ProductImageChangeRequestRepository;
import com.tastyhouse.application.product.port.out.write.ProductImageRepository;
import com.tastyhouse.application.product.port.out.write.ProductNutritionRepository;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLinkRepository;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeHistoryRepository;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupRepository;
import com.tastyhouse.application.product.port.out.write.ProductOptionRepository;
import com.tastyhouse.application.product.port.out.write.ProductPriceRepository;
import com.tastyhouse.application.product.port.out.write.ProductRepository;
import com.tastyhouse.application.product.port.out.write.ProductRepresentativeRequestRepository;
import com.tastyhouse.application.product.port.out.write.ProductShopLinkRepository;
import com.tastyhouse.application.product.port.out.write.ProductVegetarianRequestRepository;
import com.tastyhouse.application.product.port.out.write.StorePriceVerificationRepository;
import com.tastyhouse.application.product.service.OrderProductValidationService;
import com.tastyhouse.application.product.service.ProductAvailabilityService;
import com.tastyhouse.application.product.service.ProductDeletionService;
import com.tastyhouse.application.product.service.ProductExposureService;
import com.tastyhouse.application.product.service.ProductFeedbackService;
import com.tastyhouse.application.product.service.ProductImageApprovalService;
import com.tastyhouse.application.product.service.ProductNutritionService;
import com.tastyhouse.application.product.service.ProductOptionGroupLinkService;
import com.tastyhouse.application.product.service.ProductOptionGroupMergeService;
import com.tastyhouse.application.product.service.ProductPriceService;
import com.tastyhouse.application.product.service.ProductRegistrationService;
import com.tastyhouse.application.product.service.ProductRepresentativeApprovalService;
import com.tastyhouse.application.product.service.ProductReviewStatsService;
import com.tastyhouse.application.product.service.ProductShopLinkService;
import com.tastyhouse.application.product.service.ProductSortService;
import com.tastyhouse.application.product.service.ProductVegetarianApprovalService;
import com.tastyhouse.application.product.service.StorePriceVerificationService;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class ProductServiceConfig {
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
