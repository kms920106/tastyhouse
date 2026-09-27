package com.tastyhouse.application.menureview.port.out.write;

import java.util.Optional;

public interface MenuReviewStatePort {
    Optional<MenuReviewState> findById(Long id);

    Optional<MenuReviewState> findByIdAndMemberId(Long id, Long memberId);

    boolean existsByOrderProductId(Long orderProductId);

    MenuReviewState save(MenuReviewState state);

    void deleteById(Long id);
}
