package com.tastyhouse.application.ceo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.ceo.port.out.ReplyPhraseTextValidator;
import com.tastyhouse.application.ceo.port.out.write.CeoLoginHistoryRepository;
import com.tastyhouse.application.ceo.port.out.write.CeoReplyPhraseRepository;
import com.tastyhouse.application.ceo.service.CeoLoginHistoryRecorder;
import com.tastyhouse.application.ceo.service.CeoReplyPhraseService;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class CeoServiceConfig {
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
