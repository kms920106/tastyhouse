package com.tastyhouse.domain.member.domain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.repository.MemberDeliveryAddressRepository;
import com.tastyhouse.domain.member.service.MemberDeliveryAddressService;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.vo.AdminDongId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 회원 배달 주소록 도메인 서비스 단위 테스트.
 *
 * <p>Spring 컨텍스트 없이 수기 fake 리포지토리만으로 검증한다(domain-module은 Mockito 의존이 없다).
 * 검증 대상은 전부 <b>주소록 전체를 봐야 판정되는</b> 규칙이다 — 기본 배송지 유일성, 회원당 10건 한도,
 * 소유권. 행정동 매칭 실패가 예외가 아니라 null이라는 점도 함께 고정한다.
 */
class MemberDeliveryAddressServiceTest {

    private static final MemberId MEMBER_ID = MemberId.of(1L);
    private static final MemberId OTHER_MEMBER_ID = MemberId.of(2L);
    private static final BigDecimal LATITUDE = new BigDecimal("37.501234");
    private static final BigDecimal LONGITUDE = new BigDecimal("127.039876");
    private static final String ROAD_ADDRESS = "서울특별시 강남구 테헤란로 123";

    @Nested
    @DisplayName("등록(create)")
    class Create {

        @Test
        @DisplayName("10건이 이미 있으면 한도 초과로 거부한다")
        void create_rejectsWhenLimitExceeded() {
            FakeMemberDeliveryAddressRepository addressRepository = new FakeMemberDeliveryAddressRepository();
            for (int i = 0; i < 10; i++) {
                addressRepository.save(newAddress(MEMBER_ID, false));
            }
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressRepository, new FakeAdminDongRepository()
            );

            assertThatThrownBy(() -> create(service, false))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.MEMBER_DELIVERY_ADDRESS_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("9건까지는 등록되어 10건째가 마지막이다")
        void create_allowsUpToLimit() {
            FakeMemberDeliveryAddressRepository addressRepository = new FakeMemberDeliveryAddressRepository();
            for (int i = 0; i < 9; i++) {
                addressRepository.save(newAddress(MEMBER_ID, false));
            }
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressRepository, new FakeAdminDongRepository()
            );

            Long createdId = create(service, false);

            assertThat(createdId).isNotNull();
            assertThat(addressRepository.countByMemberId(MEMBER_ID)).isEqualTo(10L);
        }

        @Test
        @DisplayName("다른 회원의 주소는 한도 계산에 포함하지 않는다")
        void create_countsOnlyOwnAddresses() {
            FakeMemberDeliveryAddressRepository addressRepository = new FakeMemberDeliveryAddressRepository();
            for (int i = 0; i < 10; i++) {
                addressRepository.save(newAddress(OTHER_MEMBER_ID, false));
            }
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressRepository, new FakeAdminDongRepository()
            );

            assertThat(create(service, false)).isNotNull();
        }

        @Test
        @DisplayName("기본 배송지로 등록하면 기존 기본 배송지가 해제되어 항상 1건만 남는다")
        void create_unmarksPreviousDefault() {
            FakeMemberDeliveryAddressRepository addressRepository = new FakeMemberDeliveryAddressRepository();
            MemberDeliveryAddress previousDefault = addressRepository.save(newAddress(MEMBER_ID, true));
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressRepository, new FakeAdminDongRepository()
            );

            create(service, true);

            assertThat(addressRepository.findById(previousDefault.getId()).orElseThrow().isDefaultAddress()).isFalse();
            assertThat(defaultAddressCount(addressRepository)).isEqualTo(1);
        }

        @Test
        @DisplayName("행정동 매칭에 성공하면 adminDongId를 채운다")
        void create_fillsAdminDongIdOnMatch() {
            FakeMemberDeliveryAddressRepository addressRepository = new FakeMemberDeliveryAddressRepository();
            FakeAdminDongRepository adminDongRepository = new FakeAdminDongRepository();
            adminDongRepository.register(1168064000L, "서울특별시", "강남구", "테헤란로");
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressRepository, adminDongRepository
            );

            Long createdId = create(service, false);

            assertThat(addressRepository.findById(createdId).orElseThrow().getAdminDongId())
                .isEqualTo(AdminDongId.of(1168064000L));
        }

        @Test
        @DisplayName("도로명 주소로 매칭에 실패하면 지번 주소로 재시도한다")
        void create_fallsBackToLotAddress() {
            FakeMemberDeliveryAddressRepository addressRepository = new FakeMemberDeliveryAddressRepository();
            FakeAdminDongRepository adminDongRepository = new FakeAdminDongRepository();
            adminDongRepository.register(1168064000L, "서울특별시", "강남구", "역삼1동");
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressRepository, adminDongRepository
            );

            Long createdId = service.create(
                MEMBER_ID, "집", ROAD_ADDRESS, "서울특별시 강남구 역삼1동 678-9", "101동", LATITUDE, LONGITUDE, false
            );

            assertThat(addressRepository.findById(createdId).orElseThrow().getAdminDongId())
                .isEqualTo(AdminDongId.of(1168064000L));
        }

        @Test
        @DisplayName("행정동 매칭에 실패해도 예외 없이 adminDongId를 null로 두고 등록한다")
        void create_allowsNullAdminDongIdOnMatchFailure() {
            FakeMemberDeliveryAddressRepository addressRepository = new FakeMemberDeliveryAddressRepository();
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressRepository, new FakeAdminDongRepository()
            );

            Long createdId = create(service, false);

            assertThat(addressRepository.findById(createdId).orElseThrow().getAdminDongId()).isNull();
        }
    }

    @Nested
    @DisplayName("수정(update)")
    class Update {

        @Test
        @DisplayName("타인의 주소를 수정하면 접근 거부한다")
        void update_rejectsOtherMembersAddress() {
            FakeMemberDeliveryAddressRepository addressRepository = new FakeMemberDeliveryAddressRepository();
            MemberDeliveryAddress othersAddress = addressRepository.save(newAddress(OTHER_MEMBER_ID, false));
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressRepository, new FakeAdminDongRepository()
            );

            assertThatThrownBy(() -> service.update(
                MEMBER_ID, othersAddress.getId(), "회사", ROAD_ADDRESS, null, null, LATITUDE, LONGITUDE
            ))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.MEMBER_DELIVERY_ADDRESS_ACCESS_DENIED);
        }

        @Test
        @DisplayName("존재하지 않는 주소를 수정하면 404다")
        void update_rejectsMissingAddress() {
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                new FakeMemberDeliveryAddressRepository(), new FakeAdminDongRepository()
            );

            assertThatThrownBy(() -> service.update(
                MEMBER_ID, 999L, "회사", ROAD_ADDRESS, null, null, LATITUDE, LONGITUDE
            )).isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("본인 주소는 수정되고 명시적으로 저장된다")
        void update_savesOwnAddress() {
            FakeMemberDeliveryAddressRepository addressRepository = new FakeMemberDeliveryAddressRepository();
            MemberDeliveryAddress address = addressRepository.save(newAddress(MEMBER_ID, false));
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressRepository, new FakeAdminDongRepository()
            );
            addressRepository.saveCount = 0;

            service.update(
                MEMBER_ID, address.getId(), "회사", "서울특별시 강남구 테헤란로 500", null, "10층", LATITUDE, LONGITUDE
            );

            assertThat(addressRepository.saveCount).isEqualTo(1);
            MemberDeliveryAddress updated = addressRepository.findById(address.getId()).orElseThrow();
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
            FakeMemberDeliveryAddressRepository addressRepository = new FakeMemberDeliveryAddressRepository();
            MemberDeliveryAddress othersAddress = addressRepository.save(newAddress(OTHER_MEMBER_ID, false));
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressRepository, new FakeAdminDongRepository()
            );

            assertThatThrownBy(() -> service.delete(MEMBER_ID, othersAddress.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.MEMBER_DELIVERY_ADDRESS_ACCESS_DENIED);
            assertThat(addressRepository.findById(othersAddress.getId())).isPresent();
        }

        @Test
        @DisplayName("본인 주소는 삭제된다")
        void delete_removesOwnAddress() {
            FakeMemberDeliveryAddressRepository addressRepository = new FakeMemberDeliveryAddressRepository();
            MemberDeliveryAddress address = addressRepository.save(newAddress(MEMBER_ID, false));
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressRepository, new FakeAdminDongRepository()
            );

            service.delete(MEMBER_ID, address.getId());

            assertThat(addressRepository.findById(address.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("기본 배송지 변경(changeDefault)")
    class ChangeDefault {

        @Test
        @DisplayName("새 기본을 지정하면 기존 기본이 해제되어 회원당 1건만 남는다")
        void changeDefault_keepsSingleDefault() {
            FakeMemberDeliveryAddressRepository addressRepository = new FakeMemberDeliveryAddressRepository();
            MemberDeliveryAddress previousDefault = addressRepository.save(newAddress(MEMBER_ID, true));
            MemberDeliveryAddress target = addressRepository.save(newAddress(MEMBER_ID, false));
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressRepository, new FakeAdminDongRepository()
            );

            service.changeDefault(MEMBER_ID, target.getId());

            assertThat(addressRepository.findById(previousDefault.getId()).orElseThrow().isDefaultAddress()).isFalse();
            assertThat(addressRepository.findById(target.getId()).orElseThrow().isDefaultAddress()).isTrue();
            assertThat(defaultAddressCount(addressRepository)).isEqualTo(1);
        }

        @Test
        @DisplayName("기존 기본이 없어도 새 기본을 지정할 수 있다")
        void changeDefault_worksWithoutExistingDefault() {
            FakeMemberDeliveryAddressRepository addressRepository = new FakeMemberDeliveryAddressRepository();
            MemberDeliveryAddress target = addressRepository.save(newAddress(MEMBER_ID, false));
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressRepository, new FakeAdminDongRepository()
            );

            service.changeDefault(MEMBER_ID, target.getId());

            assertThat(addressRepository.findById(target.getId()).orElseThrow().isDefaultAddress()).isTrue();
        }

        @Test
        @DisplayName("타인의 주소를 기본으로 지정하면 접근 거부한다")
        void changeDefault_rejectsOtherMembersAddress() {
            FakeMemberDeliveryAddressRepository addressRepository = new FakeMemberDeliveryAddressRepository();
            MemberDeliveryAddress othersAddress = addressRepository.save(newAddress(OTHER_MEMBER_ID, false));
            MemberDeliveryAddressService service = new MemberDeliveryAddressService(
                addressRepository, new FakeAdminDongRepository()
            );

            assertThatThrownBy(() -> service.changeDefault(MEMBER_ID, othersAddress.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.MEMBER_DELIVERY_ADDRESS_ACCESS_DENIED);
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

    private static long defaultAddressCount(FakeMemberDeliveryAddressRepository repository) {
        return repository.findByMemberId(MEMBER_ID).stream().filter(MemberDeliveryAddress::isDefaultAddress).count();
    }

    /**
     * 인메모리 배달 주소 리포지토리. {@code save}는 실제 어댑터와 같은 시맨틱을 흉내 낸다 — 식별자가
     * 없으면 새 id를 발급해 저장하고, 있으면 같은 id의 행을 교체한다.
     */
    private static final class FakeMemberDeliveryAddressRepository implements MemberDeliveryAddressRepository {

        private final Map<Long, MemberDeliveryAddress> store = new LinkedHashMap<>();
        private final AtomicLong sequence = new AtomicLong();
        private int saveCount;

        @Override
        public Optional<MemberDeliveryAddress> findById(Long addressId) {
            return Optional.ofNullable(store.get(addressId));
        }

        @Override
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

    /** 인메모리 행정동 마스터. 등록되지 않은 조합을 조회하면 빈 Optional을 돌려 매칭 실패를 재현한다. */
    private static final class FakeAdminDongRepository implements AdminDongRepository {

        private final Map<String, AdminDong> byName = new LinkedHashMap<>();
        private final Map<Long, AdminDong> byId = new LinkedHashMap<>();

        void register(Long id, String sidoName, String sigunguName, String dongName) {
            AdminDong adminDong = AdminDong.reconstitute(id, String.valueOf(id), sidoName, sigunguName, dongName, true);
            byName.put(key(sidoName, sigunguName, dongName), adminDong);
            byId.put(id, adminDong);
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

        private static String key(String sidoName, String sigunguName, String dongName) {
            return sidoName + "|" + sigunguName + "|" + dongName;
        }
    }
}
