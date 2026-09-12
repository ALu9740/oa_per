package com.oa_server.module.admin.depts.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa_server.common.exception.BusinessException;
import com.oa_server.common.result.PageResult;
import com.oa_server.common.result.ResultCode;
import com.oa_server.module.admin.depts.dto.AdminDeptQueryDTO;
import com.oa_server.module.admin.depts.entity.Dept;
import com.oa_server.module.admin.depts.mapper.DeptSMapper;
import com.oa_server.module.admin.depts.service.AdminDeptSService;
import com.oa_server.module.admin.depts.vo.AdminDeptVO;
import com.oa_server.module.admin.depts.dto.AdminAddDeptDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 部门管理 服务实现
 *
 * @author Alu
 * @date 2026-09-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDeptSServiceImpl extends ServiceImpl<DeptSMapper, Dept> implements AdminDeptSService {
    private final DeptSMapper deptSMapper;

    @Override
    public PageResult<AdminDeptVO> getDeptList(AdminDeptQueryDTO adminDeptQueryDTO) {
        //空值兜底
        long pageNum = adminDeptQueryDTO.getPage() != null ? adminDeptQueryDTO.getPage() : 1L;
        long pageSize = adminDeptQueryDTO.getSize() != null ? adminDeptQueryDTO.getSize() : 10L;

        pageSize = Math.min(pageSize, 100L);

        //构造 MyBatis-Plus 分页对象
        Page<AdminDeptVO> page = new Page<>(pageNum, pageSize);

        //分页查询部门列表
        Page<AdminDeptVO> result = deptSMapper.selectDeptPage(page, adminDeptQueryDTO);

        return PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDept(AdminAddDeptDTO adminAddDeptDTO) {
        //根据部门名称查询部门是否存在
        Dept exitDept = deptSMapper.selectDeptByName(adminAddDeptDTO.getDeptName());
        if(exitDept != null){
            throw new BusinessException(ResultCode.DUPLICATE_NAME);
        }

        // 创建部门
        Dept dept = new Dept();
        dept.setDeptName(adminAddDeptDTO.getDeptName());
        dept.setDescription(adminAddDeptDTO.getDeptDesc());
        deptSMapper.addDept(dept);
    }
}
