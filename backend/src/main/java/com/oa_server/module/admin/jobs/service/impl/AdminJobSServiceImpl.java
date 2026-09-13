package com.oa_server.module.admin.jobs.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa_server.common.exception.BusinessException;
import com.oa_server.common.result.PageResult;
import com.oa_server.common.result.ResultCode;
import com.oa_server.module.admin.jobs.dto.AdminAddJobDTO;
import com.oa_server.module.admin.jobs.dto.AdminBatchDeleteJobDTO;
import com.oa_server.module.admin.jobs.dto.AdminEditJobDTO;
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
import java.util.List;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editJob(AdminEditJobDTO adminEditJobDTO) {
        //根据职位ID查询职位是否存在
        Job job = jobSMapper.findByIdJob(adminEditJobDTO.getId());
        if(job == null){
            throw new BusinessException(ResultCode.NOT_FOUND_JOB);
        }
        //职位名称是否重复
        if (adminEditJobDTO.getName() != null && !adminEditJobDTO.getName().equals(job.getJobName())){
            Job exitByNameJob = jobSMapper.selectJobByName(adminEditJobDTO.getName());
            if (exitByNameJob != null){
                throw new BusinessException(ResultCode.DUPLICATE_NAME_JOB);
            }
        }
        //职位名称
        if (StrUtil.isNotBlank(adminEditJobDTO.getName())) {
            job.setJobName(adminEditJobDTO.getName());
        }
        //排序
        if (ObjectUtil.isNotNull(adminEditJobDTO.getSort())){
            job.setSort(adminEditJobDTO.getSort());
        }
        //更新时间
        job.setUpdatedAt(LocalDateTime.now());
        //更新
        jobSMapper.updateJob(job);
        log.info("[管理员] 更新职位：id={}", adminEditJobDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteJob(AdminBatchDeleteJobDTO adminBatchDeleteJobDTO) {
        List<Long> ids = adminBatchDeleteJobDTO.getJobIds();
        //检查职位是否存在
        int exitCount = jobSMapper.countByIds(ids);
        if(exitCount != ids.size()){
            throw new BusinessException(ResultCode.NOT_FOUND_JOB);
        }

        //检查职位下是否有员工
        List<Long> jobWithEmp = jobSMapper.selectIdsWithEmps(ids);
        if  (!jobWithEmp.isEmpty()){
            throw new BusinessException(ResultCode.JOB_HAS_EMPLOYEES);
        }
        //批量删除职位
        jobSMapper.batchDeleteByIds(ids);
        log.info("[管理员] 批量删除职位：共{}个职位, ids={}", ids.size(), ids);
    }

}
