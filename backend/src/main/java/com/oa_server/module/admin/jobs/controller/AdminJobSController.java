package com.oa_server.module.admin.jobs.controller;

import com.oa_server.common.result.PageResult;
import com.oa_server.common.result.Result;
import com.oa_server.module.admin.jobs.dto.AdminJobQueryDTO;
import com.oa_server.module.admin.jobs.service.AdminJobSService;
import com.oa_server.module.admin.jobs.vo.AdminJobVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 职位管理接口
 *
 * @author Alu
 * @date 2026-09-13
 */
@RestController
@RequestMapping("/api/admin/jobs")
@RequiredArgsConstructor
public class AdminJobSController {
    private final AdminJobSService adminJobSService;

    /**
     * 分页查询职位列表
     */
    @GetMapping("/job-list")
    public Result<PageResult<AdminJobVO>> getJobList(AdminJobQueryDTO adminJobQueryDTO) {
        return Result.success(adminJobSService.getJobList(adminJobQueryDTO));
    }

}
