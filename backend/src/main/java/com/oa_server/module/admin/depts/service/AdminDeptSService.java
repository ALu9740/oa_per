package com.oa_server.module.admin.depts.service;

import com.oa_server.common.result.PageResult;
import com.oa_server.module.admin.depts.dto.AdminDeptQueryDTO;
import com.oa_server.module.admin.depts.dto.AdminEditDeptDTO;
import com.oa_server.module.admin.depts.vo.AdminDeptVO;
import com.oa_server.module.admin.depts.dto.AdminAddDeptDTO;

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

    /**
     * 新增部门
     *
     * @param adminAddDeptDTO 新增员工 DTO
     */
    void addDept(AdminAddDeptDTO adminAddDeptDTO);

    /**
     * 编辑部门信息
     *
     * @param adminEditDeptDTO 编辑员工信息 DTO
     */
    void editDept(AdminEditDeptDTO adminEditDeptDTO);
}
