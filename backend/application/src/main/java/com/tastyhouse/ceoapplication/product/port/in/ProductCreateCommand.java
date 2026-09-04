package com.tastyhouse.ceoapplication.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 등록 command. 연결 가게 목록은 중첩 command로 받는다.
 */
public record ProductCreateCommand(
    Long ceoId,
    Long shopId,
    Long productCategoryId,
    String name,
    String composition,
    String description,
    Integer originalPrice,
    Integer discountPrice,
    Boolean singleServing,
    Integer spiciness,
    Boolean representative,
    Boolean ratingExcluded,
    List<ProductShopLinkItemCommand> links
) {
    public ProductCreateCommand {
        if (ceoId == null
            || shopId == null
            || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
