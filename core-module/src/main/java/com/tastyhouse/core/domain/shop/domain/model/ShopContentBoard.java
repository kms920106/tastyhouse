package com.tastyhouse.core.domain.shop.domain.model;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import lombok.Getter;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

/**
 * 가게 콘텐츠보드 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopContentBoardJpaEntity} + {@code ShopContentBoardMapper}가 담당한다. 도메인이
 * 프레임워크-프리이므로 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로
 * {@code ShopContentBoardRepository#save}를 호출해야 한다.
 */
@Getter
public class ShopContentBoard {

    private static final int MAX_DESCRIPTION_LENGTH = 50;
    private static final Pattern YOUTUBE_URL_PATTERN =
        Pattern.compile("^https?://(www\\.)?(youtube\\.com/watch|youtu\\.be/).+$");

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final Long shopId;
    private final ShopContentType contentType; // 생성 이후 불변
    private ShopContentTopic topic;
    private Long imageFileId;
    private String youtubeUrl;
    private String description;
    private boolean hidden; // 관리자 숨김 조치
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private ShopContentBoard(
        Long id,
        Long shopId,
        ShopContentType contentType,
        ShopContentTopic topic,
        Long imageFileId,
        String youtubeUrl,
        String description,
        boolean hidden,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.contentType = contentType;
        this.topic = topic;
        this.imageFileId = imageFileId;
        this.youtubeUrl = youtubeUrl;
        this.description = description;
        this.hidden = hidden;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 콘텐츠보드를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static ShopContentBoard of(
        Long shopId,
        ShopContentType contentType,
        ShopContentTopic topic,
        Long imageFileId,
        String youtubeUrl,
        String description
    ) {
        validate(contentType, imageFileId, youtubeUrl, description);
        return new ShopContentBoard(null, shopId, contentType, topic, imageFileId, youtubeUrl, description, false, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static ShopContentBoard reconstitute(
        Long id,
        Long shopId,
        ShopContentType contentType,
        ShopContentTopic topic,
        Long imageFileId,
        String youtubeUrl,
        String description,
        boolean hidden,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopContentBoard(id, shopId, contentType, topic, imageFileId, youtubeUrl, description, hidden, createdAt, updatedAt);
    }

    public void update(ShopContentTopic topic, Long imageFileId, String youtubeUrl, String description) {
        validate(this.contentType, imageFileId, youtubeUrl, description);
        this.topic = topic;
        this.imageFileId = imageFileId;
        this.youtubeUrl = youtubeUrl;
        this.description = description;
    }

    public void hide() {
        this.hidden = true;
    }

    public void unhide() {
        this.hidden = false;
    }

    private static void validate(ShopContentType contentType, Long imageFileId, String youtubeUrl, String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new BusinessException(ErrorCode.SHOP_CONTENT_DESCRIPTION_TOO_LONG);
        }

        if (contentType == ShopContentType.VIDEO) {
            if (!isValidYoutubeUrl(youtubeUrl)) {
                throw new BusinessException(ErrorCode.SHOP_CONTENT_YOUTUBE_URL_INVALID);
            }
        } else if (imageFileId == null) {
            throw new BusinessException(ErrorCode.SHOP_IMAGE_SPEC_INVALID);
        }
    }

    /**
     * 유튜브 영상 링크(youtube.com/watch 또는 youtu.be/) 형식인지 검증한다.
     */
    public static boolean isValidYoutubeUrl(String url) {
        return url != null && YOUTUBE_URL_PATTERN.matcher(url).matches();
    }
}
