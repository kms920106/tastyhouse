# shadcn Button 내 아이콘 크기 적용 가이드

## 문제

shadcn `Button` 내부 SVG에 `size` prop이 적용되지 않음.

**원인**: shadcn Button의 base 클래스에 포함된 아래 셀렉터가 CSS로 강제 16px 고정

```
[&_svg:not([class*='size-'])]:size-4
```

react-icons의 `size` prop은 HTML `width/height` 속성으로 렌더링되지만, CSS 우선순위에서 밀려 무시됨.

## 해결

`size` prop 대신 Tailwind `className="size-{n}"` 사용

```tsx
// ❌ 작동 안 함
<SomeIcon size={24} />

// ✅ 올바른 방법
<SomeIcon className="size-6" />
```

shadcn의 셀렉터가 `class`에 `size-`가 포함된 요소는 건너뛰므로 Tailwind 클래스가 정상 적용됨.

## Tailwind size 참고

| 클래스 | 크기 |
|--------|------|
| `size-4` | 16px |
| `size-5` | 20px |
| `size-6` | 24px |
| `size-7` | 28px |
| `size-8` | 32px |

> 이 규칙은 `AppButton`, `AppFullButton`, `AppPrimaryButton` 등 shadcn Button을 래핑한 모든 컴포넌트에 동일하게 적용됨.
