import MemberProfileImage from '@/components/members/MemberProfileImage'
import Icon from '@/components/ui/Icon'

interface Props {
  profileImageUrl: string | null
  onImageChange: (e: React.ChangeEvent<HTMLInputElement>) => void
}

export default function ProfileImageEditor({ profileImageUrl, onImageChange }: Props) {
  return (
    <div className="flex justify-center pt-8 pb-7">
      <div className="relative">
        <MemberProfileImage profileImageUrl={profileImageUrl} />
        <label
          htmlFor="profile-image"
          className="absolute bottom-0 right-0 w-[30px] h-[30px] bg-line rounded-full flex items-center justify-center cursor-pointer"
        >
          <Icon name="account/profile/camera" />
          <input
            id="profile-image"
            type="file"
            accept="image/*"
            className="hidden"
            onChange={onImageChange}
          />
        </label>
      </div>
    </div>
  )
}
