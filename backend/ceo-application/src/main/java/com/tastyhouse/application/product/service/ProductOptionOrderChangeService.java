package com.tastyhouse.application.product.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.application.product.port.in.ProductOptionOrderChangeCommand;
import com.tastyhouse.application.product.port.in.ProductOptionOrderChangeUseCase;
import com.tastyhouse.application.product.port.out.write.ProductOptionLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductOptionSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductOptionOrderChangeService implements ProductOptionOrderChangeUseCase {

    private final ProductOptionLoadPort productOptionLoadPort;
    private final ProductOptionSavePort productOptionSavePort;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionOrderChangeService(
        ProductOptionLoadPort productOptionLoadPort,
        ProductOptionSavePort productOptionSavePort,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.productOptionLoadPort = productOptionLoadPort;
        this.productOptionSavePort = productOptionSavePort;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productOptionGroupOwnershipValidator = productOptionGroupOwnershipValidator;
    }

    @Override
    public void changeProductOptionOrder(ProductOptionOrderChangeCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long optionGroupId = command.optionGroupId();
        List<Long> optionIds = command.optionIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        productOptionGroupOwnershipValidator.validateOptionGroupShop(shopId, optionGroupId);

        Map<Long, ProductOption> byId = productOptionLoadPort
            .findAllByOptionGroupId(ProductOptionGroupId.of(optionGroupId)).stream()
            .collect(Collectors.toMap(ProductOption::getId, Function.identity()));

        List<Long> requested = distinct(optionIds);
        if (byId.size() != requested.size() || !byId.keySet().containsAll(requested)) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
        }

        for (int index = 0; index < requested.size(); index++) {
            ProductOption option = byId.get(requested.get(index));
            option.update(
                option.getName(),
                option.getAdditionalPrice(),
                index,
                option.isSoldOut(),
                option.isVisible(),
                option.getCupCount(),
                option.getPersonalCupDiscountAmount()
            );
            productOptionSavePort.save(option);
        }
    }

    private List<Long> distinct(List<Long> ids) {
        Set<Long> unique = new LinkedHashSet<>(ids);
        return List.copyOf(unique);
    }
}
