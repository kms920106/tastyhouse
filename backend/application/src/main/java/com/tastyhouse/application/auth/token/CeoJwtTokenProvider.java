package com.tastyhouse.application.auth.token;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.security.jwt.JwtProperties;

@Component
@CeoApp
public class CeoJwtTokenProvider extends com.tastyhouse.security.jwt.JwtTokenProvider {

    public CeoJwtTokenProvider(JwtProperties jwtProperties) {
        super(jwtProperties, "ceoId", CeoUserDetails::new);
    }
}
