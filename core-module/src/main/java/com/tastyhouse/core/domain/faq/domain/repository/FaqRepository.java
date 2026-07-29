package com.tastyhouse.core.domain.faq.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.faq.domain.model.Faq;
import com.tastyhouse.core.domain.faq.domain.vo.FaqId;

/**
 * FAQ 항목 write 포트.
 *
 * <p>도메인 모델을 주고받는 CRUD만 노출한다. 목록·검색·페이징 등 표현 목적 read는 이 포트가 아니라
 * infrastructure-module의 {@code faq/query/FaqQueryDao}가 담당한다(CQRS 분리).
 */
public interface FaqRepository {

    Optional<Faq> findById(FaqId faqId);

    Faq save(Faq faq);
}
