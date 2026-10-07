package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.EditorChoicePolicy;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.in.ShopChoiceListManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.EditorChoiceResult;
import com.tastyhouse.application.shop.port.out.ShopChoiceManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ShopChoiceListManagementQueryService implements ShopChoiceListManagementQueryUseCase {

    private final ShopChoiceManagementQueryPort shopChoiceManagementQueryPort;

    public ShopChoiceListManagementQueryService(ShopChoiceManagementQueryPort shopChoiceManagementQueryPort) {
        this.shopChoiceManagementQueryPort = shopChoiceManagementQueryPort;
    }

    @Override
    public PageResult<EditorChoiceResult> getShopChoices(int page, int size) {
        return shopChoiceManagementQueryPort.findEditorChoices(PageQuery.of(page, size), EditorChoicePolicy.PRODUCT_LIMIT);
    }
}
