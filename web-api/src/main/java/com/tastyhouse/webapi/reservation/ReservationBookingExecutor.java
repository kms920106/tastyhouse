package com.tastyhouse.webapi.reservation;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.service.ReservationBookingService;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 예약 생성의 트랜잭션 단위.
 *
 * <p>{@link ReservationCommandService}의 재시도 루프와 <b>별도 빈</b>으로 분리해야 낙관적 락/유니크 충돌 시
 * "새 트랜잭션"으로 재시도가 동작한다. 같은 빈의 메서드를 호출하면 프록시를 거치지 않아
 * ({@code self-invocation}) {@code @Transactional}이 적용되지 않고, 첫 시도에서 롤백 표시된 트랜잭션을
 * 그대로 재사용하게 되어 재시도가 무의미해진다.
 *
 * <p>불변식(슬롯 정원 검증·차감과 예약 저장의 원자성)은 도메인 서비스
 * {@link ReservationBookingService}가 갖고 있고, 이 클래스는 그 호출을 트랜잭션으로 감싸는 얇은 경계다.
 */
@Component
public class ReservationBookingExecutor {

    private final ReservationBookingService reservationBookingService;

    public ReservationBookingExecutor(ReservationBookingService reservationBookingService) {
        this.reservationBookingService = reservationBookingService;
    }

    /**
     * 한 번의 예약 생성 시도를 독립 트랜잭션으로 실행한다.
     * 슬롯 정원 차감 중 동시 경합이 감지되면 이 트랜잭션은 롤백되고 예외가 호출자에게 전파된다.
     */
    @Transactional
    public ReservationId bookInNewTx(
        MemberId memberId,
        ShopId shopId,
        LocalDate date,
        LocalTime time,
        Integer partySize,
        String request,
        boolean agreedRequiredTerms
    ) {
        return reservationBookingService.book(
            memberId,
            shopId,
            date,
            time,
            partySize,
            request,
            agreedRequiredTerms
        );
    }
}
