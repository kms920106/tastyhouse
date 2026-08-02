import GuestLoginBanner from '@/components/ui/GuestLoginBanner'

export default function GradeGuestSection() {
  return (
    <div className="px-[15px] py-[18px]">
      <GuestLoginBanner
        title="내 등급을 확인하려면?"
        description="로그인 후 등급을 확인해 보세요"
      />
    </div>
  )
}
