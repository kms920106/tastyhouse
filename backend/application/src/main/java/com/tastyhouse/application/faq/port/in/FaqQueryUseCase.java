package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import com.tastyhouse.application.faq.port.out.FaqCategoryResult;
import com.tastyhouse.application.faq.port.out.FaqResult;

@WebApp
public interface FaqQueryUseCase {

    List<FaqCategoryResult> getFaqCategories();

    List<FaqResult> getFaqList(Long categoryId);
}
