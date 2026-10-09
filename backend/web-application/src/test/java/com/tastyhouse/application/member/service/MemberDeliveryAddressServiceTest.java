package com.tastyhouse.application.member.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.geo.GeoBoundingBox;
import com.tastyhouse.application.member.port.out.write.MemberDeliveryAddressLoadPort;
import com.tastyhouse.application.member.port.out.write.MemberDeliveryAddressSavePort;
import com.tastyhouse.application.region.port.out.write.AdminDongLoadPort;
import com.tastyhouse.application.region.port.out.write.AdminDongSavePort;
import com.tastyhouse.application.region.port.out.write.AdminDongSyncResult;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberDeliveryAddressServiceTest {

    private static final MemberId MEMBER_ID = MemberId.of(1L);
    private static final MemberId OTHER_MEMBER_ID = MemberId.of(2L);
    private static final BigDecimal LATITUDE = new BigDecimal("37.501234");
    private static final BigDecimal LONGITUDE = new BigDecimal("127.039876");
    private static final String ROAD_ADDRESS = "서울특별시 강남구 테헤란로 123";
    private static final Long GANGNAM_ADMIN_DONG_ID = 1168064000L;

    @Nested
    @DisplayName("등록(create)")
    class Create {

        @Test
        @DisplayName("10건이 이미 있으면 한도 초과로 거부한다")
        void create_rejectsWhenLimitExceeded() {
            FakeMemberDeliveryAddressPersistence addressPersistence = new FakeMemberDeliveryAddressPersistence();
            for (int i = 0; i < 10; i++) {
                addressPersistence.save(newAddress(MEMBER_ID, false));
            }
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressPersistence,
                addressPersistence, new FakeAdminDongPersistence()
            );

            assertThatThrownBy(() -> create(service, false))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(WebErrorCode.MEMBER_DELIVERY_ADDRESS_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("9건까지는 등록되어 10건째가 마지막이다")
        void create_allowsUpToLimit() {
            FakeMemberDeliveryAddressPersistence addressPersistence = new FakeMemberDeliveryAddressPersistence();
            for (int i = 0; i < 9; i++) {
                addressPersistence.save(newAddress(MEMBER_ID, false));
            }
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressPersistence,
                addressPersistence, new FakeAdminDongPersistence()
            );

            Long createdId = create(service, false);

            assertThat(createdId).isNotNull();
            assertThat(addressPersistence.countByMemberId(MEMBER_ID)).isEqualTo(10L);
        }

        @Test
        @DisplayName("다른 회원의 주소는 한도 계산에 포함하지 않는다")
        void create_countsOnlyOwnAddresses() {
            FakeMemberDeliveryAddressPersistence addressPersistence = new FakeMemberDeliveryAddressPersistence();
            for (int i = 0; i < 10; i++) {
                addressPersistence.save(newAddress(OTHER_MEMBER_ID, false));
            }
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressPersistence,
                addressPersistence, new FakeAdminDongPersistence()
            );

            assertThat(create(service, false)).isNotNull();
        }

        @Test
        @DisplayName("기본 배송지로 등록하면 기존 기본 배송지가 해제되어 항상 1건만 남는다")
        void create_unmarksPreviousDefault() {
            FakeMemberDeliveryAddressPersistence addressPersistence = new FakeMemberDeliveryAddressPersistence();
            MemberDeliveryAddress previousDefault = addressPersistence.save(newAddress(MEMBER_ID, true));
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressPersistence,
                addressPersistence, new FakeAdminDongPersistence()
            );

            create(service, true);

            assertThat(addressPersistence.findById(previousDefault.getId()).orElseThrow().isDefaultAddress()).isFalse();
            assertThat(defaultAddressCount(addressPersistence)).isEqualTo(1);
        }

        @Test
        @DisplayName("행정동 매칭에 성공하면 adminDongId를 채운다")
        void create_fillsAdminDongIdOnMatch() {
            FakeMemberDeliveryAddressPersistence addressPersistence = new FakeMemberDeliveryAddressPersistence();
            FakeAdminDongPersistence adminDongPersistence = new FakeAdminDongPersistence();
            adminDongPersistence.registerInGangnam("테헤란로");
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressPersistence,
                addressPersistence, adminDongPersistence
            );

            Long createdId = create(service, false);

            assertThat(addressPersistence.findById(createdId).orElseThrow().getAdminDongId())
                .isEqualTo(AdminDongId.of(GANGNAM_ADMIN_DONG_ID));
        }

        @Test
        @DisplayName("도로명 주소로 매칭에 실패하면 지번 주소로 재시도한다")
        void create_fallsBackToLotAddress() {
            FakeMemberDeliveryAddressPersistence addressPersistence = new FakeMemberDeliveryAddressPersistence();
            FakeAdminDongPersistence adminDongPersistence = new FakeAdminDongPersistence();
            adminDongPersistence.registerInGangnam("역삼1동");
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressPersistence,
                addressPersistence, adminDongPersistence
            );

            Long createdId = service.create(
                MEMBER_ID, "집", ROAD_ADDRESS, "서울특별시 강남구 역삼1동 678-9", "101동", LATITUDE, LONGITUDE, false
            );

            assertThat(addressPersistence.findById(createdId).orElseThrow().getAdminDongId())
                .isEqualTo(AdminDongId.of(GANGNAM_ADMIN_DONG_ID));
        }

        @Test
        @DisplayName("행정동 매칭에 실패해도 예외 없이 adminDongId를 null로 두고 등록한다")
        void create_allowsNullAdminDongIdOnMatchFailure() {
            FakeMemberDeliveryAddressPersistence addressPersistence = new FakeMemberDeliveryAddressPersistence();
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressPersistence,
                addressPersistence, new FakeAdminDongPersistence()
            );

            Long createdId = create(service, false);

            assertThat(addressPersistence.findById(createdId).orElseThrow().getAdminDongId()).isNull();
        }
    }

    @Nested
    @DisplayName("수정(update)")
    class Update {

        @Test
        @DisplayName("타인의 주소를 수정하면 접근 거부한다")
        void update_rejectsOtherMembersAddress() {
            FakeMemberDeliveryAddressPersistence addressPersistence = new FakeMemberDeliveryAddressPersistence();
            MemberDeliveryAddress othersAddress = addressPersistence.save(newAddress(OTHER_MEMBER_ID, false));
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressPersistence,
                addressPersistence, new FakeAdminDongPersistence()
            );

            assertThatThrownBy(() -> service.update(
                MEMBER_ID, othersAddress.getId(), "회사", ROAD_ADDRESS, null, null, LATITUDE, LONGITUDE
            ))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(WebErrorCode.MEMBER_DELIVERY_ADDRESS_ACCESS_DENIED);
        }

        @Test
        @DisplayName("존재하지 않는 주소를 수정하면 404다")
        void update_rejectsMissingAddress() {
            FakeMemberDeliveryAddressPersistence fakeMemberDeliveryAddressPersistence = new FakeMemberDeliveryAddressPersistence();
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                fakeMemberDeliveryAddressPersistence,
                fakeMemberDeliveryAddressPersistence, new FakeAdminDongPersistence()
            );

            assertThatThrownBy(() -> service.update(
                MEMBER_ID, 999L, "회사", ROAD_ADDRESS, null, null, LATITUDE, LONGITUDE
            )).isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("본인 주소는 수정되고 명시적으로 저장된다")
        void update_savesOwnAddress() {
            FakeMemberDeliveryAddressPersistence addressPersistence = new FakeMemberDeliveryAddressPersistence();
            MemberDeliveryAddress address = addressPersistence.save(newAddress(MEMBER_ID, false));
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressPersistence,
                addressPersistence, new FakeAdminDongPersistence()
            );
            addressPersistence.saveCount = 0;

            service.update(
                MEMBER_ID, address.getId(), "회사", "서울특별시 강남구 테헤란로 500", null, "10층", LATITUDE, LONGITUDE
            );

            assertThat(addressPersistence.saveCount).isEqualTo(1);
            MemberDeliveryAddress updated = addressPersistence.findById(address.getId()).orElseThrow();
            assertThat(updated.getAlias()).isEqualTo("회사");
            assertThat(updated.getRoadAddress()).isEqualTo("서울특별시 강남구 테헤란로 500");
        }
    }

    @Nested
    @DisplayName("삭제(delete)")
    class Delete {

        @Test
        @DisplayName("타인의 주소를 삭제하면 접근 거부한다")
        void delete_rejectsOtherMembersAddress() {
            FakeMemberDeliveryAddressPersistence addressPersistence = new FakeMemberDeliveryAddressPersistence();
            MemberDeliveryAddress othersAddress = addressPersistence.save(newAddress(OTHER_MEMBER_ID, false));
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressPersistence,
                addressPersistence, new FakeAdminDongPersistence()
            );

            assertThatThrownBy(() -> service.delete(MEMBER_ID, othersAddress.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(WebErrorCode.MEMBER_DELIVERY_ADDRESS_ACCESS_DENIED);
            assertThat(addressPersistence.findById(othersAddress.getId())).isPresent();
        }

        @Test
        @DisplayName("본인 주소는 삭제된다")
        void delete_removesOwnAddress() {
            FakeMemberDeliveryAddressPersistence addressPersistence = new FakeMemberDeliveryAddressPersistence();
            MemberDeliveryAddress address = addressPersistence.save(newAddress(MEMBER_ID, false));
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressPersistence,
                addressPersistence, new FakeAdminDongPersistence()
            );

            service.delete(MEMBER_ID, address.getId());

            assertThat(addressPersistence.findById(address.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("기본 배송지 변경(changeDefault)")
    class ChangeDefault {

        @Test
        @DisplayName("새 기본을 지정하면 기존 기본이 해제되어 회원당 1건만 남는다")
        void changeDefault_keepsSingleDefault() {
            FakeMemberDeliveryAddressPersistence addressPersistence = new FakeMemberDeliveryAddressPersistence();
            MemberDeliveryAddress previousDefault = addressPersistence.save(newAddress(MEMBER_ID, true));
            MemberDeliveryAddress target = addressPersistence.save(newAddress(MEMBER_ID, false));
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressPersistence,
                addressPersistence, new FakeAdminDongPersistence()
            );

            service.changeDefault(MEMBER_ID, target.getId());

            assertThat(addressPersistence.findById(previousDefault.getId()).orElseThrow().isDefaultAddress()).isFalse();
            assertThat(addressPersistence.findById(target.getId()).orElseThrow().isDefaultAddress()).isTrue();
            assertThat(defaultAddressCount(addressPersistence)).isEqualTo(1);
        }

        @Test
        @DisplayName("기존 기본이 없어도 새 기본을 지정할 수 있다")
        void changeDefault_worksWithoutExistingDefault() {
            FakeMemberDeliveryAddressPersistence addressPersistence = new FakeMemberDeliveryAddressPersistence();
            MemberDeliveryAddress target = addressPersistence.save(newAddress(MEMBER_ID, false));
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressPersistence,
                addressPersistence, new FakeAdminDongPersistence()
            );

            service.changeDefault(MEMBER_ID, target.getId());

            assertThat(addressPersistence.findById(target.getId()).orElseThrow().isDefaultAddress()).isTrue();
        }

        @Test
        @DisplayName("타인의 주소를 기본으로 지정하면 접근 거부한다")
        void changeDefault_rejectsOtherMembersAddress() {
            FakeMemberDeliveryAddressPersistence addressPersistence = new FakeMemberDeliveryAddressPersistence();
            MemberDeliveryAddress othersAddress = addressPersistence.save(newAddress(OTHER_MEMBER_ID, false));
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressPersistence,
                addressPersistence, new FakeAdminDongPersistence()
            );

            assertThatThrownBy(() -> service.changeDefault(MEMBER_ID, othersAddress.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(WebErrorCode.MEMBER_DELIVERY_ADDRESS_ACCESS_DENIED);
        }
    }

    private static Long create(MemberDeliveryAddressService service, boolean isDefault) {
        return service.create(MEMBER_ID, "집", ROAD_ADDRESS, null, "101동 1001호", LATITUDE, LONGITUDE, isDefault);
    }

    private static MemberDeliveryAddress newAddress(MemberId memberId, boolean isDefault) {
        return MemberDeliveryAddress.of(
            memberId, "집", ROAD_ADDRESS, null, null, null, LATITUDE, LONGITUDE, isDefault
        );
    }

    private static long defaultAddressCount(FakeMemberDeliveryAddressPersistence repository) {
        return repository.findByMemberId(MEMBER_ID).stream().filter(MemberDeliveryAddress::isDefaultAddress).count();
    }

    private static final class FakeMemberDeliveryAddressPersistence implements MemberDeliveryAddressLoadPort, MemberDeliveryAddressSavePort {

        private final Map<Long, MemberDeliveryAddress> store = new LinkedHashMap<>();
        private final AtomicLong sequence = new AtomicLong();
        private int saveCount;

        @Override
        public Optional<MemberDeliveryAddress> findById(Long addressId) {
            return Optional.ofNullable(store.get(addressId));
        }

        public List<MemberDeliveryAddress> findByMemberId(MemberId memberId) {
            List<MemberDeliveryAddress> found = new ArrayList<>();
            for (MemberDeliveryAddress address : store.values()) {
                if (address.isOwnedBy(memberId)) {
                    found.add(address);
                }
            }
            return found;
        }

        @Override
        public long countByMemberId(MemberId memberId) {
            return findByMemberId(memberId).size();
        }

        @Override
        public Optional<MemberDeliveryAddress> findDefaultByMemberId(MemberId memberId) {
            return findByMemberId(memberId).stream().filter(MemberDeliveryAddress::isDefaultAddress).findFirst();
        }

        @Override
        public MemberDeliveryAddress save(MemberDeliveryAddress memberDeliveryAddress) {
            saveCount++;
            if (memberDeliveryAddress.getId() != null) {
                store.put(memberDeliveryAddress.getId(), memberDeliveryAddress);
                return memberDeliveryAddress;
            }

            long id = sequence.incrementAndGet();
            MemberDeliveryAddress persisted = MemberDeliveryAddress.reconstitute(
                id,
                memberDeliveryAddress.getMemberId(),
                memberDeliveryAddress.getAlias(),
                memberDeliveryAddress.getRoadAddress(),
                memberDeliveryAddress.getLotAddress(),
                memberDeliveryAddress.getDetailAddress(),
                memberDeliveryAddress.getAdminDongId(),
                memberDeliveryAddress.getLatitude(),
                memberDeliveryAddress.getLongitude(),
                memberDeliveryAddress.isDefaultAddress(),
                null,
                null
            );
            store.put(id, persisted);
            return persisted;
        }

        @Override
        public void deleteById(Long addressId) {
            store.remove(addressId);
        }
    }

    private static final class FakeAdminDongPersistence implements AdminDongLoadPort, AdminDongSavePort {

        @Override
        public AdminDongSyncResult synchronize(List<AdminDong> adminDongs) {
            throw new UnsupportedOperationException("동기화는 이 테스트의 대상이 아닙니다.");
        }

        private final Map<String, AdminDong> byName = new LinkedHashMap<>();
        private final Map<Long, AdminDong> byId = new LinkedHashMap<>();

        void registerInGangnam(String dongName) {
            AdminDong adminDong = AdminDong.reconstitute(
                GANGNAM_ADMIN_DONG_ID, String.valueOf(GANGNAM_ADMIN_DONG_ID), "서울특별시", "강남구", dongName, true, null, List.of()
            );
            byName.put(key("서울특별시", "강남구", dongName), adminDong);
            byId.put(GANGNAM_ADMIN_DONG_ID, adminDong);
        }

        @Override
        public Optional<AdminDong> findById(AdminDongId adminDongId) {
            return Optional.ofNullable(byId.get(adminDongId.value()));
        }

        @Override
        public boolean existsById(AdminDongId adminDongId) {
            return byId.containsKey(adminDongId.value());
        }

        @Override
        public Optional<AdminDong> findByDongNameMatch(String sidoName, String sigunguName, String dongName) {
            return Optional.ofNullable(byName.get(key(sidoName, sigunguName, dongName)));
        }

        @Override
        public List<AdminDong> findAllWithinBoundingBox(GeoBoundingBox boundingBox) {
            return byId.values().stream()
                .filter(AdminDong::hasCenter)
                .filter(adminDong -> boundingBox.contains(adminDong.getCenter()))
                .toList();
        }

        @Override
        public List<AdminDong> findAllByIds(Collection<AdminDongId> adminDongIds) {
            return adminDongIds.stream()
                .map(adminDongId -> byId.get(adminDongId.value()))
                .filter(Objects::nonNull)
                .toList();
        }

        @Override
        public Set<AdminDongId> filterExistingIds(Collection<AdminDongId> adminDongIds) {
            return adminDongIds.stream()
                .filter(adminDongId -> byId.containsKey(adminDongId.value()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        private static String key(String sidoName, String sigunguName, String dongName) {
            return sidoName + "|" + sigunguName + "|" + dongName;
        }
    }
}
