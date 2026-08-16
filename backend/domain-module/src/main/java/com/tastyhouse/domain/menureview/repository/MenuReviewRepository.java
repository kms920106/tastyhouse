package com.tastyhouse.domain.menureview.repository;

import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.model.MenuReview;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.order.vo.OrderProductId;

/**
 * 메뉴 평가 write 포트.
 *
 * <p>{@code existsByOrderProductId}는 조회처럼 보이지만 "주문 항목당 평가 1건"이라는 <b>불변식</b> 검증이라
 * 여기 남는다(write 포트 잔류 판정 기준). {@code findByIdAndMemberId} 역시 소유권 검증과 전이 대상 로드를
 * 겸하는 command 경로 전용이다.
 *
 * <p>반면 목록·상세·평가 가능 메뉴 목록·집계는 전부 표현 목적이므로
 * {@code infrastructure/menureview/query/}의 DAO가 담당한다.
 */
public interface MenuReviewRepository {

    Optional<MenuReview> findById(MenuReviewId menuReviewId);

    Optional<MenuReview> findByIdAndMemberId(MenuReviewId menuReviewId, MemberId memberId);

    boolean existsByOrderProductId(OrderProductId orderProductId);

    MenuReview save(MenuReview menuReview);

    void deleteById(MenuReviewId menuReviewId);
}
