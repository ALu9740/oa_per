package com.oa_server.module.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa_server.module.emp.entity.Emp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Alu
 * @date 2026-09-09
 */
@Mapper
public interface EmpMapper extends BaseMapper<Emp> {

    Emp findByEmail(@Param("email")String email);
}
