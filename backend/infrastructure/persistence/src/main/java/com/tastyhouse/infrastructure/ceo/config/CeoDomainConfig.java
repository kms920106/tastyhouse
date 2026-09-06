package com.tastyhouse.infrastructure.ceo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.ceo.port.ReplyPhraseTextValidator;
import com.tastyhouse.domain.ceo.repository.CeoLoginHistoryRepository;
import com.tastyhouse.domain.ceo.repository.CeoReplyPhraseRepository;
import com.tastyhouse.domain.ceo.service.CeoLoginHistoryRecorder;
import com.tastyhouse.domain.ceo.service.CeoReplyPhraseService;

@Configuration(proxyBeanMethods = false)
public class CeoDomainConfig {
    @Bean
    public CeoLoginHistoryRecorder ceoLoginHistoryRecorder(
        CeoLoginHistoryRepository ceoLoginHistoryRepository
    ) {
        return new CeoLoginHistoryRecorder(ceoLoginHistoryRepository);
    }

    @Bean
    public CeoReplyPhraseService ceoReplyPhraseService(
        CeoReplyPhraseRepository ceoReplyPhraseRepository,
        ReplyPhraseTextValidator replyPhraseTextValidator
    ) {
        return new CeoReplyPhraseService(ceoReplyPhraseRepository, replyPhraseTextValidator);
    }
}
