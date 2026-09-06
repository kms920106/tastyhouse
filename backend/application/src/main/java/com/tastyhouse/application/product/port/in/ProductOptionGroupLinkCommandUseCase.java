package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductOptionGroupLinkCommandUseCase {

    void linkOptionGroup(ProductOptionGroupLinkCommand command);

    void unlinkOptionGroup(ProductOptionGroupUnlinkCommand command);

    void changeOptionGroupOrder(ProductOptionGroupOrderChangeCommand command);
}
