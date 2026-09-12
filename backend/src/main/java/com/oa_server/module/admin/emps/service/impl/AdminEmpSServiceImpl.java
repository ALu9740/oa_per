package com.oa_server.module.admin.emps.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa_server.common.exception.BusinessException;
import com.oa_server.common.result.PageResult;
import com.oa_server.common.result.ResultCode;
import com.oa_server.module.admin.emps.dto.AdminAddEmpDTO;
import com.oa_server.module.admin.emps.dto.AdminEmpQueryDTO;
import com.oa_server.module.admin.emps.service.AdminEmpSService;
import com.oa_server.module.admin.emps.vo.AdminEmpVO;
import com.oa_server.module.emp.entity.Emp;
import com.oa_server.module.emp.enums.EmpAccountStatusEnum;
import com.oa_server.module.emp.enums.EmpRoleTypeEnum;
import com.oa_server.module.emp.mapper.EmpMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 员工管理 服务实现
 *
 * @author Alu
 * @date 2026-09-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminEmpSServiceImpl implements AdminEmpSService {

    private final EmpMapper empMapper;
    private final PasswordEncoder passwordEncoder;

    private final String DEFAULT_PASSWORD = "123456";

    @Override
    public PageResult<AdminEmpVO> getEmpList(AdminEmpQueryDTO adminEmpQueryDTO) {
        //空值兜底
        long pageNum = adminEmpQueryDTO.getPage() != null ? adminEmpQueryDTO.getPage() : 1L;
        long pageSize = adminEmpQueryDTO.getSize() != null ? adminEmpQueryDTO.getSize() : 10L;

        pageSize = Math.min(pageSize, 100L);
        //构造 MyBatis-Plus 分页对象
        Page<AdminEmpVO> page = new Page<>(pageNum, pageSize);

        //分页查询员工列表
        Page<AdminEmpVO> result = empMapper.selectEmpPage(page, adminEmpQueryDTO);

        //转成 PageResult 返回
        return PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addEmp(AdminAddEmpDTO adminAddEmpDTO) {
        //根据邮箱查询员工是否存在
        Emp exitByEmailEmp = empMapper.findByEmail(adminAddEmpDTO.getEmail());
        if(exitByEmailEmp != null){
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }
        //根据号码查询员工是否存在
        Emp exitByPhoneEmp = empMapper.findByPhone(adminAddEmpDTO.getPhone());
        if(exitByPhoneEmp != null){
            throw new BusinessException(ResultCode.PHONE_EXISTS);
        }
        // 创建员工
        Emp emp = new Emp();
        long id = IdWorker.getId();
        emp.setId(id);
        emp.setEmpNo(String.valueOf(id));
        emp.setName(adminAddEmpDTO.getName());
        emp.setGender(adminAddEmpDTO.getGender());
        emp.setEmail(adminAddEmpDTO.getEmail());
        emp.setPhone(adminAddEmpDTO.getPhone());
        emp.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        emp.setDeptId(adminAddEmpDTO.getDeptId());
        emp.setJobId(adminAddEmpDTO.getJobId());
        emp.setHireDate(adminAddEmpDTO.getHireDate());
        emp.setRoleType(EmpRoleTypeEnum.NORMAL.getCode());
        emp.setAccountStatus(EmpAccountStatusEnum.NORMAL.getCode());
        empMapper.addEmp(emp);
        log.info("[管理员] 新增员工：name={},email={}",adminAddEmpDTO.getName(),adminAddEmpDTO.getEmail());
    }
}
