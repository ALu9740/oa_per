const TOKEN_KEY = 'oa_access_token'
const REFRESH_TOKEN_KEY = 'oa_refresh_token'
const USER_KEY = 'oa_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function getUser() {
  const raw = localStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

export function saveLoginVO(loginVO) {
  localStorage.setItem(TOKEN_KEY, loginVO.accessToken || '')
  localStorage.setItem(REFRESH_TOKEN_KEY, loginVO.refreshToken || '')
  localStorage.setItem(USER_KEY, JSON.stringify(loginVO.empVO || {}))
}

export function updateUser(patch) {
  const user = getUser() || {}
  localStorage.setItem(USER_KEY, JSON.stringify({ ...user, ...patch }))
}

export function clearLogin() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}
