package com.tastyhouse.application.auth.token;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.auth.security.AdminUserDetails;
import com.tastyhouse.security.jwt.JwtProperties;

@Component
public class AdminJwtTokenProvider extends com.tastyhouse.security.jwt.JwtTokenProvider {

    public AdminJwtTokenProvider(JwtProperties jwtProperties) {
        super(jwtProperties, "adminId", AdminUserDetails::new);
    }
}
