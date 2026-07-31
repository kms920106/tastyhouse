package com.tastyhouse.external.mail;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 메일 발송 설정 — 프로퍼티 접두어 {@code mail}.
 *
 * <p>Spring Boot 자동설정의 {@code org.springframework.boot.autoconfigure.mail.MailProperties}와
 * 단순 클래스명이 같으므로 두 타입을 한 파일에서 함께 import하지 않는다. 바인딩 접두어도 서로
 * 달라 충돌하지 않는다 — 이 클래스는 최상위 {@code mail.*}, Spring 쪽은 {@code spring.mail.*}이다.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "mail")
public class MailProperties {

    private String provider;
    private String senderAddress;
}
