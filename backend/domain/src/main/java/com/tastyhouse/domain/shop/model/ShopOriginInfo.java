package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게 원산지 표시 정보 순수 도메인 모델(가게당 1건, upsert).
 *
 * <p>원산지는 <b>메뉴 단위가 아니라 가게 단위</b>다. 표시 지침이 "메뉴별로 표시" / "모든 음식에 같으면
 * 일괄 표시"처럼 하나의 문장 안에서 표현되므로, 메뉴별 구조화 데이터로 쪼개면 "일괄 표시" 문장을
 * 담을 수 없다. 영양성분·중량이 메뉴 단위인 것과 대비된다.
 *
 * <p><b>입력 방식은 상호 배타다.</b> {@link OriginSourceType#DIRECT}면 {@code content}만 갖고
 * {@link OriginSourceType#FRANCHISE_URL}이면 {@code url}만 갖는다. 반대편 필드는 이 모델이 null로
 * 정리하므로 "둘 다 채워진 모호한 상태"가 존재하지 않는다 — 그 상태를 허용하면 손님 화면이 어느 쪽을
 * 보여줘야 하는지 판정할 수 없다.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopOriginInfoJpaEntity} + {@code ShopOriginInfoMapper}가 담당한다. 도메인이
 * 프레임워크-프리이므로 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로
 * {@code ShopOriginInfoRepository#save}를 호출해야 한다.
 */
public class ShopOriginInfo {

    private static final int CONTENT_MAX_LENGTH = 2000;
    private static final int URL_MAX_LENGTH = 500;
    private static final String HTTP_SCHEME = "http://";
    private static final String HTTPS_SCHEME = "https://";

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private OriginSourceType sourceType; // 입력 방식 (상태전이로 재대입됨)
    private String content; // 직접 입력 본문 (DIRECT일 때만 값 존재, 최대 2000자)
    private String url; // 본사 제공 URL (FRANCHISE_URL일 때만 값 존재, 최대 500자)
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private ShopOriginInfo(
        Long id,
        ShopId shopId,
        OriginSourceType sourceType,
        String content,
        String url,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.sourceType = sourceType;
        this.content = content;
        this.url = url;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 원산지 정보를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static ShopOriginInfo of(ShopId shopId, OriginSourceType sourceType, String content, String url) {
        validate(sourceType, content, url);

        return new ShopOriginInfo(null, shopId, sourceType, contentFor(sourceType, content), urlFor(sourceType, url),
            null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static ShopOriginInfo reconstitute(
        Long id,
        ShopId shopId,
        OriginSourceType sourceType,
        String content,
        String url,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopOriginInfo(id, shopId, sourceType, content, url, createdAt, updatedAt);
    }

    /**
     * 원산지 정보를 전체 교체한다(PUT 시맨틱). 부분 수정 개념이 없으므로 입력 방식이 바뀌면
     * <b>반대편 필드를 null로 정리</b>한다 — 이전 방식의 값이 남아 있으면 조회 시 어느 쪽이 유효한지
     * 판정할 수 없다.
     */
    public void update(OriginSourceType sourceType, String content, String url) {
        validate(sourceType, content, url);

        this.sourceType = sourceType;
        this.content = contentFor(sourceType, content);
        this.url = urlFor(sourceType, url);
    }

    /**
     * 입력 방식별 필수 필드와 형식을 검증한다. 방식에 해당하지 않는 반대편 필드는 어차피 버려지므로
     * 검증하지 않는다(화면이 이전 값을 남겨 보내도 거절하지 않는다).
     */
    private static void validate(OriginSourceType sourceType, String content, String url) {
        if (sourceType == null) {
            throw new BusinessException(ErrorCode.SHOP_ORIGIN_SOURCE_TYPE_UNKNOWN);
        }
        if (sourceType == OriginSourceType.DIRECT) {
            validateContent(content);
            return;
        }
        validateUrl(url);
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.SHOP_ORIGIN_CONTENT_REQUIRED);
        }
        if (content.length() > CONTENT_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.SHOP_ORIGIN_CONTENT_TOO_LONG);
        }
    }

    private static void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new BusinessException(ErrorCode.SHOP_ORIGIN_URL_REQUIRED);
        }
        if (url.length() > URL_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.SHOP_ORIGIN_URL_TOO_LONG);
        }
        if (!url.startsWith(HTTP_SCHEME) && !url.startsWith(HTTPS_SCHEME)) {
            throw new BusinessException(ErrorCode.SHOP_ORIGIN_URL_INVALID);
        }
    }

    /** 직접 입력 방식이 아니면 본문을 버린다(상호 배타 정리). */
    private static String contentFor(OriginSourceType sourceType, String content) {
        return sourceType == OriginSourceType.DIRECT ? content : null;
    }

    /** 본사 URL 방식이 아니면 URL을 버린다(상호 배타 정리). */
    private static String urlFor(OriginSourceType sourceType, String url) {
        return sourceType == OriginSourceType.FRANCHISE_URL ? url : null;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public OriginSourceType getSourceType() {
        return this.sourceType;
    }

    public String getContent() {
        return this.content;
    }

    public String getUrl() {
        return this.url;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
