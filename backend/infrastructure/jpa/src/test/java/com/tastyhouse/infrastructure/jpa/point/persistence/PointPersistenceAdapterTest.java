package com.tastyhouse.infrastructure.jpa.point.persistence;

import java.time.LocalDateTime;
import java.util.Optional;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.point.model.Point;

import static com.tastyhouse.infrastructure.jpa.point.persistence.QPointJpaEntity.pointJpaEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PointPersistenceAdapterTest {

    private final JPAQueryFactory queryFactory = mock(JPAQueryFactory.class);
    private final PointJpaRepository pointJpaRepository = mock(PointJpaRepository.class);
    private final EntityManager entityManager = mock(EntityManager.class);
    private final JPAQuery<PointJpaEntity> query = mock();

    private final PointPersistenceAdapter adapter = new PointPersistenceAdapter(
        queryFactory, pointJpaRepository, entityManager);

    @BeforeEach
    void setUp() {
        when(queryFactory.selectFrom(pointJpaEntity)).thenReturn(query);
        when(query.where(any(Predicate.class))).thenReturn(query);
        when(query.setLockMode(any(LockModeType.class))).thenReturn(query);
    }

    @Test
    @DisplayName("잔액 로드는 PESSIMISTIC_WRITE 락을 건 뒤 조회한다")
    void acquiresPessimisticWriteLockBeforeFetch() {
        when(query.fetchOne()).thenReturn(null);

        adapter.findByMemberIdForUpdate(MemberId.of(12L));

        var ordered = inOrder(query);
        ordered.verify(query).setLockMode(LockModeType.PESSIMISTIC_WRITE);
        ordered.verify(query).fetchOne();
    }

    @Test
    @DisplayName("포인트 행이 없으면 빈 Optional을 돌려준다")
    void returnsEmptyWhenAbsent() {
        when(query.fetchOne()).thenReturn(null);

        assertThat(adapter.findByMemberIdForUpdate(MemberId.of(12L))).isEmpty();
    }

    @Test
    @DisplayName("포인트 행이 있으면 도메인 Point로 매핑해 돌려준다")
    void mapsLockedEntityToDomain() {
        PointJpaEntity entity = PointJpaEntity.create(12L, 1000, 0);
        ReflectionTestUtils.setField(entity, "id", 11L);
        when(query.fetchOne()).thenReturn(entity);

        Optional<Point> loaded = adapter.findByMemberIdForUpdate(MemberId.of(12L));

        assertThat(loaded).get()
            .usingRecursiveComparison()
            .isEqualTo(Point.reconstitute(11L, MemberId.of(12L), 1000, 0));
    }

    @Test
    @DisplayName("행 보장은 회원 ID와 현재 시각을 바인딩한 ON DUPLICATE KEY UPDATE upsert 한 문장으로 실행한다")
    void saveIfAbsentExecutesSingleUpsertStatement() {
        Query nativeQuery = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);

        adapter.saveIfAbsent(MemberId.of(12L));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sql.capture());
        assertThat(sql.getValue())
            .contains("INSERT INTO POINT")
            .contains("ON DUPLICATE KEY UPDATE member_id = member_id")
            .doesNotContain("IGNORE");
        verify(nativeQuery).setParameter("memberId", 12L);
        verify(nativeQuery).setParameter(eq("now"), any(LocalDateTime.class));
        verify(nativeQuery).executeUpdate();
    }
}
