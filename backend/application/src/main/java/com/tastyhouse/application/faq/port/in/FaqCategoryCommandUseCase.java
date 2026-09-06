package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface FaqCategoryCommandUseCase {

    Long createCategory(FaqCategoryCreateCommand command);

    void updateCategory(FaqCategoryUpdateCommand command);

    void deleteCategory(FaqCategoryDeleteCommand command);
}
