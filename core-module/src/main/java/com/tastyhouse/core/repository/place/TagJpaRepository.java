package com.tastyhouse.core.repository.place;

import com.tastyhouse.core.entity.place.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagJpaRepository extends JpaRepository<Tag, Long> {
}
