package com.oa_server.module.emp.controller;

import com.oa_server.common.result.Result;
import com.oa_server.module.emp.service.EmpService;
import com.oa_server.module.emp.vo.EmpVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 员工接口
 *
 * @author Alu
 * @date 2026-09-09
 */
@RestController
@RequestMapping("/api/emp")
@RequiredArgsConstructor
public class EmpController {

    private final EmpService empService;

    /**
     * 根据ID获取员工资料
     */
    @GetMapping("/{empId}")
    public Result<EmpVO> getEmpInfo(@PathVariable Long empId) {
        EmpVO empVO = empService.getEmpInfo(empId);
        return Result.success(empVO);
    }
}
