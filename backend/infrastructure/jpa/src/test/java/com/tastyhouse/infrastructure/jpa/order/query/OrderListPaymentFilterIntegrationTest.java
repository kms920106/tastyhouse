package com.tastyhouse.infrastructure.jpa.order.query;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.tastyhouse.application.file.port.out.FileDeleteResult;
import com.tastyhouse.application.file.port.out.FileStoragePort;
import com.tastyhouse.application.order.port.out.OrderListItemResult;
import com.tastyhouse.application.order.port.out.OrderListPaymentFilter;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.infrastructure.jpa.file.query.FileUrlResolver;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Testcontainers
@DataJpaTest(properties = {
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=file:${tastyhouse.schema}",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({OrderQueryAdapter.class, FileUrlResolver.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OrderListPaymentFilterIntegrationTest {

    private static final long MEMBER_ID = 7L;
    private static final long OTHER_MEMBER_ID = 8L;
    private static final LocalDateTime BASE = LocalDateTime.of(2026, 10, 29, 9, 0);
    private static final OrderListPaymentFilter FILTER = OrderListPaymentFilter.of(
        List.of("COMPLETED", "CANCELLED"),
        "PENDING",
        List.of("CASH_ON_SITE", "CARD_ON_SITE")
    );

    @Container
    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"));

    @Autowired
    private OrderQueryAdapter orderQueryAdapter;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM PAYMENT WHERE id > 0");
        jdbc.update("DELETE FROM ORDERS WHERE id > 0");
    }

    @Test
    @DisplayName("완료·취소 결제와 현장결제 대기 건은 목록에 나오고, 온라인 대기·실패 건과 다른 회원 주문은 나오지 않는다")
    void findOrders_includesSettledAndOnSitePendingOnly() {
        long completed = insertOrderWithPayment(MEMBER_ID, "ORD-COMPLETED", "CREDIT_CARD", "COMPLETED", BASE.plusMinutes(1));
        long cancelled = insertOrderWithPayment(MEMBER_ID, "ORD-CANCELLED", "KAKAO_PAY", "CANCELLED", BASE.plusMinutes(2));
        long cashOnSite = insertOrderWithPayment(MEMBER_ID, "ORD-CASH-SITE", "CASH_ON_SITE", "PENDING", BASE.plusMinutes(3));
        long cardOnSite = insertOrderWithPayment(MEMBER_ID, "ORD-CARD-SITE", "CARD_ON_SITE", "PENDING", BASE.plusMinutes(4));
        insertOrderWithPayment(MEMBER_ID, "ORD-ONLINE-PENDING", "CREDIT_CARD", "PENDING", BASE.plusMinutes(5));
        insertOrderWithPayment(MEMBER_ID, "ORD-ONSITE-FAILED", "CASH_ON_SITE", "FAILED", BASE.plusMinutes(6));
        insertOrderWithPayment(OTHER_MEMBER_ID, "ORD-OTHER-SITE", "CASH_ON_SITE", "PENDING", BASE.plusMinutes(7));

        PageResult<OrderListItemResult> result = orderQueryAdapter.findOrders(MEMBER_ID, FILTER, PageQuery.of(0, 20));

        assertThat(result.content()).extracting(OrderListItemResult::id)
            .containsExactly(cardOnSite, cashOnSite, cancelled, completed);
        assertThat(result.content()).extracting(OrderListItemResult::paymentStatus)
            .containsExactly("PENDING", "PENDING", "CANCELLED", "COMPLETED");
        assertThat(result.totalElements()).isEqualTo(4L);
    }

    private long insertOrderWithPayment(long memberId, String orderNumber, String paymentMethod, String paymentStatus, LocalDateTime createdAt) {
        jdbc.update("""
            INSERT INTO ORDERS (member_id, shop_id, order_number, order_method, order_status, orderer_name, orderer_phone,
                                final_amount, created_at, updated_at)
            VALUES (?, 1, ?, 'TAKEOUT', 'PENDING', '주문자', '01012345678', 21000, ?, ?)
            """, memberId, orderNumber, createdAt, createdAt);
        long orderId = jdbc.queryForObject("SELECT id FROM ORDERS WHERE order_number = ?", Long.class, orderNumber);
        jdbc.update("""
            INSERT INTO PAYMENT (order_id, payment_method, payment_status, amount, created_at, updated_at)
            VALUES (?, ?, ?, 21000, ?, ?)
            """, orderId, paymentMethod, paymentStatus, createdAt, createdAt);
        return orderId;
    }

    @SpringBootConfiguration
    @EntityScan(basePackages = {
        "com.tastyhouse.infrastructure.jpa.order.persistence",
        "com.tastyhouse.infrastructure.jpa.payment.persistence",
        "com.tastyhouse.infrastructure.jpa.shop.persistence",
        "com.tastyhouse.infrastructure.jpa.file.persistence"
    })
    @EnableJpaRepositories(basePackageClasses = OrderListPaymentFilterIntegrationTest.class)
    static class OrderQueryTestConfig {

        @Bean
        JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
            return new JPAQueryFactory(entityManager);
        }

        @Bean
        FileStoragePort fileStoragePort() {
            return new FileStoragePort() {
                @Override
                public String store(byte[] content, String storedFilename, String datePath, String contentType) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public String getFileUrl(String filePath) {
                    return filePath;
                }

                @Override
                public FileDeleteResult delete(String filePath) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
