package com.oa_server.module.admin.depts.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa_server.common.exception.BusinessException;
import com.oa_server.common.result.PageResult;
import com.oa_server.common.result.ResultCode;
import com.oa_server.module.admin.depts.dto.AdminBatchDeleteDeptDTO;
import com.oa_server.module.admin.depts.dto.AdminDeptQueryDTO;
import com.oa_server.module.admin.depts.dto.AdminEditDeptDTO;
import com.oa_server.module.admin.depts.entity.Dept;
import com.oa_server.module.admin.depts.mapper.DeptSMapper;
import com.oa_server.module.admin.depts.service.AdminDeptSService;
import com.oa_server.module.admin.depts.vo.AdminDeptVO;
import com.oa_server.module.admin.depts.dto.AdminAddDeptDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
        //创建时间
        dept.setCreatedAt(LocalDateTime.now());
        //更新时间
        dept.setUpdatedAt(LocalDateTime.now());
        deptSMapper.addDept(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editDept(AdminEditDeptDTO adminEditDeptDTO) {
        //根据部门ID查询部门是否存在
        Dept dept = deptSMapper.findByIdDept(adminEditDeptDTO.getId());
        if(dept == null){
            throw new BusinessException(ResultCode.DEPT_NOT_FOUND);
        }
        //部门名称是否重复
        if (adminEditDeptDTO.getName() != null && !adminEditDeptDTO.getName().equals(dept.getDeptName())) {
            Dept exitByNameDept = deptSMapper.selectDeptByName(adminEditDeptDTO.getName());
            if(exitByNameDept != null) {
                throw new BusinessException(ResultCode.DUPLICATE_NAME);
            }
        }
        //部门名称
        if (StrUtil.isNotBlank(adminEditDeptDTO.getName())) {
            dept.setDeptName(adminEditDeptDTO.getName());
        }
        //部门描述
        if (StrUtil.isNotBlank(adminEditDeptDTO.getDescription())) {
            dept.setDescription(adminEditDeptDTO.getDescription());
        }
        //更新时间
        dept.setUpdatedAt(LocalDateTime.now());
        //更新部门信息
        deptSMapper.editDept(dept);
        log.info("[管理员] 编辑部门：id={}",adminEditDeptDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteDept(AdminBatchDeleteDeptDTO adminBatchDeleteDeptDTO) {
        List<Long> ids = adminBatchDeleteDeptDTO.getDeptIds();
        //校验部门是否存在
        int exitCount = deptSMapper.countByIds(ids);
        if(exitCount != ids.size()){
            throw new BusinessException(ResultCode.DEPT_NOT_FOUND);
        }

        //校验部门下是否有员工
        List<Long> deptWithEmp = deptSMapper.selectDeptIdsWithEmployees(ids);
        if (!deptWithEmp.isEmpty()) {
            throw new BusinessException(ResultCode.DEPT_HAS_EMPLOYEES);
        }

        //批量删除部门
        deptSMapper.batchDeleteByIds(ids);

        log.info("[管理员] 批量删除部门：共{}个部门, ids={}", ids.size(), ids);
    }

}
