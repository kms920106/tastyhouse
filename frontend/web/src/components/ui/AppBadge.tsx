import { Badge } from './shadcn/badge'

interface AppBadgeProps extends React.ComponentProps<'span'> {
  variant?: 'default' | 'secondary' | 'destructive' | 'outline' | 'ghost' | 'link'
}

export default function AppBadge({ variant = 'default', ...props }: AppBadgeProps) {
  return <Badge variant={variant} {...props} />
}
