import request from './request'

export function sendCode(email) {
  return request.post('/auth/send-code', { email })
}

export function verifyCode(email, code) {
  return request.get('/auth/verify-code', { params: { email, code } })
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function login(data) {
  return request.post('/auth/login', data)
}

export function resetPassword(data) {
  return request.post('/auth/reset-password', data)
}

export function completeProfile(data) {
  return request.put('/auth/complete-profile', data)
}

export function logout(refreshToken) {
  return request.post('/auth/logout', null, {
    headers: {
      'X-Refresh-Token': refreshToken || '',
    },
  })
}

export function refreshToken(token) {
  return request.post('/auth/refresh', null, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })
}
