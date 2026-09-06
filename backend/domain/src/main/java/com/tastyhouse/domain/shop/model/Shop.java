package com.tastyhouse.domain.shop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.StationId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 상점 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopJpaEntity} + {@code ShopMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code ShopRepository#save}를
 * 호출해야 한다.
 */
public class Shop {

    /** 최소주문금액 미설정 값 — 제한 없음을 뜻한다. */
    public static final int MIN_ORDER_AMOUNT_UNSET = 0;
    /** 최소주문금액을 설정할 때의 하한(원). */
    public static final int MIN_ORDER_AMOUNT_LOWER_BOUND = 5000;
    /** 최소주문금액을 설정할 때의 상한(원). */
    public static final int MIN_ORDER_AMOUNT_UPPER_BOUND = 30000;

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private CeoId ceoId; // 소유 점주 ID (CEO.id 참조, null이면 점주 미배정)
    private StationId stationId; // 지하철역 ID (STATION.id 참조)
    private String name; // 상호명
    private BigDecimal latitude; // 위도
    private BigDecimal longitude; // 경도
    private final Double rating; // 평균 평점
    private String roadAddress; // 도로명 주소
    private String lotAddress; // 지번 주소
    private String phoneNumber; // 대표 전화번호
    private UploadedFileId thumbnailImageFileId; // 썸네일 이미지 파일 ID (FILE.id 참조)
    private UploadedFileId trademarkImageFileId; // 상표 이미지 파일 ID (승인 완료 시 반영, FILE.id 참조)
    private boolean permanentlyClosed; // 폐업 여부 (true: 폐업)
    private boolean hidden; // 노출정지 여부 (true: 배민앱 완전 비노출, 폐업과 별개)
    private boolean closedOnPublicHolidays; // 공휴일 휴무 여부 (true: 공휴일 휴무)
    private int minOrderAmount; // 최소주문금액 (0: 미설정, 설정 시 5000~30000, 배달 주문에만 적용)
    private boolean scheduledOrderEnabled; // 예약주문 운영 여부 (true: 고객이 수령시간을 예약할 수 있음)
    /**
     * 일회용컵 보증금제 <b>대상 사업자</b> 여부.
     *
     * <p>이것은 점주의 영업 설정이 아니라 <b>외부 규제 사실</b>이다 — 환경부·자원순환보증금관리센터가
     * 지역(제주·세종)과 사업자 규모로 지정하며, 지정·해제가 운영 이벤트로 발생한다. 그래서 점주가
     * 스스로 켤 수 없고 <b>admin만 토글</b>한다.
     *
     * <p>주소 문자열로 지역을 파싱해 판정하지 않는 이유도 같다 — 같은 지역이어도 지정 사업자가 아닐 수
     * 있고, 지정 여부는 주소에서 도출되는 값이 아니다.
     */
    private boolean cupDepositEnabled;
    /**
     * 매장 가격 인증 여부 — <b>'매장과 같은 가격' 뱃지 노출과 매장가·픽업가 설정 가능 여부를 함께 가른다.</b>
     *
     * <p>점주가 스스로 켤 수 없다. 관리자가 가격표 이미지와 실제 매장을 대조해 승인할 때만 켜지며
     * ({@code StorePriceVerificationService}), 메뉴 가격이 바뀌어 <b>배달가 &gt; 매장가</b>가 되면
     * 가격 저장 시점에 <b>동기</b>로 내려간다 — 배치로 미루면 그 사이 손님이 잘못된 뱃지를 본다.
     */
    private boolean storePriceVerified;
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private Shop(
        Long id,
        CeoId ceoId,
        StationId stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Double rating,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        UploadedFileId thumbnailImageFileId,
        UploadedFileId trademarkImageFileId,
        boolean permanentlyClosed,
        boolean hidden,
        boolean closedOnPublicHolidays,
        int minOrderAmount,
        boolean scheduledOrderEnabled,
        boolean cupDepositEnabled,
        boolean storePriceVerified,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.ceoId = ceoId;
        this.stationId = stationId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.phoneNumber = phoneNumber;
        this.thumbnailImageFileId = thumbnailImageFileId;
        this.trademarkImageFileId = trademarkImageFileId;
        this.permanentlyClosed = permanentlyClosed;
        this.hidden = hidden;
        this.closedOnPublicHolidays = closedOnPublicHolidays;
        this.minOrderAmount = minOrderAmount;
        this.scheduledOrderEnabled = scheduledOrderEnabled;
        this.cupDepositEnabled = cupDepositEnabled;
        this.storePriceVerified = storePriceVerified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 상점을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     *
     * <p>최소주문금액은 {@link #MIN_ORDER_AMOUNT_UNSET}(미설정)으로 시작한다 — 관리자의 가게 등록 화면은
     * 이 값을 다루지 않고, 점주가 {@link #changeMinOrderAmount(int)}로 직접 설정한다. 예약주문도 같은
     * 이유로 미운영({@code false})으로 시작하고 점주가 {@link #changeScheduledOrderEnabled(boolean)}로 켠다.
     *
     * <p>일회용컵 보증금제 대상 여부도 <b>미지정({@code false})</b>으로 시작한다 — 외부 규제 사실이므로
     * 가게 등록 시점에 알 수 없고, 지정되면 관리자가 {@link #changeCupDepositEnabled(boolean)}로 켠다.
     */
    public static Shop of(
        StationId stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        UploadedFileId thumbnailImageFileId
    ) {
        return new Shop(
            null,
            null,
            stationId,
            name,
            latitude,
            longitude,
            null,
            roadAddress,
            lotAddress,
            phoneNumber,
            thumbnailImageFileId,
            null,
            false,
            false,
            false,
            MIN_ORDER_AMOUNT_UNSET,
            false,
            false,
            false,
            null,
            null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static Shop reconstitute(
        Long id,
        CeoId ceoId,
        StationId stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Double rating,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        UploadedFileId thumbnailImageFileId,
        UploadedFileId trademarkImageFileId,
        boolean permanentlyClosed,
        boolean hidden,
        boolean closedOnPublicHolidays,
        int minOrderAmount,
        boolean scheduledOrderEnabled,
        boolean cupDepositEnabled,
        boolean storePriceVerified,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Shop(
            id,
            ceoId,
            stationId,
            name,
            latitude,
            longitude,
            rating,
            roadAddress,
            lotAddress,
            phoneNumber,
            thumbnailImageFileId,
            trademarkImageFileId,
            permanentlyClosed,
            hidden,
            closedOnPublicHolidays,
            minOrderAmount,
            scheduledOrderEnabled,
            cupDepositEnabled,
            storePriceVerified,
            createdAt,
            updatedAt
        );
    }

    public ShopId getShopId() {
        return ShopId.of(this.id);
    }

    /**
     * 가게 기본 정보를 수정한다.
     *
     * <p>폐업({@link #close()})한 가게는 수정할 수 없다 — 폐업을 되돌리는 API("폐업 취소")가 admin·ceo
     * 어디에도 없어 폐업은 불가역이며, 되살릴 수 없는 가게의 정보를 계속 고치는 것은 업무상 오조작이다.
     */
    public void update(
        StationId stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        UploadedFileId thumbnailImageFileId
    ) {
        validateNotPermanentlyClosed();

        this.stationId = stationId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.phoneNumber = phoneNumber;
        this.thumbnailImageFileId = thumbnailImageFileId;
    }

    /**
     * 소유 점주를 배정한다(관리자가 가게-점주 연결 시 사용). null이면 점주 미배정 상태로 되돌린다.
     */
    public void assignCeo(CeoId ceoId) {
        this.ceoId = ceoId;
    }

    /**
     * 대표 전화번호를 갱신한다. 전화번호 다건 관리에서 대표번호 변경 시 {@code Shop.phoneNumber}를 동기화한다.
     */
    public void changePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * 승인 완료된 상표 이미지를 반영한다.
     */
    public void changeTrademarkImage(UploadedFileId trademarkImageFileId) {
        this.trademarkImageFileId = trademarkImageFileId;
    }

    /**
     * 승인 완료된 대표(썸네일) 이미지를 반영한다.
     */
    public void changeThumbnailImage(UploadedFileId thumbnailImageFileId) {
        this.thumbnailImageFileId = thumbnailImageFileId;
    }

    /**
     * 공휴일 휴무 여부를 설정한다.
     */
    public void updateHolidayClosure(boolean closedOnPublicHolidays) {
        this.closedOnPublicHolidays = closedOnPublicHolidays;
    }

    /**
     * 최소주문금액을 변경한다.
     *
     * <p>{@link #MIN_ORDER_AMOUNT_UNSET}(0)은 "미설정"(제한 없음)이고, 값을 설정할 때는
     * {@link #MIN_ORDER_AMOUNT_LOWER_BOUND}원 이상 {@link #MIN_ORDER_AMOUNT_UPPER_BOUND}원 이하여야 한다.
     * 폐업한 가게는 {@link #update}와 마찬가지로 변경할 수 없다.
     */
    public void changeMinOrderAmount(int minOrderAmount) {
        validateNotPermanentlyClosed();

        if (minOrderAmount != MIN_ORDER_AMOUNT_UNSET
            && (minOrderAmount < MIN_ORDER_AMOUNT_LOWER_BOUND || minOrderAmount > MIN_ORDER_AMOUNT_UPPER_BOUND)) {
            throw new BusinessException(ErrorCode.SHOP_MIN_ORDER_AMOUNT_OUT_OF_RANGE);
        }

        this.minOrderAmount = minOrderAmount;
    }

    /**
     * 주문 금액이 가게 최소주문금액을 충족하는지 검증한다.
     *
     * <p>기준 금액은 상품 할인까지 반영한 금액(쿠폰·포인트 차감 전)이다 — 고객이 장바구니에 담는 시점의
     * 금액이 판정 대상이고, 쿠폰·포인트는 결제 단계에서만 적용되기 때문이다. 따라서 쿠폰·포인트로 최종
     * 결제금액이 최소주문금액 아래로 내려가도 주문은 거부되지 않는다.
     *
     * <p>최소주문금액이 미설정(0)이거나 배달 외 주문방식이면 검증하지 않는다 — 배민 가이드상 픽업(포장)에는
     * 가게 최소주문금액이 적용되지 않으며, 매장 테이블 오더·예약에도 적용 근거가 없다.
     */
    public void validateMinOrderAmount(OrderMethod orderMethod, int orderAmountAfterProductDiscount) {
        if (minOrderAmount == MIN_ORDER_AMOUNT_UNSET || orderMethod != OrderMethod.DELIVERY) {
            return;
        }

        if (orderAmountAfterProductDiscount < minOrderAmount) {
            throw new BusinessException(ErrorCode.SHOP_MINIMUM_ORDER_AMOUNT_NOT_MET);
        }
    }

    /**
     * 예약주문 운영 여부를 변경한다.
     *
     * <p>설정 단위는 <b>가게 하나</b>이며 주문유형별로 나누지 않는다(PDF 규격). 리드타임·슬롯 단위는
     * {@code ScheduledOrderPolicy} 상수로 고정되어 점주가 조정하지 않는다.
     *
     * <p><b>끄더라도 이미 접수된 예약주문은 건드리지 않는다</b> — "금액·시간은 결제 시점 기준 확정"이라는
     * 이 도메인의 원칙상, OFF는 신규 예약만 차단한다. 폐업한 가게는 {@link #changeMinOrderAmount(int)}와
     * 마찬가지로 변경할 수 없다.
     */
    public void changeScheduledOrderEnabled(boolean scheduledOrderEnabled) {
        validateNotPermanentlyClosed();

        this.scheduledOrderEnabled = scheduledOrderEnabled;
    }

    /**
     * 일회용컵 보증금제 대상 사업자 지정/해제를 반영한다. <b>admin 전용</b>이다.
     *
     * <p>점주가 스스로 켤 수 없는 이유는 이것이 영업 설정이 아니라 외부 규제 사실이기 때문이다
     * (필드 주석 참조). 폐업한 가게는 다른 설정과 마찬가지로 변경할 수 없다.
     */
    public void changeCupDepositEnabled(boolean cupDepositEnabled) {
        validateNotPermanentlyClosed();

        this.cupDepositEnabled = cupDepositEnabled;
    }

    /**
     * 매장 가격 인증을 켠다 — <b>관리자 승인 시에만 호출한다.</b>
     *
     * <p>점주가 직접 켜는 경로를 두지 않는 것은 이 값이 가격표와 실제 매장을 대조한 <b>검수 결과</b>이기
     * 때문이다({@code vegetarianType}·{@code representative}가 승인 전용인 것과 같은 판단).
     */
    public void verifyStorePrice() {
        this.storePriceVerified = true;
    }

    /**
     * 매장 가격 인증을 내린다(재인증 필요 판정).
     *
     * <p>승인과 달리 <b>폐업 검증을 하지 않는다</b> — 인증을 내리는 것은 안전한 방향이므로 어떤 상태의
     * 가게에서도 막히지 않아야 한다. 여기서 막으면 폐업 가게가 잘못된 뱃지를 영구히 유지한다.
     */
    public void clearStorePriceVerification() {
        this.storePriceVerified = false;
    }

    /** 매장 가격 인증 여부 — 매장가·픽업가 설정 가능 여부이자 '매장과 같은 가격' 뱃지의 근거다. */
    public boolean isStorePriceVerified() {
        return this.storePriceVerified;
    }

    /** 일회용컵 보증금 옵션그룹을 <b>새로 만들 수</b> 있는 가게인가. */
    public boolean canUseCupDeposit() {
        return this.cupDepositEnabled;
    }

    /**
     * 보증금 옵션그룹 <b>생성</b> 자격을 검증한다.
     *
     * <p><b>게이트는 생성 경로에만 둔다</b> — 이미 만들어진 보증금 옵션그룹은 지정이 해제돼도 조회·주문이
     * 계속 동작해야 한다. 규제 해제는 "즉시 무효"가 아니라 유예를 두고 정리되는 사건이고, 조회 경로까지
     * 막으면 진행 중인 주문의 금액 계산이 도중에 달라진다.
     */
    public void validateCupDepositEnabled() {
        if (!this.cupDepositEnabled) {
            throw new BusinessException(ErrorCode.SHOP_CUP_DEPOSIT_NOT_ENABLED);
        }
    }

    /**
     * 배민앱에서 가게를 완전히 숨긴다(노출정지).
     */
    public void hide() {
        this.hidden = true;
    }

    /**
     * 노출정지를 해제해 다시 노출한다.
     *
     * <p>폐업({@link #close()})한 가게는 다시 노출할 수 없다 — 폐업을 되돌리는 API("폐업 취소")가
     * admin·ceo 어디에도 없으므로, 폐업 가게를 재노출하면 되돌릴 수 없는 상태로 회원에게 다시 보이게 된다.
     */
    public void show() {
        validateNotPermanentlyClosed();

        this.hidden = false;
    }

    /**
     * 가게를 폐업 처리한다. <b>되돌릴 수 없다</b> — 폐업 취소 API가 없으므로 이후 {@link #show()}·
     * {@link #update}는 {@link ErrorCode#SHOP_ALREADY_PERMANENTLY_CLOSED}로 거부된다.
     */
    public void close() {
        this.permanentlyClosed = true;
    }

    /**
     * 폐업 상태에서 금지된 동작을 막는다.
     *
     * <p>{@link #close()}(멱등)·{@link #hide()}(추가 은닉)에는 <b>적용하지 않는다</b> — 둘 다 폐업 상태를
     * 되돌리거나 회원에게 다시 노출시키지 않으므로 막을 이유가 없고, 막으면 폐업 API의 멱등성이 깨진다.
     */
    private void validateNotPermanentlyClosed() {
        if (permanentlyClosed) {
            throw new BusinessException(ErrorCode.SHOP_ALREADY_PERMANENTLY_CLOSED);
        }
    }

    public Long getId() {
        return this.id;
    }

    public CeoId getCeoId() {
        return this.ceoId;
    }

    public StationId getStationId() {
        return this.stationId;
    }

    public String getName() {
        return this.name;
    }

    public BigDecimal getLatitude() {
        return this.latitude;
    }

    public BigDecimal getLongitude() {
        return this.longitude;
    }

    public Double getRating() {
        return this.rating;
    }

    public String getRoadAddress() {
        return this.roadAddress;
    }

    public String getLotAddress() {
        return this.lotAddress;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public UploadedFileId getThumbnailImageFileId() {
        return this.thumbnailImageFileId;
    }

    public UploadedFileId getTrademarkImageFileId() {
        return this.trademarkImageFileId;
    }

    public boolean isPermanentlyClosed() {
        return this.permanentlyClosed;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean isClosedOnPublicHolidays() {
        return this.closedOnPublicHolidays;
    }

    public boolean isCupDepositEnabled() {
        return this.cupDepositEnabled;
    }

    public int getMinOrderAmount() {
        return this.minOrderAmount;
    }

    /** 예약주문 운영 여부. {@code false}면 수령시간 예약을 신규로 받지 않는다. */
    public boolean isScheduledOrderEnabled() {
        return this.scheduledOrderEnabled;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
