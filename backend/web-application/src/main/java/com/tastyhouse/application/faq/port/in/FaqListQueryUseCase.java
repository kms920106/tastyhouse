package com.tastyhouse.application.faq.port.in;

import java.util.List;

import com.tastyhouse.application.faq.port.out.FaqResult;

public interface FaqListQueryUseCase {

    List<FaqResult> getFaqList(Long categoryId);
}
