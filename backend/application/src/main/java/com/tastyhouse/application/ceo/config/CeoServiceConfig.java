package com.tastyhouse.application.ceo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.ceo.port.out.ReplyPhraseTextValidator;
import com.tastyhouse.application.ceo.port.out.write.CeoLoginHistoryPersistencePort;
import com.tastyhouse.application.ceo.port.out.write.CeoReplyPhrasePersistencePort;
import com.tastyhouse.application.ceo.service.CeoLoginHistoryRecorder;
import com.tastyhouse.application.ceo.service.CeoReplyPhraseService;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class CeoServiceConfig {

    @Bean
    public CeoLoginHistoryRecorder ceoLoginHistoryRecorder(
        CeoLoginHistoryPersistencePort ceoLoginHistoryPersistencePort
    ) {
        return new CeoLoginHistoryRecorder(ceoLoginHistoryPersistencePort);
    }

    @Bean
    public CeoReplyPhraseService ceoReplyPhraseService(
        CeoReplyPhrasePersistencePort ceoReplyPhrasePersistencePort,
        ReplyPhraseTextValidator replyPhraseTextValidator
    ) {
        return new CeoReplyPhraseService(ceoReplyPhrasePersistencePort, replyPhraseTextValidator);
    }
}
