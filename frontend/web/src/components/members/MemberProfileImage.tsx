import Avatar from '@/components/ui/Avatar'

interface Props {
  profileImageUrl: string | null | undefined
  alt?: string
}

export default function MemberProfileImage({ profileImageUrl, alt = '프로필 이미지' }: Props) {
  return <Avatar src={profileImageUrl} alt={alt} size="bg" />
}
