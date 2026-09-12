package com.oa_server.module.admin.depts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa_server.module.admin.depts.dto.AdminDeptQueryDTO;
import com.oa_server.module.admin.depts.entity.Dept;
import com.oa_server.module.admin.depts.vo.AdminDeptVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 部门 Mapper 接口
 *
 * @author Alu
 * @date 2026-09-12
 */
@Mapper
public interface DeptSMapper extends BaseMapper<Dept> {
    Page<AdminDeptVO> selectDeptPage(Page<AdminDeptVO> page,@Param("adminDeptQueryDTO") AdminDeptQueryDTO adminDeptQueryDTO);

    Dept selectDeptByName(String deptName);

    void addDept(Dept dept);
}
