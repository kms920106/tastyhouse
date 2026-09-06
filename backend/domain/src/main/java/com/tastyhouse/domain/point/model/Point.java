package com.tastyhouse.domain.point.model;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public class Point {
    private final Long id;
    private final MemberId memberId;
    private Integer availablePoints;
    private final Integer expiredThisMonth;

    private Point(Long id, MemberId memberId, Integer availablePoints, Integer expiredThisMonth) {
        this.id = id;
        this.memberId = memberId;
        this.availablePoints = availablePoints != null ? availablePoints : 0;
        this.expiredThisMonth = expiredThisMonth != null ? expiredThisMonth : 0;
    }

    public static Point of(MemberId memberId) {
        return new Point(null, memberId, 0, 0);
    }

    public static Point reconstitute(Long id, MemberId memberId, Integer availablePoints, Integer expiredThisMonth) {
        return new Point(id, memberId, availablePoints, expiredThisMonth);
    }

    public void addPoints(Integer amount) {
        this.availablePoints += amount;
    }

    public void deductPoints(Integer amount) {
        if (this.availablePoints < amount) {
            throw new BusinessException(ErrorCode.POINT_INSUFFICIENT);
        }
        this.availablePoints -= amount;
    }

    public Long getId() {
        return this.id;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public Integer getAvailablePoints() {
        return this.availablePoints;
    }

    public Integer getExpiredThisMonth() {
        return this.expiredThisMonth;
    }
}
