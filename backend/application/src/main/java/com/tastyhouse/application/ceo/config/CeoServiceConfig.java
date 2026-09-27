package com.tastyhouse.application.ceo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.ceo.port.out.ReplyPhraseTextValidator;
import com.tastyhouse.application.ceo.port.out.write.CeoLoginHistoryStatePort;
import com.tastyhouse.application.ceo.port.out.write.CeoReplyPhraseStatePort;
import com.tastyhouse.application.ceo.port.out.write.CeoStatePort;
import com.tastyhouse.application.ceo.service.CeoLoginHistoryRecorder;
import com.tastyhouse.application.ceo.service.CeoReplyPhraseService;
import com.tastyhouse.application.ceo.store.CeoLoginHistoryRepository;
import com.tastyhouse.application.ceo.store.CeoLoginHistoryStore;
import com.tastyhouse.application.ceo.store.CeoReplyPhraseRepository;
import com.tastyhouse.application.ceo.store.CeoReplyPhraseStore;
import com.tastyhouse.application.ceo.store.CeoRepository;
import com.tastyhouse.application.ceo.store.CeoStore;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class CeoServiceConfig {
    @Bean
    public CeoRepository ceoRepository(CeoStatePort ceoStatePort) {
        return new CeoStore(ceoStatePort);
    }

    @Bean
    public CeoLoginHistoryRepository ceoLoginHistoryRepository(CeoLoginHistoryStatePort ceoLoginHistoryStatePort) {
        return new CeoLoginHistoryStore(ceoLoginHistoryStatePort);
    }

    @Bean
    public CeoReplyPhraseRepository ceoReplyPhraseRepository(CeoReplyPhraseStatePort ceoReplyPhraseStatePort) {
        return new CeoReplyPhraseStore(ceoReplyPhraseStatePort);
    }

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
