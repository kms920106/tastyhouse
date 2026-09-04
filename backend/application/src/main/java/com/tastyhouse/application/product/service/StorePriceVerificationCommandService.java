package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.StorePriceVerificationApproveCommand;
import com.tastyhouse.application.product.port.in.StorePriceVerificationCommandUseCase;
import com.tastyhouse.application.product.port.in.StorePriceVerificationRejectCommand;
import com.tastyhouse.application.product.port.in.StorePriceVerificationStartReviewCommand;
import com.tastyhouse.domain.product.service.StorePriceVerificationService;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;

/**
 * 매장 가격 인증 요청 검수 변경 서비스(CQRS command 측).
 *
 * <p>승인 시 요청 상태 전이·각 가격 행의 매장가 반영·가게 인증 플래그 켜기가 한 트랜잭션에서 함께
 * 일어나야 하는 원자 연산이므로 그 본체는 도메인 서비스
 * {@link StorePriceVerificationService}가 담당한다. 이 서비스는 트랜잭션 경계와 식별자
 * 승격({@code Long} → ID VO), 그리고 처리 시각 주입만 책임진다.
 *
 * <p><b>{@code now}를 여기서 만들어 넘기는 이유</b>는 도메인 서비스가 시간을 스스로 읽지 않기 때문이다
 * (순수 POJO·테스트 가능성). 한 요청 안의 여러 전이가 같은 시각을 갖도록 메서드마다 한 번만 읽는다.
 *
 * <p><b>요청처리 현황 인덱스({@code SHOP_REQUEST_INDEX}) 동기화는 이 서비스가 하지 않는다.</b>
 * {@code ShopRequestIndexRecorder}의 공개 sync 메서드 중 {@code (ShopRequestType, Long,
 * ShopRequestStatus, String)} 조합을 받는 것이 없어({@code syncBlindRequestStatus}는 요청 유형이
 * {@code REVIEW_BLIND}로 고정) 이 유형을 반영할 방법이 없으며, 메서드 추가는 domain-module 변경이라
 * 이 작업 범위를 벗어난다. 상세는 작업 보고 참조 — <b>도메인 변경이 선행돼야 하는 미결 항목</b>이다.
 */
@Service
@AdminApp
@Transactional
public class StorePriceVerificationCommandService implements StorePriceVerificationCommandUseCase {

    private final StorePriceVerificationService storePriceVerificationService;

    public StorePriceVerificationCommandService(StorePriceVerificationService storePriceVerificationService) {
        this.storePriceVerificationService = storePriceVerificationService;
    }

    /**
     * 검수에 착수한다({@code PENDING} → {@code IN_PROGRESS}).
     *
     * <p>{@code IN_PROGRESS}가 존재하는 이유가 바로 이 경로다 — 검수자가 가격표와 실제 매장을 대조하는
     * 동안 점주 화면이 "접수됨"이 아니라 "검수 중"을 보여주고, 그 사이 점주의 재요청이 차단된다.
     */
    @Override
    public void startReview(StorePriceVerificationStartReviewCommand command) {
        Long id = command.verificationId();
        StorePriceVerificationId verificationId = StorePriceVerificationId.of(id);
        storePriceVerificationService.startReview(verificationId, LocalDateTime.now());
    }

    /**
     * 승인한다 — 요청에 담긴 매장가가 각 가격 행에 반영되고 가게 인증이 켜진다.
     *
     * <p>반영되는 값은 <b>요청 시점에 박제된 매장가</b>다. 승인 시점의 현재 가격을 다시 읽지 않으므로
     * 검수자가 화면에서 본 값과 반영되는 값이 항상 같다.
     */
    @Override
    public void approve(StorePriceVerificationApproveCommand command) {
        Long id = command.verificationId();
        StorePriceVerificationId verificationId = StorePriceVerificationId.of(id);
        storePriceVerificationService.approve(verificationId, LocalDateTime.now());
    }

    /** 반려한다. 사유는 필수다 — 점주가 무엇을 고쳐 다시 요청해야 하는지 알아야 한다. */
    @Override
    public void reject(StorePriceVerificationRejectCommand command) {
        Long id = command.verificationId();
        String rejectReason = command.rejectReason();
        StorePriceVerificationId verificationId = StorePriceVerificationId.of(id);
        storePriceVerificationService.reject(verificationId, rejectReason, LocalDateTime.now());
    }
}
