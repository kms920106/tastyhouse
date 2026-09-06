package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductShopLinkCommandUseCase {

    void replaceLinks(ProductShopLinkReplaceCommand command);

    void linkToShop(ProductShopLinkCreateCommand command);

    void unlinkFromShop(ProductShopLinkDeleteCommand command);
}
