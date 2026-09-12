package com.oa_server.module.admin.depts.controller;

import com.oa_server.common.result.PageResult;
import com.oa_server.common.result.Result;
import com.oa_server.module.admin.depts.dto.AdminDeptQueryDTO;
import com.oa_server.module.admin.depts.service.AdminDeptSService;
import com.oa_server.module.admin.depts.vo.AdminDeptVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 部门管理接口
 *
 * @author Alu
 * @date 2026-09-12
 */
@RestController
@RequestMapping("/api/admin/depts")
@RequiredArgsConstructor
public class AdminDeptSController {

    private final AdminDeptSService adminDeptSService;

    /**
     * 分页查询部门列表
     */
    @GetMapping("/dept-list")
    public Result<PageResult<AdminDeptVO>> getDeptList(AdminDeptQueryDTO adminDeptQueryDTO) {
        return Result.success(adminDeptSService.getDeptList(adminDeptQueryDTO));
    }
}
