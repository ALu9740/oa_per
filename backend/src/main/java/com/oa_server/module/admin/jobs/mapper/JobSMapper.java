package com.oa_server.module.admin.jobs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa_server.module.admin.jobs.dto.AdminJobQueryDTO;
import com.oa_server.module.admin.jobs.entity.Job;
import com.oa_server.module.admin.jobs.vo.AdminJobVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 职位 Mapper 接口
 *
 * @author Alu
 * @date 2026-09-13
 */
@Mapper
public interface JobSMapper extends BaseMapper<Job> {
    Page<AdminJobVO> selectJobList(Page<AdminJobVO> page,@Param("adminJobQueryDTO") AdminJobQueryDTO adminJobQueryDTO);
}
