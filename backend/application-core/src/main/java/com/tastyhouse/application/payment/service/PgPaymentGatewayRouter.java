package com.tastyhouse.application.payment.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.application.payment.port.out.PgCancelLookupResult;
import com.tastyhouse.application.payment.port.out.PgCancelResult;
import com.tastyhouse.application.payment.port.out.PgConfirmResult;
import com.tastyhouse.application.payment.port.out.PgPaymentGatewayPort;
import com.tastyhouse.application.payment.port.out.PgPaymentLookupResult;
import com.tastyhouse.application.payment.port.out.PgProviderCode;
import com.tastyhouse.application.payment.port.out.PgProviderGatewayPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

@Service
class PgPaymentGatewayRouter implements PgPaymentGatewayPort {

    private final Map<String, PgProviderGatewayPort> gateways;

    public PgPaymentGatewayRouter(List<PgProviderGatewayPort> gateways) {
        Map<String, PgProviderGatewayPort> registered = new HashMap<>();
        for (PgProviderGatewayPort gateway : gateways) {
            String provider = toPgProvider(gateway.provider()).name();
            PgProviderGatewayPort previous = registered.putIfAbsent(provider, gateway);
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

    @Override
    public PgPaymentLookupResult lookupPayment(String pgProvider, String pgOrderId) {
        return resolve(pgProvider).lookupPayment(pgOrderId);
    }

    @Override
    public PgCancelLookupResult lookupCancellation(String pgProvider, String pgOrderId, String paymentKey) {
        return resolve(pgProvider).lookupCancellation(pgOrderId, paymentKey);
    }

    private PgProviderGatewayPort resolve(String pgProvider) {
        if (!supports(pgProvider)) {
            throw new ApplicationException(ApplicationErrorCode.PG_PROVIDER_UNSUPPORTED,
                ApplicationErrorCode.PG_PROVIDER_UNSUPPORTED.getDefaultMessage() + ": " + pgProvider);
        }
        return gateways.get(pgProvider);
    }

    private static PgProvider toPgProvider(PgProviderCode code) {
        return PgProvider.valueOf(code.name());
    }
}
