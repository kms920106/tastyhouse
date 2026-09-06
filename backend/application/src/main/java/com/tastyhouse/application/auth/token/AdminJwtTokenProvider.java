package com.tastyhouse.application.auth.token;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Component;

import com.tastyhouse.security.jwt.JwtProperties;
import com.tastyhouse.application.auth.security.AdminUserDetails;

@Component
@AdminApp
public class AdminJwtTokenProvider extends com.tastyhouse.security.jwt.JwtTokenProvider {

    public AdminJwtTokenProvider(JwtProperties jwtProperties) {
        super(jwtProperties, "adminId", AdminUserDetails::new);
    }
}
