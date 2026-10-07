package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.EditorChoicePolicy;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shop.port.in.ShopEditorChoiceQueryUseCase;
import com.tastyhouse.application.shop.port.out.EditorChoiceResult;
import com.tastyhouse.application.shop.port.out.ShopChoiceQueryPort;

@Service
@Transactional(readOnly = true)
class ShopEditorChoiceQueryService implements ShopEditorChoiceQueryUseCase {

    private final ShopChoiceQueryPort shopChoiceQueryPort;

    public ShopEditorChoiceQueryService(ShopChoiceQueryPort shopChoiceQueryPort) {
        this.shopChoiceQueryPort = shopChoiceQueryPort;
    }

    @Override
    public List<EditorChoiceResult> searchEditorChoices(int page, int size) {
        return shopChoiceQueryPort.findEditorChoices(PageQuery.of(page, size), EditorChoicePolicy.PRODUCT_LIMIT).content();
    }
}
