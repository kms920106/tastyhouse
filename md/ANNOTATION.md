// Controller
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController { ... }

// Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService { ... }

// Entity
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "password")
public class User { ... }

핵심 원칙 요약

Spring 표준 → Lombok → 외부 라이브러리 순으로
코드 리뷰 시 Spring 핵심 애노테이션이 먼저 눈에 띄어야 함
팀 컨벤션으로 고정하는 게 가장 중요 (일관성 > 완벽한 순서)
