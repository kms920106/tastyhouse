package com.tastyhouse.domain.shop.model;

import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shared.model.DayType;

/**
 * 상점 영업시간 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopBusinessHourJpaEntity} + {@code ShopBusinessHourMapper}가 담당한다.
 */
public class ShopBusinessHour {

    /** 영업시간 최소 길이(분) — PDF 규격 "최소 1시간". */
    private static final long MIN_DURATION_MINUTES = 60;

    /** 영업시간 최대 길이(분) — PDF 규격 "최대 23시간 55분"(24시간 영업은 {@code is24Hours}로 표현). */
    private static final long MAX_DURATION_MINUTES = 23 * 60 + 55;

    private final Long id;
    private final ShopId shopId;
    private DayType dayType;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean isClosed;
    private Boolean is24Hours; // 24시간 영업 여부 (true면 openTime/closeTime 무관)

    private ShopBusinessHour(
        Long id,
        ShopId shopId,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        this.id = id;
        this.shopId = shopId;
        this.dayType = dayType;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = isClosed;
        this.is24Hours = is24Hours;
    }

    /**
     * 신규 영업시간을 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     *
     * <p>영업시간 규격 불변식({@link #validateBusinessHour})을 강제한다. 이 검증은 원래
     * {@code ShopBusinessHourService}에 있었으나, 영업시간 <b>값 자체의 불변식</b>(다른 애그리거트를 읽지
     * 않는 순수 값 규칙)이라 애그리거트로 내렸다 — 서비스에 두면 {@code of()}를 직접 부르는 경로
     * (배치·마이그레이션)가 규격을 우회할 수 있었다. 서비스에는 영업시간 애그리거트를 읽어야 하는
     * 크로스 애그리거트 검증({@code validateBreakTimeWithinBusinessHours})만 남는다.
     *
     * <p>{@link #reconstitute}는 이 검증을 <b>거치지 않는다</b> — 기존 DB 데이터가 새 불변식을 위반해도
     * 로드는 가능해야 하기 때문이다.
     */
    public static ShopBusinessHour of(
        ShopId shopId,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        validateBusinessHour(openTime, closeTime, isClosed, is24Hours);

        return new ShopBusinessHour(null, shopId, dayType, openTime, closeTime, isClosed, is24Hours);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     *
     * <p><b>{@link #of}와 달리 영업시간 규격 검증을 하지 않는다</b> — 불변식 도입 이전에 저장된 기존
     * 영업시간이 새 규칙을 위반하더라도 로드는 가능해야 하기 때문이다.
     */
    public static ShopBusinessHour reconstitute(
        Long id,
        ShopId shopId,
        DayType dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        return new ShopBusinessHour(id, shopId, dayType, openTime, closeTime, isClosed, is24Hours);
    }

    /**
     * 영업시간을 변경한다. 생성({@code of})과 같은 규격 불변식을 강제한다 — 생성만 막고 변경을 열어두면
     * 같은 위반 값이 곧바로 뒷문으로 들어오기 때문이다.
     */
    public void update(DayType dayType, LocalTime openTime, LocalTime closeTime, Boolean isClosed, Boolean is24Hours) {
        validateBusinessHour(openTime, closeTime, isClosed, is24Hours);

        this.dayType = dayType;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = isClosed;
        this.is24Hours = is24Hours;
    }

    /**
     * 이 영업시간 행 기준으로 주어진 시각이 영업 중인지 판정한다.
     *
     * <p>24시간 영업이면 시각과 무관하게 영업중이고, 휴무 표시 행이거나 개점·폐점 시각이 없으면 영업이
     * 아니다. 그 외에는 {@code [openTime, closeTime)} 반열림 구간으로 보며, 폐점이 개점보다 이르면
     * 자정을 넘기는 영업으로 간주한다.
     *
     * <p>이 판정은 원래 {@code ShopOperatingStatusCalculator}가 {@code getIs24Hours()}/{@code getIsClosed()}
     * /{@code getOpenTime()}/{@code getCloseTime()}을 꺼내 {@code Boolean.TRUE.equals(...)}로 4중 방어하며
     * 수행했다. {@code Boolean} 래퍼의 null 3-상태 정규화는 애그리거트 <b>안 한 곳</b>에 있어야 호출부마다
     * 방어 코드가 복제되지 않으므로 모델로 이식했다.
     */
    public boolean isOpenAt(LocalTime time) {
        if (is24Hours()) {
            return true;
        }
        if (isClosed() || openTime == null || closeTime == null) {
            return false;
        }
        return isWithinRange(time, openTime, closeTime);
    }

    /**
     * 이 영업시간이 자정을 넘겨 다음날 새벽까지 이어지는 경우, 주어진 시각이 그 <b>연장 구간</b>에 드는지
     * 판정한다(전일 행을 오늘 새벽 시각으로 확인할 때 쓴다).
     *
     * <p>24시간·휴무 행은 연장 개념이 없으므로 항상 false다.
     */
    public boolean extendsIntoNextDayAt(LocalTime time) {
        if (is24Hours() || isClosed() || openTime == null || closeTime == null) {
            return false;
        }
        return crossesMidnight(openTime, closeTime) && time.isBefore(closeTime);
    }

    /** 휴무 표시 여부. {@code Boolean} 래퍼의 null은 false로 정규화한다. */
    public boolean isClosed() {
        return Boolean.TRUE.equals(isClosed);
    }

    /** 24시간 영업 여부. {@code Boolean} 래퍼의 null은 false로 정규화한다. */
    public boolean is24Hours() {
        return Boolean.TRUE.equals(is24Hours);
    }

    /**
     * 영속화용 원본 값 접근자 — null을 정규화하지 않고 {@code Boolean} 그대로 반환한다.
     * 도메인 판정에는 null을 false로 접는 {@link #isClosed()}를 쓰고, DB 컬럼이 nullable이므로
     * 매퍼는 미설정(null)과 false를 구분해 저장해야 해서 이 접근자를 쓴다.
     */
    public Boolean getIsClosed() {
        return this.isClosed;
    }

    /**
     * 영속화용 원본 값 접근자 — null을 정규화하지 않고 {@code Boolean} 그대로 반환한다.
     * 도메인 판정에는 {@link #is24Hours()}를 쓴다.
     */
    public Boolean getIs24Hours() {
        return this.is24Hours;
    }

    /**
     * 시각이 {@code [start, end)} 구간에 드는지 판단한다. {@code end < start}면 자정을 넘기는 구간으로 본다.
     */
    private static boolean isWithinRange(LocalTime time, LocalTime start, LocalTime end) {
        if (crossesMidnight(start, end)) {
            return !time.isBefore(start) || time.isBefore(end);
        }
        return !time.isBefore(start) && time.isBefore(end);
    }

    private static boolean crossesMidnight(LocalTime start, LocalTime end) {
        return end.isBefore(start);
    }

    /**
     * 영업시간 PDF 규격을 검증한다: 휴무/24시간이면 시간 검증 생략, 그 외에는 5분 단위·최소 1시간~최대
     * 23시간 55분. 자정 넘김(종료 &lt; 시작)은 허용하며 다음날로 넘어간 것으로 계산한다.
     *
     * <p>인스턴스 상태를 읽지 않으므로 {@code static}이다 — 그래야 생성({@code of})과 변경
     * ({@code update})이 같은 검증 한 벌을 공유할 수 있다.
     */
    private static void validateBusinessHour(LocalTime openTime, LocalTime closeTime, Boolean isClosed, Boolean is24Hours) {
        if (Boolean.TRUE.equals(isClosed) || Boolean.TRUE.equals(is24Hours)) {
            return;
        }
        if (openTime == null || closeTime == null) {
            throw new BusinessException(ErrorCode.SHOP_BUSINESS_HOUR_INVALID_RANGE);
        }
        if (isNotFiveMinuteUnit(openTime) || isNotFiveMinuteUnit(closeTime)) {
            throw new BusinessException(ErrorCode.SHOP_BUSINESS_HOUR_INVALID_UNIT);
        }
        long durationMinutes = minutesBetween(openTime, closeTime);
        if (durationMinutes < MIN_DURATION_MINUTES || durationMinutes > MAX_DURATION_MINUTES) {
            throw new BusinessException(ErrorCode.SHOP_BUSINESS_HOUR_INVALID_RANGE);
        }
    }

    private static boolean isNotFiveMinuteUnit(LocalTime time) {
        return time.getMinute() % 5 != 0 || time.getSecond() != 0 || time.getNano() != 0;
    }

    /**
     * open→close 경과 분. 자정 넘김(close ≤ open)이면 다음날로 넘어간 것으로 24시간을 더해 계산한다.
     */
    private static long minutesBetween(LocalTime open, LocalTime close) {
        int openMin = open.getHour() * 60 + open.getMinute();
        int closeMin = close.getHour() * 60 + close.getMinute();
        int diff = closeMin - openMin;
        if (diff <= 0) {
            diff += 24 * 60;
        }
        return diff;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public DayType getDayType() {
        return this.dayType;
    }

    public LocalTime getOpenTime() {
        return this.openTime;
    }

    public LocalTime getCloseTime() {
        return this.closeTime;
    }
}
