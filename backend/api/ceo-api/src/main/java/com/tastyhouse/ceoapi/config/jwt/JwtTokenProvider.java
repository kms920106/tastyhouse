package com.tastyhouse.ceoapi.config.jwt;

import org.springframework.stereotype.Component;

import com.tastyhouse.security.jwt.JwtProperties;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;

/**
 * 공용 {@link com.tastyhouse.security.jwt.JwtTokenProvider}(access/refresh 발급·검증 메커니즘)를 상속한다.
 * ceo-api는 검증용 토큰이 없으므로 추가 메서드 없이 principal 식별자 클레임({@code ceoId})과
 * principal 재구성({@code CustomUserDetails})만 주입한다.
 */
@Component
public class JwtTokenProvider extends com.tastyhouse.security.jwt.JwtTokenProvider {

    public JwtTokenProvider(JwtProperties jwtProperties) {
        super(jwtProperties, "ceoId", CustomUserDetails::new);
    }
}
