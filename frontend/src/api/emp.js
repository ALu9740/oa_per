import request from './request'

export function getEmpInfo(empId) {
  return request.get(`/emp/${empId}`)
}

export function updateProfile(data) {
  return request.put('/emp/update-profile', data)
}

export function uploadAvatar(base64) {
  return request.post('/emp/upload-avatar', { base64 })
}

export function changePassword(data) {
  return request.put('/emp/change-password', data)
}