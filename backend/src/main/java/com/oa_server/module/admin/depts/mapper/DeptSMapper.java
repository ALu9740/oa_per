package com.oa_server.module.admin.depts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa_server.module.admin.depts.dto.AdminDeptQueryDTO;
import com.oa_server.module.admin.depts.entity.Dept;
import com.oa_server.module.admin.depts.vo.AdminDeptVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门 Mapper 接口
 *
 * @author Alu
 * @date 2026-09-12
 */
@Mapper
public interface DeptSMapper extends BaseMapper<Dept> {
    Page<AdminDeptVO> selectDeptPage(Page<AdminDeptVO> page,@Param("adminDeptQueryDTO") AdminDeptQueryDTO adminDeptQueryDTO);

    Dept selectDeptByName(@Param("deptName") String deptName);

    void addDept(Dept dept);

    Dept findByIdDept(@Param("id") Long id);

    void editDept(Dept dept);

    int countByIds(@Param("ids") List<Long> ids);

    void batchDeleteByIds(@Param("ids") List<Long> ids);

    List<Long> selectDeptIdsWithEmployees(@Param("deptIds") List<Long> deptIds);
}
