package com.tastyhouse.application.payment.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tastyhouse.application.payment.port.out.PgCancelResult;
import com.tastyhouse.application.payment.port.out.PgConfirmResult;
import com.tastyhouse.application.payment.port.out.PgPaymentGateway;
import com.tastyhouse.application.payment.port.out.PgProviderCode;
import com.tastyhouse.application.payment.port.out.PgProviderGateway;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.payment.model.PgProvider;

public class PgPaymentGatewayRouter implements PgPaymentGateway {

    private final Map<String, PgProviderGateway> gateways;

    public PgPaymentGatewayRouter(List<PgProviderGateway> gateways) {
        Map<String, PgProviderGateway> registered = new HashMap<>();
        for (PgProviderGateway gateway : gateways) {
            String provider = toPgProvider(gateway.provider()).name();
            PgProviderGateway previous = registered.putIfAbsent(provider, gateway);
            if (previous != null) {
                throw new IllegalStateException("PG사 " + provider + " 게이트웨이가 중복 등록됐습니다: "
                    + previous.getClass().getName() + ", " + gateway.getClass().getName());
            }
        }
        this.gateways = registered;
    }

    @Override
    public boolean supports(String pgProvider) {
        return pgProvider != null && gateways.containsKey(pgProvider);
    }

    @Override
    public PgConfirmResult confirmPayment(String pgProvider, Long paymentId, String paymentKey, String pgOrderId, int amount) {
        return resolve(pgProvider).confirmPayment(paymentId, paymentKey, pgOrderId, amount);
    }

    @Override
    public PgCancelResult cancelPayment(String pgProvider, String pgTid, String cancelReason) {
        return resolve(pgProvider).cancelPayment(pgTid, cancelReason);
    }

    private PgProviderGateway resolve(String pgProvider) {
        if (!supports(pgProvider)) {
            throw new BusinessException(ErrorCode.PG_PROVIDER_UNSUPPORTED,
                ErrorCode.PG_PROVIDER_UNSUPPORTED.getDefaultMessage() + ": " + pgProvider);
        }
        return gateways.get(pgProvider);
    }

    private static PgProvider toPgProvider(PgProviderCode code) {
        return PgProvider.valueOf(code.name());
    }
}
