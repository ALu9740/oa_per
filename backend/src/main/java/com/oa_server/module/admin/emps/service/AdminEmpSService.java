package com.oa_server.module.admin.emps.service;

import com.oa_server.common.result.PageResult;
import com.oa_server.module.admin.emps.dto.AdminEmpQueryDTO;
import com.oa_server.module.admin.emps.vo.AdminEmpVO;

/**
 * 员工管理 服务实现
 *
 * @author Alu
 * @date 2026-09-12
 */
public interface AdminEmpSService {
    /**
     * 条件 + 分页查询员工列表
     *
     * @param adminEmpQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult<AdminEmpVO> getEmpList(AdminEmpQueryDTO adminEmpQueryDTO);
}
