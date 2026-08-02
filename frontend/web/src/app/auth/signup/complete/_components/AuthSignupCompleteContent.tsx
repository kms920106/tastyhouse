import Icon from '@/components/ui/Icon'

export default function AuthSignupCompleteContent() {
  return (
    <div className="flex-1 flex flex-col items-center justify-center">
      <Icon name="logo-150" />
      <div className="flex flex-col items-center text-center mt-[30px]">
        <h2 className="text-[23px] leading-[23px]">회원가입이 완료되었습니다.</h2>
        <p className="mt-[15px] text-sm leading-relaxed text-[#666666]">
          테이스티하우스의 회원이 되신걸 환영합니다.
          <br />
          다양한 서비스들을 만나보세요
        </p>
      </div>
    </div>
  )
}
