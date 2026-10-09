package com.tastyhouse.infrastructure.jpa.ceo.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface CeoReplyPhraseJpaRepository extends JpaRepository<CeoReplyPhraseJpaEntity, Long> {
}
