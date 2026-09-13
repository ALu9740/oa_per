package com.oa_server.module.admin.jobs.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa_server.common.exception.BusinessException;
import com.oa_server.common.result.PageResult;
import com.oa_server.common.result.ResultCode;
import com.oa_server.module.admin.jobs.dto.AdminAddJobDTO;
import com.oa_server.module.admin.jobs.dto.AdminJobQueryDTO;
import com.oa_server.module.admin.jobs.entity.Job;
import com.oa_server.module.admin.jobs.mapper.JobSMapper;
import com.oa_server.module.admin.jobs.service.AdminJobSService;
import com.oa_server.module.admin.jobs.vo.AdminJobVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addJob(AdminAddJobDTO adminAddJobDTO) {
        //根据职位名称查询职位是否存在
        Job exitJob = jobSMapper.selectJobByName(adminAddJobDTO.getJobName());

        if(exitJob != null){
            throw new BusinessException(ResultCode.DUPLICATE_NAME_JOB);
        }

        // 创建职位
        Job job = new Job();
        job.setJobName(adminAddJobDTO.getJobName());
        job.setSort(adminAddJobDTO.getSort() != null ? adminAddJobDTO.getSort() : 0);
        //创建时间
        job.setCreatedAt(LocalDateTime.now());
        //更新时间
        job.setUpdatedAt(LocalDateTime.now());
        jobSMapper.addJob(job);
        log.info("新增职位成功，职位名称：{}", job.getJobName());
    }

}
