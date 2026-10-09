package com.tastyhouse.application.product.port.out.write;

import java.util.List;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

public interface ProductCommonOptionGroupLinkPersistencePort {

    ProductCommonOptionGroupLink save(ProductCommonOptionGroupLink link);

    List<ProductCommonOptionGroupLink> findAllByOptionGroupIdIn(List<ProductOptionGroupId> optionGroupIds);

    void delete(ProductCommonOptionGroupLink link);
}
