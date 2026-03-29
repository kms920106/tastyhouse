package com.tastyhouse.core.repository.follow;

import com.tastyhouse.core.entity.follow.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowJpaRepository extends JpaRepository<Follow, Long> {
}
