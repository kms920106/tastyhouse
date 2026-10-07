package com.tastyhouse.application.faq.port.in;

public interface FaqCategoryCreateUseCase {

    Long createCategory(FaqCategoryCreateCommand command);
}
