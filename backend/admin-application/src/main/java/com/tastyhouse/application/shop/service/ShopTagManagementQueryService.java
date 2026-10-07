package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopTagManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopChoiceManagementQueryPort;
import com.tastyhouse.application.shop.port.out.TagResult;

@Service
@Transactional(readOnly = true)
class ShopTagManagementQueryService implements ShopTagManagementQueryUseCase {

    private final ShopChoiceManagementQueryPort shopChoiceManagementQueryPort;

    public ShopTagManagementQueryService(ShopChoiceManagementQueryPort shopChoiceManagementQueryPort) {
        this.shopChoiceManagementQueryPort = shopChoiceManagementQueryPort;
    }

    @Override
    public List<TagResult> getTags() {
        return shopChoiceManagementQueryPort.findAllTags();
    }
}
