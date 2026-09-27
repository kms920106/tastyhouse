package com.tastyhouse.infrastructure.notification.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.notification.port.out.write.NotificationState;
import com.tastyhouse.application.notification.port.out.write.NotificationStatePort;

import static com.tastyhouse.infrastructure.notification.persistence.QNotificationJpaEntity.notificationJpaEntity;

@Repository
public class NotificationStatePortImpl implements NotificationStatePort {
    private final JPAQueryFactory queryFactory;
    private final NotificationJpaRepository notificationJpaRepository;

    public NotificationStatePortImpl(JPAQueryFactory queryFactory, NotificationJpaRepository notificationJpaRepository) {
        this.queryFactory = queryFactory;
        this.notificationJpaRepository = notificationJpaRepository;
    }

    @Override
    public Optional<NotificationState> findById(Long id) {
        return notificationJpaRepository.findById(id)
            .map(NotificationMapper::toState);
    }

    @Override
    public List<NotificationState> findUnreadByMemberId(Long memberId) {
        return queryFactory
            .selectFrom(notificationJpaEntity)
            .where(
                notificationJpaEntity.memberId.eq(memberId),
                notificationJpaEntity.read.isFalse()
            )
            .fetch()
            .stream()
            .map(NotificationMapper::toState)
            .toList();
    }

    @Override
    public NotificationState save(NotificationState state) {
        if (state.id() == null) {
            NotificationJpaEntity saved = notificationJpaRepository.save(NotificationMapper.toEntity(state));
            return NotificationMapper.toState(saved);
        }

        NotificationJpaEntity entity = notificationJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 알림입니다: " + state.id()));
        NotificationMapper.applyChanges(entity, state);
        return NotificationMapper.toState(entity);
    }

    @Override
    public List<NotificationState> saveAll(List<NotificationState> states) {
        return states.stream()
            .map(this::save)
            .toList();
    }
}
