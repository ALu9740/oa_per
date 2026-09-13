package com.oa_server.module.admin.jobs.service;

import com.oa_server.common.result.PageResult;
import com.oa_server.module.admin.jobs.dto.AdminAddJobDTO;
import com.oa_server.module.admin.jobs.dto.AdminJobQueryDTO;
import com.oa_server.module.admin.jobs.vo.AdminJobVO;

/**
 * 职位管理服务接口
 *
 * @author Alu
 * @date 2026-09-13
 */
public interface AdminJobSService {
    /**
     * 条件 + 分页查询列表
     *
     * @param adminJobQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult<AdminJobVO> getJobList(AdminJobQueryDTO adminJobQueryDTO);

    /**
     * 新增职位
     *
     * @param adminAddDeptDTO 新增职位 DTO
     */
    void addJob(AdminAddJobDTO adminAddDeptDTO);
}
