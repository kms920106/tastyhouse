package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductExposureCommandUseCase {

    void replaceExposure(ProductExposureReplaceCommand command);

    void clearExposure(ProductExposureClearCommand command);
}
