interface FBAuthResponse {
  accessToken: string
  expiresIn: number
  reauthorize_required_in: number
  signedRequest: string
  userID: string
}

interface FBLoginResponse {
  status: 'connected' | 'not_authorized' | 'unknown'
  authResponse: FBAuthResponse | null
}

interface FBLoginOptions {
  scope?: string
  auth_type?: 'rerequest' | 'reauthenticate'
}

interface FacebookSDK {
  init(params: {
    appId: string
    cookie?: boolean
    xfbml?: boolean
    version: string
  }): void
  login(callback: (response: FBLoginResponse) => void, options?: FBLoginOptions): void
  logout(callback: (response: FBLoginResponse) => void): void
  getLoginStatus(callback: (response: FBLoginResponse) => void): void
  api(
    path: string,
    params: Record<string, unknown>,
    callback: (response: Record<string, unknown>) => void,
  ): void
}

declare global {
  interface Window {
    FB: FacebookSDK
    fbAsyncInit: () => void
  }
}

export {}
