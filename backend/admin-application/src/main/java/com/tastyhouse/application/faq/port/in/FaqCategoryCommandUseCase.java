package com.tastyhouse.application.faq.port.in;

public interface FaqCategoryCommandUseCase {

    Long createCategory(FaqCategoryCreateCommand command);

    void updateCategory(FaqCategoryUpdateCommand command);

    void deleteCategory(FaqCategoryDeleteCommand command);
}
