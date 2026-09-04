package com.tastyhouse.application.shared.marker;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이 클래스가 뜨는 실행 앱 = <b>ceo-api</b>.
 *
 * <p>{@code useDefaultFilters=false} 스캔({@link com.tastyhouse.application.CeoApplicationConfig})의
 * <b>유일한 포함 기준</b>이자, ArchUnit 앱 격리 규칙의 술어다. 새 빈({@code @Service}·{@code @Component})과
 * 새 UseCase 인터페이스는 <b>반드시 마커 하나를 단다</b> — 마커가 없으면 어느 앱에도 뜨지 않고,
 * 컴파일은 통과하므로 실패는 기동 시점에야 드러난다.
 *
 * <p>Command record에는 붙이지 않는다. 소속은 그 record를 시그니처에 쓰는 UseCase의 마커에서 유도한다
 * (ArchUnit {@code commandRecordsShouldBelongToExactlyOneApp}).
 *
 * <p>{@code @Component} 메타를 얹지 않은 <b>순수 마커</b>다. 얹으면 {@code @Component} 22개의 의미가
 * 흐려지므로 {@code @Service}/{@code @Component}는 그대로 남긴다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CeoApp {
}
