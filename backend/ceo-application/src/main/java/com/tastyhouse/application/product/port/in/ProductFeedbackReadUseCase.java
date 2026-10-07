package com.tastyhouse.application.product.port.in;

public interface ProductFeedbackReadUseCase {

    void markRead(ProductFeedbackReadCommand command);
}
