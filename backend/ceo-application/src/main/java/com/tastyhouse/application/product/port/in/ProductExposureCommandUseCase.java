package com.tastyhouse.application.product.port.in;

public interface ProductExposureCommandUseCase {

    void replaceExposure(ProductExposureReplaceCommand command);

    void clearExposure(ProductExposureClearCommand command);
}
