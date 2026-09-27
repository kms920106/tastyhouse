package com.tastyhouse.application.notice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.notice.port.out.write.NoticeStatePort;
import com.tastyhouse.application.notice.store.NoticeRepository;
import com.tastyhouse.application.notice.store.NoticeStore;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class NoticeServiceConfig {
    @Bean
    public NoticeRepository noticeRepository(NoticeStatePort noticeStatePort) {
        return new NoticeStore(noticeStatePort);
    }
}
