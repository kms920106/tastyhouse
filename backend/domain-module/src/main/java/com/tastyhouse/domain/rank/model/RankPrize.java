package com.tastyhouse.domain.rank.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.domain.rank.vo.RankPrizeId;

/**
 * 랭킹 경품 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code RankPrizeJpaEntity} + {@code RankPrizeMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code RankPrizeRepository#save}를
 * 호출해야 한다.
 */
public class RankPrize {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final RankPeriodId rankId; // 랭킹 기간 ID (RankPeriod 참조)
    private Integer prizeRank; // 수상 순위
    private String name; // 경품 이름
    private String brand; // 경품 브랜드
    private UploadedFileId imageFileId; // 경품 이미지 파일 ID
    private boolean deleted; // 삭제 여부 (true: 삭제됨, Soft Delete)
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private RankPrize(
        Long id,
        RankPeriodId rankId,
        Integer prizeRank,
        String name,
        String brand,
        UploadedFileId imageFileId,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.rankId = rankId;
        this.prizeRank = prizeRank;
        this.name = name;
        this.brand = brand;
        this.imageFileId = imageFileId;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 랭킹 경품을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static RankPrize of(RankPeriodId rankId, Integer prizeRank, String name, String brand, UploadedFileId imageFileId) {
        return new RankPrize(null, rankId, prizeRank, name, brand, imageFileId, false, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static RankPrize reconstitute(
        Long id,
        RankPeriodId rankId,
        Integer prizeRank,
        String name,
        String brand,
        UploadedFileId imageFileId,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new RankPrize(id, rankId, prizeRank, name, brand, imageFileId, deleted, createdAt, updatedAt);
    }

    public RankPrizeId getRankPrizeId() {
        return RankPrizeId.of(this.id);
    }

    public void update(Integer prizeRank, String name, String brand, UploadedFileId imageFileId) {
        this.prizeRank = prizeRank;
        this.name = name;
        this.brand = brand;
        this.imageFileId = imageFileId;
    }

    public void delete() {
        this.deleted = true;
    }

    public Long getId() {
        return this.id;
    }

    public RankPeriodId getRankId() {
        return this.rankId;
    }

    public Integer getPrizeRank() {
        return this.prizeRank;
    }

    public String getName() {
        return this.name;
    }

    public String getBrand() {
        return this.brand;
    }

    public UploadedFileId getImageFileId() {
        return this.imageFileId;
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
