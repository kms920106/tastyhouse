package com.tastyhouse.application.auth.token;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.auth.security.AdminUserDetails;
import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.security.jwt.JwtProperties;

@Component
@AdminApp
public class AdminJwtTokenProvider extends com.tastyhouse.security.jwt.JwtTokenProvider {

    public AdminJwtTokenProvider(JwtProperties jwtProperties) {
        super(jwtProperties, "adminId", AdminUserDetails::new);
    }
}
