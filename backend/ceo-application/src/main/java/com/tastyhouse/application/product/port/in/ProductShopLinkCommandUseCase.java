package com.tastyhouse.application.product.port.in;

public interface ProductShopLinkCommandUseCase {

    void replaceLinks(ProductShopLinkReplaceCommand command);

    void linkToShop(ProductShopLinkCreateCommand command);

    void unlinkFromShop(ProductShopLinkDeleteCommand command);
}
