package com.oa_server.module.admin.emps.controller;


import com.oa_server.common.result.PageResult;
import com.oa_server.common.result.Result;
import com.oa_server.module.admin.emps.dto.*;
import com.oa_server.module.admin.emps.service.AdminEmpSService;
import com.oa_server.module.admin.emps.vo.AdminEmpVO;
import com.oa_server.module.admin.emps.vo.OptionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *员工管理接口
 *
 * @author Alu
 * @date 2026-09-12
 */
@RestController
@RequestMapping("/api/admin/emps" )
@RequiredArgsConstructor
public class AdminEmpSController {

    private final AdminEmpSService adminEmpSService;

    /**
     * 分页查询员工列表
     */
    @GetMapping("/emp-list")
    public Result<PageResult<AdminEmpVO>> getEmpList(AdminEmpQueryDTO adminEmpQueryDTO) {
        return Result.success(adminEmpSService.getEmpList(adminEmpQueryDTO));
    }

    /**
     * 新增员工
     */
    @PostMapping("/emp-add")
    public Result<Void> addEmp(@Valid @RequestBody AdminAddEmpDTO adminAddEmpDTO) {
        adminEmpSService.addEmp(adminAddEmpDTO);
        return Result.success();
    }

    /**
     * 编辑员工信息
     */
    @PutMapping("/emp-edit")
    public Result<Void> editEmp(@Valid @RequestBody AdminEditEmpDTO adminEditEmpDTO) {
        adminEmpSService.editEmp(adminEditEmpDTO);
        return Result.success();
    }

    /**
     * 更新账号状态
     */
    @PutMapping("/emp-account-status")
    public Result<Void> updateAccountStatus(@Valid @RequestBody AdminUpdateAccountStatusDTO adminUpdateAccountStatusDTO) {
        adminEmpSService.updateAccountStatus(adminUpdateAccountStatusDTO);
        return Result.success();
    }

    /**
     * 批量删除员工
     */
    @PutMapping("/emp-batch-delete")
    public Result<Void> batchDeleteEmp(@Valid @RequestBody AdminBatchDeleteEmpDTO adminBatchDeleteEmpDTO) {
        adminEmpSService.batchDeleteEmp(adminBatchDeleteEmpDTO);
        return Result.success();
    }

    /**
     * 部门选项列表
     */
    @GetMapping("/dept-options")
    public Result<List<OptionVO>> getDeptOptions() {
        return Result.success(adminEmpSService.getDeptOptions());
    }

    /**
     * 职位选项列表
     */
    @GetMapping("/job-options")
    public Result<List<OptionVO>> getJobOptions() {
        return Result.success(adminEmpSService.getJobOptions());
    }
}
