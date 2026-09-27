package com.tastyhouse.application.ceo.store;

import com.tastyhouse.application.ceo.port.out.write.CeoState;
import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.domain.ceo.model.CeoStatus;
import com.tastyhouse.domain.shared.vo.PhoneNumber;

final class CeoStateMapper {
    private CeoStateMapper() {
    }

    static Ceo toDomain(CeoState state) {
        return Ceo.reconstitute(
            state.id(),
            state.username(),
            state.password(),
            state.name(),
            state.businessRegistrationNumber(),
            state.phoneNumber() == null ? null : new PhoneNumber(state.phoneNumber()),
            state.email(),
            state.status() == null ? null : CeoStatus.valueOf(state.status())
        );
    }

    static CeoState toState(Ceo ceo) {
        return new CeoState(
            ceo.getId(),
            ceo.getUsername(),
            ceo.getPassword(),
            ceo.getName(),
            ceo.getBusinessRegistrationNumber(),
            ceo.getPhoneNumber() == null ? null : ceo.getPhoneNumber().value(),
            ceo.getEmail(),
            ceo.getStatus() == null ? null : ceo.getStatus().name()
        );
    }
}
