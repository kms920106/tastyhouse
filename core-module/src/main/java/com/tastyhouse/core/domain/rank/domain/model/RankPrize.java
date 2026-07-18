package com.tastyhouse.core.domain.rank.domain.model;

import jakarta.persistence.Column;
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

import com.tastyhouse.core.domain.rank.domain.vo.RankPrizeId;
import com.tastyhouse.core.shared.entity.BaseEntity;

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
public class RankPrize extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rank_id", nullable = false)
    private Long rankId;

    @Column(name = "prize_rank", nullable = false)
    private Integer prizeRank;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    @Column(name = "image_file_id")
    private Long imageFileId;

    private RankPrize(Long rankId, Integer prizeRank, String name, String brand, Long imageFileId) {
        this.rankId = rankId;
        this.prizeRank = prizeRank;
        this.name = name;
        this.brand = brand;
        this.imageFileId = imageFileId;
    }

    public static RankPrize of(Long rankId, Integer prizeRank, String name, String brand, Long imageFileId) {
        return new RankPrize(rankId, prizeRank, name, brand, imageFileId);
    }

    public RankPrizeId getRankPrizeId() {
        return RankPrizeId.of(this.id);
    }

    public void update(Integer prizeRank, String name, String brand, Long imageFileId) {
        this.prizeRank = prizeRank;
        this.name = name;
        this.brand = brand;
        this.imageFileId = imageFileId;
    }
}
