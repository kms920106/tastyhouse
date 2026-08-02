import LoginCredentialForm from './LoginCredentialForm'
import LoginHeader from './LoginHeader'
import LoginNavLinks from './LoginNavLinks'
import LoginSocialButtons from './LoginSocialButtons'

export default function LoginPage() {
  return (
    <>
      <LoginHeader />
      <div className="px-[15px] py-[30px]">
        <LoginCredentialForm />
        <LoginNavLinks />
        <LoginSocialButtons />
      </div>
    </>
  )
}
