package com.tastyhouse.infrastructure.ceo.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CeoReplyPhraseJpaRepository extends JpaRepository<CeoReplyPhraseJpaEntity, Long> {
}
