package com.tastyhouse.application.product.port.in;

public interface ProductFeedbackCommandUseCase {

    Long submitFeedback(ProductFeedbackCreateCommand command);
}
