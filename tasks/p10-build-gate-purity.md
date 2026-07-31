# P10. domain-module 순수성 빌드 강제 + 빌드·문서 정합

## 배경

domain-module 소스는 실제로 프레임워크-프리(spring/jakarta/querydsl import 0건)지만, **이것이 빌드로 강제되지 않는다.** 루트 build.gradle의 `subprojects` 블록이 모든 모듈에 spring-boot-starter를 주입하므로 domain-module에서 `@Service`를 import해도 컴파일이 통과한다. 순수성은 현재 리뷰 규율로만 유지된다. 문서에도 실제 빌드와 상충하는 서술이 있다.

## 문제 상세

1. **루트 `build.gradle:31-46`** — `subprojects { }`가 모든 서브프로젝트에 `org.springframework.boot` 플러그인 + `implementation 'org.springframework.boot:spring-boot-starter'` + `testImplementation spring-boot-starter-test`를 주입. domain-module의 컴파일 클래스패스에 spring-core/context/beans가 올라와 있어 "production 의존은 Lombok 하나뿐"이 컴파일 관점에선 거짓.
2. **Lombok이 `implementation`으로 선언** — `domain-module/build.gradle:16`(다른 모듈 동일): `compileOnly` + `annotationProcessor`가 표준인데 `implementation`이라 lombok jar가 런타임 산출물에 포함됨.
3. **문서 상충** — 루트 `CLAUDE.md`의 "모듈 경계 규칙" 절이 "infrastructure-module은 API 모듈에 **runtimeOnly로 의존**되어 컴파일 타임 은닉이 강제된다"고 서술하지만, 실제 4개 api 모듈 build.gradle은 모두 `implementation project(':infrastructure-module')`이고, 같은 문서의 다른 절은 "api 모듈이 infra를 컴파일 타임에 보는 이유는 query DAO 직접 주입"이라고 정반대로 서술 — security-module 분리 근거 서술이 낡음.
4. **미사용 의존 선언** — admin-api/ceo-api의 `implementation project(':external-api')`는 소스 import 0건.

## 작업 지시

1. 루트 `build.gradle`의 `subprojects` 블록에서 domain-module을 제외하거나(`subprojects.findAll { it.name != 'domain-module' }` 또는 domain-module에서 configuration exclude), spring 주입을 각 모듈 build.gradle로 내리는 방식 중 **기존 빌드 구조에 가장 침습이 적은 방식**을 선택한다. 목표: domain-module 컴파일 클래스패스에 `org.springframework.*`가 없어서 `import org.springframework.stereotype.Service;` 한 줄이 **컴파일 에러**가 되는 상태.
   - domain-module의 `src/test`는 JUnit/AssertJ가 필요하므로 testImplementation은 유지(단 spring-boot-starter-test 통째가 아니라 junit-jupiter+assertj로 축소 가능한지 확인).
   - `org.springframework.boot` 플러그인 자체가 domain-module에 필요한지(bootJar 비활성 여부 포함) 확인하고 불필요하면 `java-library`로.
2. 검증: domain-module 임시 파일에 spring import를 넣어 컴파일이 실제로 깨지는지 확인 후 제거(red 확인). gradle 금지 환경이면 verify-without-gradle의 javac 클래스패스 구성으로 동일 검증.
3. Lombok을 `compileOnly` + `annotationProcessor`로 교정한다 — 전 모듈 일괄이 위험하면 domain-module만 우선하고 나머지는 후속 제안으로 보고.
4. CLAUDE.md의 "runtimeOnly 은닉" 서술을 현재 실태(`implementation` + ArchUnit persistence 금지로 은닉)에 맞게 교정한다. 상충하는 두 절 중 낡은 쪽(모듈 경계 규칙 절)을 고친다.
5. admin-api/ceo-api의 미사용 `external-api` 의존 선언을 제거한다(제거 후 컴파일 확인 — 리플렉션/스캔 경유 사용이 없는지 `com.tastyhouse.external` 문자열 grep 포함).

## 수용 기준

- [ ] domain-module에서 spring import가 컴파일 에러가 됨을 red 테스트로 증명
- [ ] 기존 전 모듈 컴파일·테스트가 여전히 통과 (verify-without-gradle)
- [ ] CLAUDE.md의 runtimeOnly 서술이 실태와 일치하게 교정됨
- [ ] 미사용 의존 제거 후 컴파일 정상
- [ ] domain-module 산출물에 lombok 런타임 포함 안 됨 (compileOnly 전환 시)

## 주의사항

- **빌드 파일 수정은 팬아웃이 크다** — 한 번에 하나씩 바꾸고 매번 전 모듈 컴파일 확인. gradle build 금지이므로 verify-without-gradle 절차의 클래스패스 목록도 함께 갱신해야 할 수 있다(그 메모리 문서가 spring jar를 domain 클래스패스에 넣고 있다면 제거).
- spring-boot-starter 제거 시 domain-module `src/test`가 `@SpringBootTest`류를 쓰는 파일이 없는지 먼저 grep(순수 단위 테스트만 있어야 정상).
- P2(ArchUnit domain 순수성 규칙)와 상보적이다 — P10은 컴파일 게이트, P2는 아키텍처 게이트. 둘 다 있어야 완전.
