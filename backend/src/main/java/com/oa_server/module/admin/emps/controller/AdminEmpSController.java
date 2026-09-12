package com.oa_server.module.admin.emps.controller;


import com.oa_server.common.result.PageResult;
import com.oa_server.common.result.Result;
import com.oa_server.module.admin.emps.dto.AdminAddEmpDTO;
import com.oa_server.module.admin.emps.dto.AdminEditEmpDTO;
import com.oa_server.module.admin.emps.dto.AdminEmpQueryDTO;
import com.oa_server.module.admin.emps.service.AdminEmpSService;
import com.oa_server.module.admin.emps.vo.AdminEmpVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
