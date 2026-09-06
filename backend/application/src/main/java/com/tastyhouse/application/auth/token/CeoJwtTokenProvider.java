package com.tastyhouse.application.auth.token;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Component;

import com.tastyhouse.security.jwt.JwtProperties;
import com.tastyhouse.application.auth.security.CeoUserDetails;

@Component
@CeoApp
public class CeoJwtTokenProvider extends com.tastyhouse.security.jwt.JwtTokenProvider {

    public CeoJwtTokenProvider(JwtProperties jwtProperties) {
        super(jwtProperties, "ceoId", CeoUserDetails::new);
    }
}
