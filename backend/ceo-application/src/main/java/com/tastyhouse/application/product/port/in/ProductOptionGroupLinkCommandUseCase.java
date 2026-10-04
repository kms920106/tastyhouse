package com.tastyhouse.application.product.port.in;

public interface ProductOptionGroupLinkCommandUseCase {

    void linkOptionGroup(ProductOptionGroupLinkCommand command);

    void unlinkOptionGroup(ProductOptionGroupUnlinkCommand command);

    void changeOptionGroupOrder(ProductOptionGroupOrderChangeCommand command);
}
