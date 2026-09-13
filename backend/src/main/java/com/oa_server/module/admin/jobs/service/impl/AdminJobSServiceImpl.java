package com.oa_server.module.admin.jobs.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa_server.common.result.PageResult;
import com.oa_server.module.admin.jobs.dto.AdminJobQueryDTO;
import com.oa_server.module.admin.jobs.entity.Job;
import com.oa_server.module.admin.jobs.mapper.JobSMapper;
import com.oa_server.module.admin.jobs.service.AdminJobSService;
import com.oa_server.module.admin.jobs.vo.AdminJobVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 职位管理 服务实现
 *
 * @author Alu
 * @date 2026-09-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminJobSServiceImpl extends ServiceImpl<JobSMapper, Job> implements AdminJobSService {
    private final JobSMapper jobSMapper;

    @Override
    public PageResult<AdminJobVO> getJobList(AdminJobQueryDTO adminJobQueryDTO) {
        //空值兜底
        long pageNum = adminJobQueryDTO.getPage() != null ? adminJobQueryDTO.getPage() : 1L;
        long pageSize = adminJobQueryDTO.getSize() != null ? adminJobQueryDTO.getSize() : 10L;

        pageSize = Math.min(pageSize, 100L);

        //构造 MyBatis-Plus 分页对象
        Page<AdminJobVO> page = new Page<>(pageNum, pageSize);
        //分页查询职位列表
        Page<AdminJobVO> result = jobSMapper.selectJobList(page, adminJobQueryDTO);

        log.info("分页查询职位列表成功，共 {} 条记录", result.getTotal());
        return PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );
    }
}
