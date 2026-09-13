package com.oa_server.module.admin.jobs.controller;

import com.oa_server.common.result.PageResult;
import com.oa_server.common.result.Result;
import com.oa_server.module.admin.jobs.dto.AdminAddJobDTO;
import com.oa_server.module.admin.jobs.dto.AdminBatchDeleteJobDTO;
import com.oa_server.module.admin.jobs.dto.AdminEditJobDTO;
import com.oa_server.module.admin.jobs.dto.AdminJobQueryDTO;
import com.oa_server.module.admin.jobs.service.AdminJobSService;
import com.oa_server.module.admin.jobs.vo.AdminJobVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 新增职位
     */
    @PostMapping("/job-add")
    public Result<Void> addJob(@Valid @RequestBody AdminAddJobDTO adminAddJobDTO) {
        adminJobSService.addJob(adminAddJobDTO);
        return Result.success();
    }

    /**
     * 编辑职位信息
     */
    @PutMapping("/job-edit")
    public Result<Void> editJob(@Valid @RequestBody AdminEditJobDTO adminEditJobDTO) {
        adminJobSService.editJob(adminEditJobDTO);
        return Result.success();
    }

    /**
     * 批量删除职位
     */
    @DeleteMapping("/job-batch-delete")
    public Result<Void> batchDeleteByIds(@Valid @RequestBody AdminBatchDeleteJobDTO adminBatchDeleteJobDTO) {
        adminJobSService.batchDeleteJob(adminBatchDeleteJobDTO);
        return Result.success();
    }
}
