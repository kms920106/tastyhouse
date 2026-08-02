package com.tastyhouse.infrastructure.rank.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.rank.domain.vo.RankPeriodId;
import com.tastyhouse.infrastructure.file.persistence.UploadedFileIdConverter;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 랭킹 경품 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code RankPrize}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code RankPrizeMapper}가 수행한다.
 */
@Getter
@Entity
@Table(
    name = "RANK_PRIZE",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_rank_prize_rank", columnNames = {"rank_id", "prize_rank"})
    },
    indexes = {
        @Index(name = "idx_rank_prize", columnList = "rank_id, prize_rank")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankPrizeJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = RankPeriodIdConverter.class)
    @Column(name = "rank_id", nullable = false)
    private RankPeriodId rankId;

    @Column(name = "prize_rank", nullable = false)
    private Integer prizeRank;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    @Convert(converter = UploadedFileIdConverter.class)
    @Column(name = "image_file_id")
    private UploadedFileId imageFileId;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    private RankPrizeJpaEntity(
        RankPeriodId rankId,
        Integer prizeRank,
        String name,
        String brand,
        UploadedFileId imageFileId,
        boolean deleted
    ) {
        this.rankId = rankId;
        this.prizeRank = prizeRank;
        this.name = name;
        this.brand = brand;
        this.imageFileId = imageFileId;
        this.deleted = deleted;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code RankPrizeMapper#toEntity}에서만 호출한다.
     */
    static RankPrizeJpaEntity create(
        RankPeriodId rankId,
        Integer prizeRank,
        String name,
        String brand,
        UploadedFileId imageFileId,
        boolean deleted
    ) {
        return new RankPrizeJpaEntity(rankId, prizeRank, name, brand, imageFileId, deleted);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(Integer prizeRank, String name, String brand, UploadedFileId imageFileId, boolean deleted) {
        this.prizeRank = prizeRank;
        this.name = name;
        this.brand = brand;
        this.imageFileId = imageFileId;
        this.deleted = deleted;
    }
}
