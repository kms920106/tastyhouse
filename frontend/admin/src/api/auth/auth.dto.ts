export interface LoginRequest {
  username: string;
  password: string;
  rememberMe: boolean;
}

export interface RefreshRequest {
  refreshToken: string;
}

export interface AuthToken {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}
