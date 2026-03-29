package com.tastyhouse.core.repository.point;

import com.tastyhouse.core.entity.point.MemberPointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberPointHistoryJpaRepository extends JpaRepository<MemberPointHistory, Long> {
}
