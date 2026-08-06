package com.tastyhouse.domain.holiday.model;

import java.time.LocalDate;

/**
 * 법정 공휴일 순수 도메인 모델.
 *
 * <p>Java 애플리케이션 계층에 생성 경로가 없는 <b>read-only 애그리거트</b>다({@code RecommendedKeyword}
 * 선례) — 캘린더는 {@code insert.sql} 시드로 관리하고, 자동 동기화(공공데이터포털 연동)는 후속 PR이다.
 * 그래서 {@code of}를 두지 않고 {@link #reconstitute}만 공개한다.
 *
 * <p><b>이 캘린더에는 일요일 자체를 담지 않는다.</b> 이 데이터 규칙 하나가 배달팁의 두 요구사항을
 * 코드 분기 없이 동시에 만족시킨다:
 * <ul>
 *   <li>"일요일은 공휴일 배달팁 대상이 아니다"(일요일은 시간별 배달팁으로 처리) — 평범한 일요일은
 *       캘린더에 없으므로 공휴일로 판정되지 않는다.</li>
 *   <li>"법정공휴일과 일요일이 겹치면 공휴일 배달팁을 부과한다" — 그 날짜는 법정공휴일이라서
 *       캘린더에 있으므로 공휴일로 판정된다.</li>
 * </ul>
 * 따라서 캘린더에 일요일 날짜가 보이는 것은 정상이며, "일요일이라서"가 아니라 "법정공휴일이라서"
 * 들어 있는 것이다. 이 의도를 모르고 일요일을 일괄 추가하면 위 첫 번째 규칙이 깨진다.
 */
public class PublicHoliday {

    private final Long id;
    private final LocalDate holidayDate;
    private final String name;
    private final boolean substitute;

    private PublicHoliday(Long id, LocalDate holidayDate, String name, boolean substitute) {
        this.id = id;
        this.holidayDate = holidayDate;
        this.name = name;
        this.substitute = substitute;
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     *
     * <p>생성 팩토리({@code of})가 없는 것은 의도된 것이다 — 위 클래스 Javadoc 참고.
     */
    public static PublicHoliday reconstitute(Long id, LocalDate holidayDate, String name, boolean substitute) {
        return new PublicHoliday(id, holidayDate, name, substitute);
    }

    public Long getId() {
        return this.id;
    }

    public LocalDate getHolidayDate() {
        return this.holidayDate;
    }

    public String getName() {
        return this.name;
    }

    /** 대체공휴일 여부. */
    public boolean isSubstitute() {
        return this.substitute;
    }
}
