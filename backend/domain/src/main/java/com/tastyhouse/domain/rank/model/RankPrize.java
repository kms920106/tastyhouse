package com.tastyhouse.domain.rank.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.domain.rank.vo.RankPrizeId;

public class RankPrize {
    private final Long id;
    private final RankPeriodId rankId;
    private Integer prizeRank;
    private String name;
    private String brand;
    private UploadedFileId imageFileId;
    private boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

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

    public static RankPrize of(RankPeriodId rankId, Integer prizeRank, String name, String brand, UploadedFileId imageFileId) {
        return new RankPrize(null, rankId, prizeRank, name, brand, imageFileId, false, null, null);
    }

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
