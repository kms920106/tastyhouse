package com.tastyhouse.application.auth.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.domain.ceo.model.CeoLoginFailureReason;
import com.tastyhouse.application.auth.port.in.CeoAuthLoginCommand;
import com.tastyhouse.application.auth.port.in.CeoLoginUseCase;
import com.tastyhouse.application.auth.port.out.CeoJwtResult;
import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.application.auth.token.CeoTokenService;
import com.tastyhouse.application.ceo.port.in.CeoLoginFailureRecordUseCase;
import com.tastyhouse.application.ceo.port.in.CeoLoginHistoryFailureCommand;
import com.tastyhouse.application.ceo.port.in.CeoLoginHistorySuccessCommand;
import com.tastyhouse.application.ceo.port.in.CeoLoginSuccessRecordUseCase;
import com.tastyhouse.application.ceo.port.out.write.CeoPersistencePort;

@Service
class CeoLoginService implements CeoLoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(CeoLoginService.class);

    private final AuthenticationManager authenticationManager;
    private final CeoTokenService tokenService;
    private final CeoPersistencePort ceoPersistencePort;
    private final CeoLoginSuccessRecordUseCase ceoLoginSuccessRecordUseCase;
    private final CeoLoginFailureRecordUseCase ceoLoginFailureRecordUseCase;

    public CeoLoginService(
        AuthenticationManager authenticationManager,
        CeoTokenService tokenService,
        CeoPersistencePort ceoPersistencePort,
        CeoLoginSuccessRecordUseCase ceoLoginSuccessRecordUseCase,
        CeoLoginFailureRecordUseCase ceoLoginFailureRecordUseCase
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.ceoPersistencePort = ceoPersistencePort;
        this.ceoLoginSuccessRecordUseCase = ceoLoginSuccessRecordUseCase;
        this.ceoLoginFailureRecordUseCase = ceoLoginFailureRecordUseCase;
    }

    @Override
    public CeoJwtResult login(CeoAuthLoginCommand command) {
        String username = command.username();
        String ipAddress = command.ipAddress();
        String userAgent = command.userAgent();

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, command.password())
            );
        } catch (AuthenticationException e) {
            recordFailureQuietly(username, e, ipAddress, userAgent);
            throw e;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        CeoUserDetails userDetails = (CeoUserDetails) authentication.getPrincipal();
        CeoLoginHistorySuccessCommand historyCommand =
            CeoLoginHistorySuccessCommand.of(userDetails.getCeoId(), ipAddress, userAgent);
        ceoLoginSuccessRecordUseCase.recordSuccess(historyCommand);

        return tokenService.issue(authentication, command.rememberMe());
    }

    private void recordFailureQuietly(
        String username,
        AuthenticationException authenticationException,
        String ipAddress,
        String userAgent
    ) {
        try {
            Optional<Ceo> ceo = ceoPersistencePort.findByUsername(username);
            if (ceo.isEmpty()) {

                return;
            }
            CeoLoginFailureReason failureReason = resolveFailureReason(authenticationException);
            if (failureReason == null) {

                return;
            }
            CeoLoginHistoryFailureCommand command = CeoLoginHistoryFailureCommand.of(
                ceo.get().getId(),
                failureReason.name(),
                ipAddress,
                userAgent
            );
            ceoLoginFailureRecordUseCase.recordFailure(command);
        } catch (RuntimeException e) {
            log.error("점주 로그인 실패 이력 기록에 실패했습니다. username={}", username, e);
        }
    }

    private CeoLoginFailureReason resolveFailureReason(AuthenticationException e) {
        if (e instanceof BadCredentialsException) {
            return CeoLoginFailureReason.BAD_CREDENTIALS;
        }
        if (e instanceof DisabledException || e instanceof LockedException) {
            return CeoLoginFailureReason.ACCOUNT_INACTIVE;
        }
        return null;
    }
}
