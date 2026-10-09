package com.tastyhouse.application.auth.service;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.auth.port.out.SocialOAuthClientPort;
import com.tastyhouse.application.auth.port.out.SocialProvider;

@Component
class SocialOAuthClientRouter {

    private final Map<SocialProvider, SocialOAuthClientPort> clients;

    public SocialOAuthClientRouter(List<SocialOAuthClientPort> clients) {
        Map<SocialProvider, SocialOAuthClientPort> registered = new EnumMap<>(SocialProvider.class);
        for (SocialOAuthClientPort client : clients) {
            SocialProvider provider = client.provider();
            SocialOAuthClientPort previous = registered.putIfAbsent(provider, client);
            if (previous != null) {
                throw new IllegalStateException("소셜 로그인 " + provider + " 클라이언트가 중복 등록됐습니다: "
                    + previous.getClass().getName() + ", " + client.getClass().getName());
            }
        }
        List<SocialProvider> missing = Arrays.stream(SocialProvider.values())
            .filter(provider -> !registered.containsKey(provider))
            .toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("소셜 로그인 클라이언트가 등록되지 않은 provider가 있습니다: " + missing);
        }
        this.clients = registered;
    }

    SocialOAuthClientPort resolve(SocialProvider provider) {
        SocialOAuthClientPort client = clients.get(provider);
        if (client == null) {
            throw new IllegalStateException("소셜 로그인 " + provider + " 클라이언트가 등록되지 않았습니다.");
        }
        return client;
    }
}
