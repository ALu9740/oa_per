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

// 部门管理（管理员）

export function getDeptList(params) {
  return request.get('/admin/depts/dept-list', { params })
}

export function addDept(data) {
  return request.post('/admin/depts/dept-add', data)
}

export function editDept(data) {
  return request.put('/admin/depts/dept-edit', data)
}

export function batchDeleteDept(deptIds) {
  return request.put('/admin/depts/dept-batch-delete', { deptIds })
}

// 职位管理（管理员）

export function getJobList(params) {
  return request.get('/admin/jobs/job-list', { params })
}

export function addJob(data) {
  return request.post('/admin/jobs/job-add', data)
}

export function editJob(data) {
  return request.put('/admin/jobs/job-edit', data)
}

export function batchDeleteJob(jobIds) {
  return request.delete('/admin/jobs/job-batch-delete', { data: { jobIds } })
}
