package com.tastyhouse.infrastructure.persistence.holiday.persistence;

import java.time.LocalDate;
import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.holiday.model.PublicHoliday;
import com.tastyhouse.application.holiday.port.out.write.PublicHolidayLoadPort;

import static com.tastyhouse.infrastructure.persistence.holiday.persistence.QPublicHolidayJpaEntity.publicHolidayJpaEntity;

@Repository
class PublicHolidayPersistenceAdapter implements PublicHolidayLoadPort {

    private final JPAQueryFactory queryFactory;

    public PublicHolidayPersistenceAdapter(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public boolean existsByHolidayDate(LocalDate holidayDate) {
        return queryFactory.selectOne()
            .from(publicHolidayJpaEntity)
            .where(publicHolidayJpaEntity.holidayDate.eq(holidayDate))
            .fetchFirst() != null;
    }

    @Override
    public List<PublicHoliday> findAllByHolidayDateBetween(LocalDate from, LocalDate to) {
        return queryFactory.selectFrom(publicHolidayJpaEntity)
            .where(publicHolidayJpaEntity.holidayDate.between(from, to))
            .fetch()
            .stream()
            .map(PublicHolidayMapper::toDomain)
            .toList();
    }
}
