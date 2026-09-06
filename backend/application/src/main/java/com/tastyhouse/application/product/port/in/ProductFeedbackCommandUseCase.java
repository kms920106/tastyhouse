package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface ProductFeedbackCommandUseCase {

    Long submitFeedback(ProductFeedbackCreateCommand command);
}
