import request from './request'

// 员工管理（管理员）

export function getEmpList(params) {
  return request.get('/admin/emps/emp-list', { params })
}

export function addEmp(data) {
  return request.post('/admin/emps/emp-add', data)
}

export function editEmp(data) {
  return request.put('/admin/emps/emp-edit', data)
}

export function updateEmpStatus(data) {
  return request.put('/admin/emps/emp-account-status', data)
}

export function batchDeleteEmp(empIds) {
  return request.put('/admin/emps/emp-batch-delete', { empIds })
}

export function getDeptOptions() {
  return request.get('/admin/emps/dept-options')
}

export function getJobOptions() {
  return request.get('/admin/emps/job-options')
}
