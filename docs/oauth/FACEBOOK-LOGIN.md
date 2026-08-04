# Facebook Login for Web (JavaScript SDK) 핵심 정리

> **출처**: [Facebook Developers 공식문서](https://developers.facebook.com/documentation/facebook-login/web)
> **최종 업데이트 기준**: 2026년 3월  
> **최신 Graph API 버전**: v25.0

---

## 1. 사전 준비

- Facebook Developer 계정
- Facebook App ID (웹사이트용으로 등록)
- **HTTPS 필수** — JS SDK를 통한 인증은 HTTPS 페이지에서만 동작

---

## 2. SDK 초기화

### 2-1. SDK 비동기 로드

```js
(function(d, s, id) {
  var js, fjs = d.getElementsByTagName(s)[0];
  if (d.getElementById(id)) return;
  js = d.createElement(s); js.id = id;
  js.src = "https://connect.facebook.net/en_US/sdk.js";
  fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));
```

### 2-2. SDK 초기화 (`FB.init`)

```js
window.fbAsyncInit = function() {
  FB.init({
    appId      : '{app-id}',
    cookie     : true,   // 서버에서 세션 접근 가능하게 쿠키 활성화
    xfbml      : true,   // 소셜 플러그인 파싱
    version    : 'v25.0' // Graph API 버전
  });
};
```

### 2-3. App Dashboard 설정

| 설정 항목 | 값 |
|---|---|
| **Login with JavaScript SDK** | Yes |
| **Allowed Domains for JavaScript SDK** | SDK를 호스팅하는 도메인 |
| **Valid OAuth Redirect URIs** | 인증 성공 후 리다이렉트 URL |

---

## 3. 로그인 상태 확인 (`FB.getLoginStatus`)

```js
FB.getLoginStatus(function(response) {
  // response.status로 판단
});
```

### 응답 구조

```json
{
  "status": "connected",
  "authResponse": {
    "accessToken": "{access-token}",
    "expiresIn": "{unix-timestamp}",
    "reauthorize_required_in": "{seconds}",
    "signedRequest": "{signed-parameter}",
    "userID": "{user-id}"
  }
}
```

### status 값

| 값 | 의미 |
|---|---|
| `connected` | Facebook 로그인 O + 내 앱 로그인 O |
| `not_authorized` | Facebook 로그인 O + 내 앱 로그인 X |
| `unknown` | Facebook 로그인 X (또는 `FB.logout()` 호출 후) |

---

## 4. 로그인 처리

### 4-1. 방법 A: Login Button (XFBML)

```html
<fb:login-button
  scope="public_profile,email"
  onlogin="checkLoginState();">
</fb:login-button>
```

#### Login Button 주요 속성

| HTML5 속성 | 설명 | 기본값 |
|---|---|---|
| `data-scope` | 요청할 권한 목록 (쉼표 구분) | `public_profile` |
| `data-size` | 버튼 크기 | `small` / `medium` / `large` |
| `data-onlogin` | 로그인 완료 시 실행할 JS 함수 | - |
| `data-auto-logout-link` | 로그인 시 로그아웃 버튼으로 전환 | `false` |
| `data-use-continue-as` | "Continue as {이름}" 버튼 활성화 | `false` |
| `data-button-type` | `continue_with` 또는 `log_in_with` | - |

#### Continue as {Name} 버튼 크기

| 크기 | 높이 | 폭 |
|---|---|---|
| Small | 20px | 200px (고정) |
| Medium | 28px | 200–320px |
| Large | 40px | 240–400px |

#### 동적 렌더링 시 주의

SDK `init` 이후 동적으로 `<div class="fb-login-button">` 추가 시, `FB.XFBML.parse()`를 수동 호출해야 버튼이 렌더링됨.

### 4-2. 방법 B: `FB.login()` (커스텀 버튼)

```js
FB.login(function(response) {
  if (response.status === 'connected') {
    // 로그인 성공
  } else {
    // 로그인 실패 또는 취소
  }
}, { scope: 'public_profile,email' });
```

> **주의**: `FB.login()`은 반드시 사용자 클릭 이벤트 핸들러 내에서 호출해야 한다. 그렇지 않으면 브라우저 팝업 차단에 걸림.

---

## 5. 로그아웃 (`FB.logout`)

```js
FB.logout(function(response) {
  // 로그아웃 완료
});
```

### 주의사항

- 사용자가 앱 로그인 플로우를 통해 Facebook에도 동시에 로그인한 경우, 앱 로그아웃 시 Facebook에서도 로그아웃될 수 있음
- 로그아웃해도 권한(Permissions)은 해제되지 않음 — 별도로 revoke 필요

---

## 6. Access Token

### 6-1. 핵심 특성

| 항목 | 내용 |
|---|---|
| **저장** | JS SDK가 브라우저에서 자동 관리 (별도 저장 불필요) |
| **유효 시간** | 브라우저 토큰은 약 **2시간**, SDK가 자동 갱신 |
| **형식** | 가변 길이의 불투명(opaque) 문자열 |
| **서버 전달** | `response.authResponse.accessToken`으로 추출하여 서버에 전달 가능 |

### 6-2. 토큰 가져오기

```js
FB.getLoginStatus(function(response) {
  if (response.status === 'connected') {
    console.log(response.authResponse.accessToken);
  }
});
```

### 6-3. Long-Lived Token

- 브라우저 토큰(단기)을 서버로 보낸 후, 서버에서 **Long-Lived Token**으로 교환
- 공식 가이드: [Get Long-Lived Tokens](https://developers.facebook.com/documentation/facebook-login/guides/access-tokens/get-long-lived)

### 6-4. 서버에서 토큰 검증

서버로 토큰을 전달할 경우 반드시 재검증 필요:
- Access Token Debug 엔드포인트 호출
- `app_id`와 `user_id`가 예상 값과 일치하는지 확인
- 참고: [Manually Build a Login Flow — Check Token](https://developers.facebook.com/documentation/facebook-login/guides/advanced/manual-flow#checktoken)

### 6-5. API 호출

```js
FB.api('/me', { fields: 'name,email' }, function(response) {
  console.log(response.name);
  console.log(response.email);
});
```

### 6-6. Deauthorize Callback

사용자가 Facebook.com에서 앱을 삭제할 때 알림을 받으려면, App Dashboard에서 **Deauthorize Callback URL**을 설정.

---

## 7. Permissions (권한 관리)

### 7-1. 기본 원칙

- `public_profile`과 `email`은 **앱 리뷰 없이** 자동 부여
- 그 외 권한은 **Facebook 앱 리뷰** 필요
- `scope` 파라미터로 Login Dialog 생성 시 요청
- 사용자는 특정 권한을 **거절(decline)**할 수 있음 → 반드시 핸들링 필요

### 7-2. 권한 확인

```
GET /me/permissions
```

응답 예시:
```json
{
  "data": [
    { "permission": "public_profile", "status": "granted" },
    { "permission": "email", "status": "granted" }
  ]
}
```

### 7-3. 추가 권한 요청

이미 로그인한 사용자에게 새로운 권한을 나중에 요청할 수 있음:

```js
FB.login(function(response) {
  console.log(response);
}, { scope: 'email' }); // 새 권한만 명시
```

> **Best Practice**: 읽기 권한과 쓰기 권한을 동시에 요청하지 않기

### 7-4. 거절된 권한 재요청

거절된 권한은 일반 `FB.login()`으로 다시 요청해도 다이얼로그에 표시되지 않음.  
**`auth_type: 'rerequest'`** 플래그를 추가해야 함:

```js
FB.login(function(response) {
  console.log(response);
}, {
  scope: 'user_likes',
  auth_type: 'rerequest'
});
```

> **주의**: 재요청 전 사용자에게 해당 권한이 필요한 이유를 설명하는 UI를 보여주는 것을 권장. Login Button은 rerequest를 지원하지 않으므로, 커스텀 버튼 + `FB.login()` 조합 사용.

---

## 8. HTTPS 설정

### 8-1. 왜 필수인가

- Facebook JS SDK는 **HTTPS 페이지에서만** 인증 동작
- Google Chrome은 HTTP 사이트를 "Not secure"로 표시
- HTTPS는 Access Token이 네트워크에서 노출되는 것을 방지

### 8-2. 인증서 발급 (추천)

**Let's Encrypt** — 무료, 자동 갱신, Facebook 공식 추천

### 8-3. 클라우드 호스팅별 HTTPS 지원

| 플랫폼 | 방법 |
|---|---|
| AWS | AWS Certificate Manager (무료, ELB/CloudFront 연동) |
| Azure | 포털 내장 + Let's Encrypt 확장 |
| Google App Engine | 관리형 무료 인증서 자동 적용 |
| Heroku | Automated Certificate Management (Hobby+ dyno) |
| Cloudflare | One-click SSL (CDN 경유) |

### 8-4. Mixed Content 해결

HTTPS 페이지에서 HTTP 리소스 로드 시 경고/차단 발생. 해결 방법:

```html
<!-- HTTP 헤더 -->
Content-Security-Policy: upgrade-insecure-requests

<!-- 또는 HTML meta 태그 -->
<meta http-equiv="Content-Security-Policy" content="upgrade-insecure-requests">
```

---

## 9. 전체 구현 예제 (Full Code)

```html
<!DOCTYPE html>
<html>
<head>
  <title>Facebook Login JavaScript Example</title>
  <meta charset="UTF-8">
</head>
<body>

<script>
  function statusChangeCallback(response) {
    if (response.status === 'connected') {
      testAPI();
    } else {
      document.getElementById('status').innerHTML =
        'Please log into this app.';
    }
  }

  function checkLoginState() {
    FB.getLoginStatus(function(response) {
      statusChangeCallback(response);
    });
  }

  window.fbAsyncInit = function() {
    FB.init({
      appId      : '{app-id}',
      cookie     : true,
      xfbml      : true,
      version    : 'v25.0'
    });
    FB.getLoginStatus(function(response) {
      statusChangeCallback(response);
    });
  };

  // SDK 비동기 로드
  (function(d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id)) return;
    js = d.createElement(s); js.id = id;
    js.src = "https://connect.facebook.net/en_US/sdk.js";
    fjs.parentNode.insertBefore(js, fjs);
  }(document, 'script', 'facebook-jssdk'));

  function testAPI() {
    FB.api('/me', { fields: 'name,email' }, function(response) {
      document.getElementById('status').innerHTML =
        'Thanks for logging in, ' + response.name + '!';
    });
  }
</script>

<fb:login-button
  scope="public_profile,email"
  onlogin="checkLoginState();">
</fb:login-button>

<div id="status"></div>

</body>
</html>
```

---

## 10. 주요 API 레퍼런스 Quick Link

| API | 설명 |
|---|---|
| `FB.init(params)` | SDK 초기화 |
| `FB.login(callback, opts)` | 로그인 다이얼로그 호출 |
| `FB.logout(callback)` | 로그아웃 |
| `FB.getLoginStatus(callback)` | 현재 로그인 상태 확인 |
| `FB.api(path, params, callback)` | Graph API 호출 |
| `FB.XFBML.parse(dom?)` | XFBML 플러그인 재파싱 (동적 렌더링 시) |

---

## 11. 체크리스트 (구현 시 확인 사항)

- [ ] App Dashboard에서 Facebook Login 제품 추가 완료
- [ ] "Login with JavaScript SDK" 토글 ON
- [ ] Allowed Domains에 SDK 호스팅 도메인 등록
- [ ] Valid OAuth Redirect URIs 설정
- [ ] 사이트 HTTPS 적용 완료
- [ ] `public_profile`, `email` 외 권한 사용 시 앱 리뷰 신청
- [ ] 사용자가 권한 거절 시 대응 로직 구현
- [ ] 서버로 토큰 전달 시 재검증 로직 구현
- [ ] 로그아웃 기능 제공 (정책상 필수)
- [ ] Deauthorize Callback URL 설정
