import AppButton from '@/components/ui/AppButton'
import Icon from '@/components/ui/Icon'
import Link from 'next/link'

export default function CustomerCenterGuideSection() {
  return (
    <div className="px-[15px] py-[31px]">
      <p className="text-sm leading-[14px]">고객센터 이용 안내</p>
      <div className="mt-5 flex flex-col gap-[7px]">
        <p className="text-sm leading-[14px] text-[#666666]">
          • 이전 고객님의 상담이 길어질 경우 통화가 지연될 수 있습니다.
        </p>
        <p className="text-sm leading-relaxed text-[#666666]">
          • 전화가 지연될 경우 카카오톡 문의를 통해 연락처와 통화가 가능한 시간대를 남겨주시면 확인
          후 바로 연락을 드립니다.
        </p>
        <p className="text-sm leading-[14px] text-[#666666]">
          • 카카오톡 문의를 이용해주시면 빠른 답변이 가능합니다.
        </p>
      </div>
      <div className="mt-[30px]">
        <AppButton className="gap-1 w-full h-[50px] bg-[#fddc3f] text-base leading-[16px] text-black">
          <Icon name="kakao" />
          <Link href="http://pf.kakao.com/_dXxcxbX/chat">카카오톡 문의</Link>
        </AppButton>
      </div>
    </div>
  )
}
