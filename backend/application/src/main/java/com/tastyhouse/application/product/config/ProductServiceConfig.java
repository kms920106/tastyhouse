package com.tastyhouse.application.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.product.service.CupDepositPolicy;
import com.tastyhouse.domain.product.service.ProductExposureCalculator;
import com.tastyhouse.domain.product.service.StorePriceBadgePolicy;
import com.tastyhouse.application.product.port.out.ProductReviewStatisticsPort;
import com.tastyhouse.application.product.port.out.ShopRequestIndexSyncPort;
import com.tastyhouse.application.product.port.out.StorePriceVerificationPort;
import com.tastyhouse.application.product.port.out.write.ProductAllergenPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductBbqPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductCategoryPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupLinkPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductExposureHourPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackReadPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductImageChangeRequestPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductImagePersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductNutritionPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLinkPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeHistoryPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPricePersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductRepresentativeRequestPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductShopLinkPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductVegetarianRequestPersistencePort;
import com.tastyhouse.application.product.port.out.write.StorePriceVerificationPersistencePort;
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
        ProductPersistencePort productPersistencePort,
        ProductCategoryPersistencePort productCategoryPersistencePort,
        ProductOptionGroupPersistencePort productOptionGroupPersistencePort,
        ProductOptionPersistencePort productOptionPersistencePort,
        ProductImagePersistencePort productImagePersistencePort,
        ProductBbqPersistencePort productBbqPersistencePort,
        ProductOptionGroupLinkPersistencePort productOptionGroupLinkPersistencePort,
        ProductShopLinkPersistencePort productShopLinkPersistencePort
    ) {
        return new ProductRegistrationService(
            productPersistencePort,
            productCategoryPersistencePort,
            productOptionGroupPersistencePort,
            productOptionPersistencePort,
            productImagePersistencePort,
            productBbqPersistencePort,
            productOptionGroupLinkPersistencePort,
            productShopLinkPersistencePort
        );
    }

    @Bean
    public ProductReviewStatsService productReviewStatsService(
        ProductPersistencePort productPersistencePort,
        ProductReviewStatisticsPort productReviewStatisticsPort
    ) {
        return new ProductReviewStatsService(productPersistencePort, productReviewStatisticsPort);
    }

    @Bean
    public OrderProductValidationService orderProductValidationService(
        ProductPersistencePort productPersistencePort,
        ProductPricePersistencePort productPricePersistencePort,
        ProductOptionGroupPersistencePort productOptionGroupPersistencePort,
        ProductOptionPersistencePort productOptionPersistencePort,
        ProductImagePersistencePort productImagePersistencePort,
        ProductOptionGroupLinkPersistencePort productOptionGroupLinkPersistencePort,
        ProductExposureHourPersistencePort productExposureHourPersistencePort,
        ProductExposureCalculator productExposureCalculator,
        CupDepositPolicy cupDepositPolicy
    ) {
        return new OrderProductValidationService(
            productPersistencePort,
            productPricePersistencePort,
            productOptionGroupPersistencePort,
            productOptionPersistencePort,
            productImagePersistencePort,
            productOptionGroupLinkPersistencePort,
            productExposureHourPersistencePort,
            productExposureCalculator,
            cupDepositPolicy
        );
    }

    @Bean
    public ProductAvailabilityService productAvailabilityService(
        ProductPersistencePort productPersistencePort,
        ProductOptionPersistencePort productOptionPersistencePort,
        ProductCommonOptionPersistencePort productCommonOptionPersistencePort,
        ProductOptionGroupPersistencePort productOptionGroupPersistencePort,
        ProductCommonOptionGroupPersistencePort productCommonOptionGroupPersistencePort,
        ProductOptionGroupLinkPersistencePort productOptionGroupLinkPersistencePort,
        ProductCommonOptionGroupLinkPersistencePort productCommonOptionGroupLinkPersistencePort
    ) {
        return new ProductAvailabilityService(
            productPersistencePort,
            productOptionPersistencePort,
            productCommonOptionPersistencePort,
            productOptionGroupPersistencePort,
            productCommonOptionGroupPersistencePort,
            productOptionGroupLinkPersistencePort,
            productCommonOptionGroupLinkPersistencePort
        );
    }

    @Bean
    public ProductDeletionService productDeletionService(ProductPersistencePort productPersistencePort) {
        return new ProductDeletionService(productPersistencePort);
    }

    @Bean
    public ProductSortService productSortService(
        ProductPersistencePort productPersistencePort,
        ProductCategoryPersistencePort productCategoryPersistencePort
    ) {
        return new ProductSortService(productPersistencePort, productCategoryPersistencePort);
    }

    @Bean
    public ProductOptionGroupLinkService productOptionGroupLinkService(
        ProductOptionGroupLinkPersistencePort productOptionGroupLinkPersistencePort,
        ProductPersistencePort productPersistencePort
    ) {
        return new ProductOptionGroupLinkService(productOptionGroupLinkPersistencePort, productPersistencePort);
    }

    @Bean
    public ProductOptionGroupMergeService productOptionGroupMergeService(
        ProductOptionGroupPersistencePort productOptionGroupPersistencePort,
        ProductOptionPersistencePort productOptionPersistencePort,
        ProductOptionGroupLinkPersistencePort productOptionGroupLinkPersistencePort,
        ProductOptionGroupLinkService productOptionGroupLinkService,
        ProductOptionGroupMergeHistoryPersistencePort productOptionGroupMergeHistoryPersistencePort
    ) {
        return new ProductOptionGroupMergeService(
            productOptionGroupPersistencePort,
            productOptionPersistencePort,
            productOptionGroupLinkPersistencePort,
            productOptionGroupLinkService,
            productOptionGroupMergeHistoryPersistencePort
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
        ProductPersistencePort productPersistencePort,
        ProductExposureHourPersistencePort productExposureHourPersistencePort,
        ProductExposureCalculator productExposureCalculator
    ) {
        return new ProductExposureService(
            productPersistencePort,
            productExposureHourPersistencePort,
            productExposureCalculator
        );
    }

    @Bean
    public ProductImageApprovalService productImageApprovalService(
        ProductImageChangeRequestPersistencePort productImageChangeRequestPersistencePort,
        ProductImagePersistencePort productImagePersistencePort,
        ProductPersistencePort productPersistencePort
    ) {
        return new ProductImageApprovalService(
            productImageChangeRequestPersistencePort,
            productImagePersistencePort,
            productPersistencePort
        );
    }

    @Bean
    public ProductNutritionService productNutritionService(
        ProductNutritionPersistencePort productNutritionPersistencePort,
        ProductAllergenPersistencePort productAllergenPersistencePort,
        ProductPersistencePort productPersistencePort
    ) {
        return new ProductNutritionService(
            productNutritionPersistencePort,
            productAllergenPersistencePort,
            productPersistencePort
        );
    }

    @Bean
    public ProductVegetarianApprovalService productVegetarianApprovalService(
        ProductVegetarianRequestPersistencePort productVegetarianRequestPersistencePort,
        ProductPersistencePort productPersistencePort
    ) {
        return new ProductVegetarianApprovalService(
            productVegetarianRequestPersistencePort,
            productPersistencePort
        );
    }

    @Bean
    public ProductRepresentativeApprovalService productRepresentativeApprovalService(
        ProductRepresentativeRequestPersistencePort productRepresentativeRequestPersistencePort,
        ProductPersistencePort productPersistencePort,
        ProductImagePersistencePort productImagePersistencePort
    ) {
        return new ProductRepresentativeApprovalService(
            productRepresentativeRequestPersistencePort,
            productPersistencePort,
            productImagePersistencePort
        );
    }

    @Bean
    public ProductPriceService productPriceService(
        ProductPricePersistencePort productPricePersistencePort,
        ProductPersistencePort productPersistencePort,
        StorePriceVerificationPort storePriceVerificationPort
    ) {
        return new ProductPriceService(
            productPricePersistencePort,
            productPersistencePort,
            storePriceVerificationPort
        );
    }

    @Bean
    public StorePriceVerificationService storePriceVerificationService(
        StorePriceVerificationPersistencePort storePriceVerificationPersistencePort,
        ProductPricePersistencePort productPricePersistencePort,
        ProductPersistencePort productPersistencePort,
        StorePriceVerificationPort storePriceVerificationPort,
        ShopRequestIndexSyncPort shopRequestIndexSyncPort
    ) {
        return new StorePriceVerificationService(
            storePriceVerificationPersistencePort,
            productPricePersistencePort,
            productPersistencePort,
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
        ProductPersistencePort productPersistencePort,
        ProductFeedbackPersistencePort productFeedbackPersistencePort,
        ProductFeedbackReadPersistencePort productFeedbackReadPersistencePort
    ) {
        return new ProductFeedbackService(
            productPersistencePort,
            productFeedbackPersistencePort,
            productFeedbackReadPersistencePort
        );
    }

    @Bean
    public ProductShopLinkService productShopLinkService(
        ProductPersistencePort productPersistencePort,
        ProductShopLinkPersistencePort productShopLinkPersistencePort,
        ProductCategoryPersistencePort productCategoryPersistencePort
    ) {
        return new ProductShopLinkService(
            productPersistencePort,
            productShopLinkPersistencePort,
            productCategoryPersistencePort
        );
    }
}
