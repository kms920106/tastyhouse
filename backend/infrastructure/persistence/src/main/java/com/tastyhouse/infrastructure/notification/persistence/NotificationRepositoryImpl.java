package com.tastyhouse.infrastructure.notification.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.notification.model.Notification;
import com.tastyhouse.domain.notification.repository.NotificationRepository;
import com.tastyhouse.domain.notification.vo.NotificationId;

import static com.tastyhouse.infrastructure.notification.persistence.QNotificationJpaEntity.notificationJpaEntity;

@Repository
public class NotificationRepositoryImpl implements NotificationRepository {
    private final JPAQueryFactory queryFactory;
    private final NotificationJpaRepository notificationJpaRepository;

    public NotificationRepositoryImpl(JPAQueryFactory queryFactory, NotificationJpaRepository notificationJpaRepository) {
        this.queryFactory = queryFactory;
        this.notificationJpaRepository = notificationJpaRepository;
    }

    @Override
    public Optional<Notification> findById(NotificationId notificationId) {
        return notificationJpaRepository.findById(notificationId.value())
            .map(NotificationMapper::toDomain);
    }

    @Override
    public List<Notification> findUnreadByMemberId(MemberId memberId) {
        return queryFactory
            .selectFrom(notificationJpaEntity)
            .where(
                notificationJpaEntity.memberId.eq(memberId.value()),
                notificationJpaEntity.read.isFalse()
            )
            .fetch()
            .stream()
            .map(NotificationMapper::toDomain)
            .toList();
    }

    @Override
    public Notification save(Notification notification) {
        if (notification.getId() == null) {
            NotificationJpaEntity saved = notificationJpaRepository.save(NotificationMapper.toEntity(notification));
            return NotificationMapper.toDomain(saved);
        }

        NotificationJpaEntity entity = notificationJpaRepository.findById(notification.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 알림입니다: " + notification.getId()));
        NotificationMapper.applyChanges(entity, notification);
        return NotificationMapper.toDomain(entity);
    }

    @Override
    public List<Notification> saveAll(List<Notification> notifications) {
        return notifications.stream()
            .map(this::save)
            .toList();
    }
}
