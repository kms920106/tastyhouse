import { type Props as AppButtonProps } from '@/components/ui/AppButton'
import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import { Spinner } from '@/components/ui/shadcn/spinner'

interface Props extends AppButtonProps {
  isSubmitting: boolean
  loadingText?: string
}

export default function AppSubmitButton({
  isSubmitting,
  loadingText,
  disabled,
  children,
  ...props
}: Props) {
  return (
    <AppPrimaryButton disabled={isSubmitting || disabled} {...props}>
      {isSubmitting ? (
        <>
          {loadingText}
          <Spinner />
        </>
      ) : (
        children
      )}
    </AppPrimaryButton>
  )
}
