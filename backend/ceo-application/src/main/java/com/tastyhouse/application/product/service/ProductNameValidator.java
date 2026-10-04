package com.tastyhouse.application.product.service;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;

@Component
public class ProductNameValidator {

    private static final Pattern ALLOWED_NAME =
        Pattern.compile("^[가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z0-9\\s:,./~%&()+\\[\\]™®]*$");

    private final ProductPersistencePort productPersistencePort;

    public ProductNameValidator(ProductPersistencePort productPersistencePort) {
        this.productPersistencePort = productPersistencePort;
    }

    public void validateForCreate(Long shopId, String name) {
        validateCharacters(name);
        if (productPersistencePort.existsByShopIdAndName(ShopId.of(shopId), name)) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_NAME_DUPLICATED);
        }
    }

    public void validateForUpdate(Long shopId, Long productId, String name) {
        validateCharacters(name);
        if (productPersistencePort.existsByShopIdAndNameAndIdNot(ShopId.of(shopId), name, ProductId.of(productId))) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_NAME_DUPLICATED);
        }
    }

    private void validateCharacters(String name) {
        if (name != null && !ALLOWED_NAME.matcher(name).matches()) {
            throw new ApplicationException(CeoErrorCode.PRODUCT_NAME_INVALID_CHARACTER);
        }
    }
}
