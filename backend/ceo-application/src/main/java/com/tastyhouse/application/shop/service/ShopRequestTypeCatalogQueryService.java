package com.tastyhouse.application.shop.service;

import java.util.Arrays;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.application.shared.port.out.CodeLabelResult;
import com.tastyhouse.application.shop.port.in.ShopRequestTypeCatalogQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopRequestTypeCatalogResult;
import com.tastyhouse.application.shop.port.out.ShopRequestTypeView;

@Service
@Transactional(readOnly = true)
class ShopRequestTypeCatalogQueryService implements ShopRequestTypeCatalogQueryUseCase {

    @Override
    public ShopRequestTypeCatalogResult getRequestTypes() {
        return new ShopRequestTypeCatalogResult(
            Arrays.stream(ShopRequestType.values())
                .map(requestType -> new ShopRequestTypeView(
                    requestType.name(),
                    requestType.getDescription(),
                    requestType.isContractAmending()
                ))
                .toList(),
            Arrays.stream(ShopRequestStatus.values())
                .map(status -> new CodeLabelResult(status.name(), status.getDescription()))
                .toList()
        );
    }
}
