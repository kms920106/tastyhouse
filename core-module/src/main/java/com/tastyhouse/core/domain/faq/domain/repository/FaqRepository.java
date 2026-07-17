package com.tastyhouse.core.domain.faq.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.faq.domain.model.Faq;
import com.tastyhouse.core.domain.faq.domain.vo.FaqId;
import com.tastyhouse.core.domain.faq.application.dto.FaqListItemDto;
import com.tastyhouse.core.domain.faq.application.dto.FaqResult;
import com.tastyhouse.core.domain.faq.application.dto.FaqSearchCondition;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface FaqRepository {

    List<FaqResult> findAllActiveItems();

    List<FaqResult> findActiveItemsByCategoryId(Long categoryId);

    PageResult<FaqListItemDto> findFaqPage(FaqSearchCondition condition, PageQuery pageQuery);

    Optional<Faq> findById(FaqId faqId);

    Faq save(Faq faq);
}
