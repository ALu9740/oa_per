package com.oa_server.module.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa_server.module.admin.emps.dto.AdminEmpQueryDTO;
import com.oa_server.module.admin.emps.vo.AdminEmpVO;
import com.oa_server.module.emp.entity.Emp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 员工 Mapper 接口
 *
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

    int updateAvatar(@Param("id") Long loginEmpId,@Param("avatar") String url);

    void changePassword(Emp emp);

    Page<AdminEmpVO> selectEmpPage(Page<AdminEmpVO> page, AdminEmpQueryDTO adminEmpQueryDTO);

    void addEmp(Emp emp);

    Emp findByPhone(String phone);

    void editEmp(Emp emp);
}
