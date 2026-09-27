package com.tastyhouse.application.event.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.event.port.out.write.EventAnnouncementStatePort;
import com.tastyhouse.application.event.port.out.write.EventStatePort;
import com.tastyhouse.application.event.port.out.write.EventWinnerStatePort;
import com.tastyhouse.application.event.store.EventAnnouncementRepository;
import com.tastyhouse.application.event.store.EventAnnouncementStore;
import com.tastyhouse.application.event.store.EventRepository;
import com.tastyhouse.application.event.store.EventStore;
import com.tastyhouse.application.event.store.EventWinnerRepository;
import com.tastyhouse.application.event.store.EventWinnerStore;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class EventServiceConfig {
    @Bean
    public EventRepository eventRepository(EventStatePort eventStatePort) {
        return new EventStore(eventStatePort);
    }

    @Bean
    public EventAnnouncementRepository eventAnnouncementRepository(EventAnnouncementStatePort eventAnnouncementStatePort) {
        return new EventAnnouncementStore(eventAnnouncementStatePort);
    }

    @Bean
    public EventWinnerRepository eventWinnerRepository(EventWinnerStatePort eventWinnerStatePort) {
        return new EventWinnerStore(eventWinnerStatePort);
    }
}
