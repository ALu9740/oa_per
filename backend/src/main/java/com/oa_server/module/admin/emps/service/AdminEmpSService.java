package com.oa_server.module.admin.emps.service;

import com.oa_server.common.result.PageResult;
import com.oa_server.module.admin.emps.dto.*;
import com.oa_server.module.admin.emps.vo.AdminEmpVO;
import com.oa_server.module.admin.emps.vo.OptionVO;

import java.util.List;

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

    /**
     * 新增员工

     * @param adminAddEmpDTO 新增员工 DTO
     */
    void addEmp(AdminAddEmpDTO adminAddEmpDTO);

    /**
     * 编辑员工信息
     *
     * @param adminEditEmpDTO 编辑员工信息 DTO
     */
    void editEmp(AdminEditEmpDTO adminEditEmpDTO);

    /**
     * 更新账号状态
     *
     * @param adminUpdateAccountStatusDTO 更新账号状态 DTO
     */
    void updateAccountStatus(AdminUpdateAccountStatusDTO adminUpdateAccountStatusDTO);

    /**
     * 批量删除员工
     *
     * @param adminBatchDeleteEmpDTO 批量删除员工 DTO
     */
    void batchDeleteEmp(AdminBatchDeleteEmpDTO adminBatchDeleteEmpDTO);

    /**
     * 部门选项列表
     */
    List<OptionVO> getDeptOptions();

    /**
     * 职位选项列表
     */
    List<OptionVO> getJobOptions();
}
