package com.oa_server.module.admin.depts.service;

import com.oa_server.common.result.PageResult;
import com.oa_server.module.admin.depts.dto.AdminDeptQueryDTO;
import com.oa_server.module.admin.depts.vo.AdminDeptVO;

/**
 * 部门管理 服务实现
 *
 * @author Alu
 * @date 2026-09-12
 */
public interface AdminDeptSService {
    /**
     * 条件 + 分页查询部门列表
     *
     * @param adminDeptQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult<AdminDeptVO> getDeptList(AdminDeptQueryDTO adminDeptQueryDTO);
}
