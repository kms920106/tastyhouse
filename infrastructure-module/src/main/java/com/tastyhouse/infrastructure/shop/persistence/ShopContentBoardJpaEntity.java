package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.domain.shop.domain.model.ShopContentType;
import com.tastyhouse.domain.shop.domain.model.ShopContentTopic;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;
import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.infrastructure.file.persistence.UploadedFileIdConverter;

/**
 * 가게 콘텐츠보드 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ShopContentBoard}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ShopContentBoardMapper}가 수행한다.
 */
@Getter
@Entity
@Table(name = "SHOP_CONTENT_BOARD")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopContentBoardJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = ShopIdConverter.class)
    @Column(name = "shop_id", nullable = false)
    private ShopId shopId;

    @Column(name = "content_type", nullable = false, length = 10, columnDefinition = "VARCHAR(10)")
    @Enumerated(EnumType.STRING)
    private ShopContentType contentType;

    @Column(name = "topic", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    @Enumerated(EnumType.STRING)
    private ShopContentTopic topic;

    @Convert(converter = UploadedFileIdConverter.class)
    @Column(name = "image_file_id")
    private UploadedFileId imageFileId;

    @Column(name = "youtube_url", length = 500)
    private String youtubeUrl;

    @Column(name = "description", length = 50)
    private String description;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    private ShopContentBoardJpaEntity(
        ShopId shopId,
        ShopContentType contentType,
        ShopContentTopic topic,
        UploadedFileId imageFileId,
        String youtubeUrl,
        String description,
        boolean hidden
    ) {
        this.shopId = shopId;
        this.contentType = contentType;
        this.topic = topic;
        this.imageFileId = imageFileId;
        this.youtubeUrl = youtubeUrl;
        this.description = description;
        this.hidden = hidden;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ShopContentBoardMapper#toEntity}에서만 호출한다.
     */
    static ShopContentBoardJpaEntity create(
        ShopId shopId,
        ShopContentType contentType,
        ShopContentTopic topic,
        UploadedFileId imageFileId,
        String youtubeUrl,
        String description,
        boolean hidden
    ) {
        return new ShopContentBoardJpaEntity(shopId, contentType, topic, imageFileId, youtubeUrl, description, hidden);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·contentType은 건드리지 않는다.
     */
    void applyChanges(ShopContentTopic topic, UploadedFileId imageFileId, String youtubeUrl, String description, boolean hidden) {
        this.topic = topic;
        this.imageFileId = imageFileId;
        this.youtubeUrl = youtubeUrl;
        this.description = description;
        this.hidden = hidden;
    }
}
