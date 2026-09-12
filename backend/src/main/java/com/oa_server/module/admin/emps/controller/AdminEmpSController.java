package com.oa_server.module.admin.emps.controller;


import com.oa_server.common.result.PageResult;
import com.oa_server.common.result.Result;
import com.oa_server.module.admin.emps.dto.AdminEmpQueryDTO;
import com.oa_server.module.admin.emps.service.AdminEmpSService;
import com.oa_server.module.admin.emps.vo.AdminEmpVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
