package com.tastyhouse.application.product.port.in;

public interface ProductFeedbackCreateUseCase {

    Long submitFeedback(ProductFeedbackCreateCommand command);
}
