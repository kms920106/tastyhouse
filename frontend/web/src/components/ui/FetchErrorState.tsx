import Icon from '@/components/ui/Icon'

interface Props {
  message: string
}

export default function FetchErrorState({ message }: Props) {
  return (
    <div className="flex-1 flex flex-col items-center justify-center">
      <div className="relative w-[35px] h-[35px]">
        <Icon name="notice" />
      </div>
      <div className="mt-[15px]">
        <p className="text-sm leading-relaxed text-[#aaaaaa] whitespace-pre-line text-center">
          {message}
        </p>
      </div>
    </div>
  )
}
