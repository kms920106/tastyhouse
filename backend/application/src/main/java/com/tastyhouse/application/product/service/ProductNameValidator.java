package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

@Component
@CeoApp
public class ProductNameValidator {

    private static final Pattern ALLOWED_NAME =
        Pattern.compile("^[가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z0-9\\s:,./~%&()+\\[\\]™®]*$");

    private final ProductRepository productRepository;

    public ProductNameValidator(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void validateForCreate(Long shopId, String name) {
        validateCharacters(name);
        if (productRepository.existsByShopIdAndName(ShopId.of(shopId), name)) {
            throw new BusinessException(ErrorCode.PRODUCT_NAME_DUPLICATED);
        }
    }

    public void validateForUpdate(Long shopId, Long productId, String name) {
        validateCharacters(name);
        if (productRepository.existsByShopIdAndNameAndIdNot(ShopId.of(shopId), name, ProductId.of(productId))) {
            throw new BusinessException(ErrorCode.PRODUCT_NAME_DUPLICATED);
        }
    }

    private void validateCharacters(String name) {
        if (name != null && !ALLOWED_NAME.matcher(name).matches()) {
            throw new BusinessException(ErrorCode.PRODUCT_NAME_INVALID_CHARACTER);
        }
    }
}
