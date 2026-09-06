package com.tastyhouse.domain.product.service;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 일회용컵 보증금 <b>정책 상수와 계산</b>의 단일 소유자.
 *
 * <p>요율(컵 1개당 300원)은 환경부가 정하는 <b>규제 값</b>이지 가게별 설정이 아니다. 그래서
 * 설정값(properties)이 아니라 코드 상수로 두고, 옵션 행에는 <b>개수만</b> 남긴다 — 요율이 바뀌어도
 * 옵션 데이터 마이그레이션이 필요 없다(과거 주문의 금액은 스냅샷이 보존한다).
 *
 * <p>이 클래스가 한 곳에 있어야 하는 이유는 같은 요율을 <b>세 경로</b>가 쓰기 때문이다 — 점주 설정
 * (ceo), 손님 메뉴판 표시(web), 주문 금액 확정(order). 상수가 흩어지면 요율 변경 시 한 곳만 바뀌어
 * "화면에 보이는 금액과 결제 금액이 다른" 사고가 난다.
 *
 * <p>리포지토리도 시계도 갖지 않는 순수 계산기다({@code ProductExposureCalculator} 선례). 빈 등록은
 * infrastructure-module의 {@code ProductDomainConfig}가 담당한다.
 */
public class CupDepositPolicy {

    /** 컵 1개당 보증금(원). 자원순환보증금관리센터 고시값. */
    public static final int DEPOSIT_PER_CUP = 300;

    /** 옵션 1건이 제공할 수 있는 컵의 최대 개수. 세트 메뉴를 고려한 상한이다. */
    public static final int MAX_CUP_COUNT = 10;

    /** 컵 개수의 하한. 0개는 "보증금 옵션이 아니다"와 같으므로 유형으로 표현한다. */
    public static final int MIN_CUP_COUNT = 1;

    /**
     * 컵 개수에 해당하는 보증금을 계산한다. {@code null}이나 0 이하면 0원이다 — 일반 옵션은 컵 개수를
     * 갖지 않으므로 이 경로로 들어와도 금액에 영향이 없어야 한다.
     */
    public int depositAmountOf(Integer cupCount) {
        if (cupCount == null || cupCount <= 0) {
            return 0;
        }
        validateCupCount(cupCount);
        return cupCount * DEPOSIT_PER_CUP;
    }

    /**
     * 컵 개수가 허용 범위인지 검증한다.
     *
     * <p>요청 단계의 {@code @Min}/{@code @Max}와 <b>중복해서</b> 검사하는 이유는, 이 값이 금액을
     * 결정하기 때문이다 — 검증이 표현 계층에만 있으면 다른 진입 경로(배치·관리자)가 생길 때 조용히
     * 우회된다.
     */
    public void validateCupCount(Integer cupCount) {
        if (cupCount == null || cupCount < MIN_CUP_COUNT || cupCount > MAX_CUP_COUNT) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_CUP_COUNT_INVALID);
        }
    }
}
