import Image from 'next/image'

interface Props {
  message: string
}

export default function EmptyState({ message }: Props) {
  return (
    <div className="flex-1 flex flex-col items-center justify-center">
      <div className="relative w-[35px] h-[40px]">
        <Image src="/images/mypage/logo-gray.png" alt="로고" width={35} height={40} />
      </div>
      <div className="mt-[15px]">
        <p className="text-sm leading-[14px] text-[#aaaaaa]">{message}</p>
      </div>
    </div>
  )
}
