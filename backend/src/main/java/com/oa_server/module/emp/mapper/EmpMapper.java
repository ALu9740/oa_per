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

    Emp findById(@Param("id") Long id);

    int insertEmp(Emp emp);

    int completeProfile(@Param("id") Long id, @Param("name") String name, @Param("gender") Integer gender, @Param("phone") String phone, @Param("accountStatus") Integer accountStatus);

    void resetPassword(Emp emp);

    int updateProfile(@Param("id") Long id, @Param("name") String name, @Param("gender") Integer gender, @Param("phone") String phone);
}
