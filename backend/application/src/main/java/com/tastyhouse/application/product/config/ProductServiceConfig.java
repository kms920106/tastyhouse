package com.tastyhouse.application.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.product.service.CupDepositPolicy;
import com.tastyhouse.domain.product.service.ProductExposureCalculator;
import com.tastyhouse.domain.product.service.StorePriceBadgePolicy;
import com.tastyhouse.application.product.port.out.ProductReviewStatisticsPort;
import com.tastyhouse.application.product.port.out.ShopRequestIndexSyncPort;
import com.tastyhouse.application.product.port.out.StorePriceVerificationPort;
import com.tastyhouse.application.product.port.out.write.ProductAllergenStatePort;
import com.tastyhouse.application.product.port.out.write.ProductBbqStatePort;
import com.tastyhouse.application.product.port.out.write.ProductCategoryStatePort;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupLinkStatePort;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupStatePort;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionStatePort;
import com.tastyhouse.application.product.port.out.write.ProductExposureHourStatePort;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackReadStatePort;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackStatePort;
import com.tastyhouse.application.product.port.out.write.ProductImageChangeRequestStatePort;
import com.tastyhouse.application.product.port.out.write.ProductImageStatePort;
import com.tastyhouse.application.product.port.out.write.ProductNutritionStatePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLinkStatePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeExclusionStatePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeHistoryStatePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupStatePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionStatePort;
import com.tastyhouse.application.product.port.out.write.ProductPriceStatePort;
import com.tastyhouse.application.product.port.out.write.ProductRepresentativeRequestStatePort;
import com.tastyhouse.application.product.port.out.write.ProductShopLinkStatePort;
import com.tastyhouse.application.product.port.out.write.ProductStatePort;
import com.tastyhouse.application.product.port.out.write.ProductVegetarianRequestStatePort;
import com.tastyhouse.application.product.port.out.write.StorePriceVerificationStatePort;
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
import com.tastyhouse.application.product.store.ProductAllergenRepository;
import com.tastyhouse.application.product.store.ProductAllergenStore;
import com.tastyhouse.application.product.store.ProductBbqRepository;
import com.tastyhouse.application.product.store.ProductBbqStore;
import com.tastyhouse.application.product.store.ProductCategoryRepository;
import com.tastyhouse.application.product.store.ProductCategoryStore;
import com.tastyhouse.application.product.store.ProductCommonOptionGroupLinkRepository;
import com.tastyhouse.application.product.store.ProductCommonOptionGroupLinkStore;
import com.tastyhouse.application.product.store.ProductCommonOptionGroupRepository;
import com.tastyhouse.application.product.store.ProductCommonOptionGroupStore;
import com.tastyhouse.application.product.store.ProductCommonOptionRepository;
import com.tastyhouse.application.product.store.ProductCommonOptionStore;
import com.tastyhouse.application.product.store.ProductExposureHourRepository;
import com.tastyhouse.application.product.store.ProductExposureHourStore;
import com.tastyhouse.application.product.store.ProductFeedbackReadRepository;
import com.tastyhouse.application.product.store.ProductFeedbackReadStore;
import com.tastyhouse.application.product.store.ProductFeedbackRepository;
import com.tastyhouse.application.product.store.ProductFeedbackStore;
import com.tastyhouse.application.product.store.ProductImageChangeRequestRepository;
import com.tastyhouse.application.product.store.ProductImageChangeRequestStore;
import com.tastyhouse.application.product.store.ProductImageRepository;
import com.tastyhouse.application.product.store.ProductImageStore;
import com.tastyhouse.application.product.store.ProductNutritionRepository;
import com.tastyhouse.application.product.store.ProductNutritionStore;
import com.tastyhouse.application.product.store.ProductOptionGroupLinkRepository;
import com.tastyhouse.application.product.store.ProductOptionGroupLinkStore;
import com.tastyhouse.application.product.store.ProductOptionGroupMergeExclusionRepository;
import com.tastyhouse.application.product.store.ProductOptionGroupMergeExclusionStore;
import com.tastyhouse.application.product.store.ProductOptionGroupMergeHistoryRepository;
import com.tastyhouse.application.product.store.ProductOptionGroupMergeHistoryStore;
import com.tastyhouse.application.product.store.ProductOptionGroupRepository;
import com.tastyhouse.application.product.store.ProductOptionGroupStore;
import com.tastyhouse.application.product.store.ProductOptionRepository;
import com.tastyhouse.application.product.store.ProductOptionStore;
import com.tastyhouse.application.product.store.ProductPriceRepository;
import com.tastyhouse.application.product.store.ProductPriceStore;
import com.tastyhouse.application.product.store.ProductRepository;
import com.tastyhouse.application.product.store.ProductRepresentativeRequestRepository;
import com.tastyhouse.application.product.store.ProductRepresentativeRequestStore;
import com.tastyhouse.application.product.store.ProductShopLinkRepository;
import com.tastyhouse.application.product.store.ProductShopLinkStore;
import com.tastyhouse.application.product.store.ProductStore;
import com.tastyhouse.application.product.store.ProductVegetarianRequestRepository;
import com.tastyhouse.application.product.store.ProductVegetarianRequestStore;
import com.tastyhouse.application.product.store.StorePriceVerificationRepository;
import com.tastyhouse.application.product.store.StorePriceVerificationStore;
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

    @Bean
    public ProductAllergenRepository productAllergenRepository(ProductAllergenStatePort productAllergenStatePort) {
        return new ProductAllergenStore(productAllergenStatePort);
    }

    @Bean
    public ProductBbqRepository productBbqRepository(ProductBbqStatePort productBbqStatePort) {
        return new ProductBbqStore(productBbqStatePort);
    }

    @Bean
    public ProductExposureHourRepository productExposureHourRepository(ProductExposureHourStatePort productExposureHourStatePort) {
        return new ProductExposureHourStore(productExposureHourStatePort);
    }

    @Bean
    public ProductNutritionRepository productNutritionRepository(ProductNutritionStatePort productNutritionStatePort) {
        return new ProductNutritionStore(productNutritionStatePort);
    }

    @Bean
    public ProductFeedbackReadRepository productFeedbackReadRepository(ProductFeedbackReadStatePort productFeedbackReadStatePort) {
        return new ProductFeedbackReadStore(productFeedbackReadStatePort);
    }

    @Bean
    public ProductFeedbackRepository productFeedbackRepository(ProductFeedbackStatePort productFeedbackStatePort) {
        return new ProductFeedbackStore(productFeedbackStatePort);
    }

    @Bean
    public ProductCategoryRepository productCategoryRepository(ProductCategoryStatePort productCategoryStatePort) {
        return new ProductCategoryStore(productCategoryStatePort);
    }

    @Bean
    public ProductImageRepository productImageRepository(ProductImageStatePort productImageStatePort) {
        return new ProductImageStore(productImageStatePort);
    }

    @Bean
    public ProductImageChangeRequestRepository productImageChangeRequestRepository(ProductImageChangeRequestStatePort productImageChangeRequestStatePort) {
        return new ProductImageChangeRequestStore(productImageChangeRequestStatePort);
    }

    @Bean
    public ProductRepresentativeRequestRepository productRepresentativeRequestRepository(ProductRepresentativeRequestStatePort productRepresentativeRequestStatePort) {
        return new ProductRepresentativeRequestStore(productRepresentativeRequestStatePort);
    }

    @Bean
    public ProductVegetarianRequestRepository productVegetarianRequestRepository(ProductVegetarianRequestStatePort productVegetarianRequestStatePort) {
        return new ProductVegetarianRequestStore(productVegetarianRequestStatePort);
    }

    @Bean
    public ProductShopLinkRepository productShopLinkRepository(ProductShopLinkStatePort productShopLinkStatePort) {
        return new ProductShopLinkStore(productShopLinkStatePort);
    }

    @Bean
    public StorePriceVerificationRepository storePriceVerificationRepository(StorePriceVerificationStatePort storePriceVerificationStatePort) {
        return new StorePriceVerificationStore(storePriceVerificationStatePort);
    }

    @Bean
    public ProductRepository productRepository(ProductStatePort productStatePort) {
        return new ProductStore(productStatePort);
    }

    @Bean
    public ProductPriceRepository productPriceRepository(ProductPriceStatePort productPriceStatePort) {
        return new ProductPriceStore(productPriceStatePort);
    }

    @Bean
    public ProductOptionRepository productOptionRepository(ProductOptionStatePort productOptionStatePort) {
        return new ProductOptionStore(productOptionStatePort);
    }

    @Bean
    public ProductCommonOptionRepository productCommonOptionRepository(ProductCommonOptionStatePort productCommonOptionStatePort) {
        return new ProductCommonOptionStore(productCommonOptionStatePort);
    }

    @Bean
    public ProductOptionGroupRepository productOptionGroupRepository(ProductOptionGroupStatePort productOptionGroupStatePort) {
        return new ProductOptionGroupStore(productOptionGroupStatePort);
    }

    @Bean
    public ProductCommonOptionGroupRepository productCommonOptionGroupRepository(ProductCommonOptionGroupStatePort productCommonOptionGroupStatePort) {
        return new ProductCommonOptionGroupStore(productCommonOptionGroupStatePort);
    }

    @Bean
    public ProductOptionGroupLinkRepository productOptionGroupLinkRepository(ProductOptionGroupLinkStatePort productOptionGroupLinkStatePort) {
        return new ProductOptionGroupLinkStore(productOptionGroupLinkStatePort);
    }

    @Bean
    public ProductCommonOptionGroupLinkRepository productCommonOptionGroupLinkRepository(ProductCommonOptionGroupLinkStatePort productCommonOptionGroupLinkStatePort) {
        return new ProductCommonOptionGroupLinkStore(productCommonOptionGroupLinkStatePort);
    }

    @Bean
    public ProductOptionGroupMergeExclusionRepository productOptionGroupMergeExclusionRepository(ProductOptionGroupMergeExclusionStatePort productOptionGroupMergeExclusionStatePort) {
        return new ProductOptionGroupMergeExclusionStore(productOptionGroupMergeExclusionStatePort);
    }

    @Bean
    public ProductOptionGroupMergeHistoryRepository productOptionGroupMergeHistoryRepository(ProductOptionGroupMergeHistoryStatePort productOptionGroupMergeHistoryStatePort) {
        return new ProductOptionGroupMergeHistoryStore(productOptionGroupMergeHistoryStatePort);
    }
}
