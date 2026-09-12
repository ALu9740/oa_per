package com.oa_server.module.admin.emps.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa_server.common.exception.BusinessException;
import com.oa_server.common.result.PageResult;
import com.oa_server.common.result.ResultCode;
import com.oa_server.module.admin.emps.dto.AdminAddEmpDTO;
import com.oa_server.module.admin.emps.dto.AdminEditEmpDTO;
import com.oa_server.module.admin.emps.dto.AdminEmpQueryDTO;
import com.oa_server.module.admin.emps.dto.AdminUpdateAccountStatusDTO;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editEmp(AdminEditEmpDTO adminEditEmpDTO) {
        //根据 id 查询员工是否存在
        Emp emp = empMapper.findById(adminEditEmpDTO.getId());
        if(emp == null){
            throw new BusinessException(ResultCode.EMP_NOT_FOUND);
        }

        //邮箱唯一校验
        if (adminEditEmpDTO.getEmail() != null && !adminEditEmpDTO.getEmail().equals(emp.getEmail())) {
            Emp exitByEmailEmp = empMapper.findByEmail(adminEditEmpDTO.getEmail());
            if (exitByEmailEmp != null) {
                throw new BusinessException(ResultCode.EMAIL_EXISTS);
            }
        }

        //手机号唯一校验
        if (adminEditEmpDTO.getPhone() != null && !adminEditEmpDTO.getPhone().equals(emp.getPhone())) {
            Emp exitByPhoneEmp = empMapper.findByPhone(adminEditEmpDTO.getPhone());
            if (exitByPhoneEmp != null) {
                throw new BusinessException(ResultCode.PHONE_EXISTS);
            }
        }

        //姓名
        if(StrUtil.isNotBlank(adminEditEmpDTO.getName())){
            emp.setName(adminEditEmpDTO.getName());
        }

        //性别
        if(ObjectUtil.isNotNull(adminEditEmpDTO.getGender())){
            emp.setGender(adminEditEmpDTO.getGender());
        }

        //手机号
        if(StrUtil.isNotBlank(adminEditEmpDTO.getPhone())){
            emp.setPhone(adminEditEmpDTO.getPhone());
        }

        //邮箱
        if(StrUtil.isNotBlank(adminEditEmpDTO.getEmail())){
            emp.setEmail(adminEditEmpDTO.getEmail());
        }

        //部门ID
        if(ObjectUtil.isNotNull(adminEditEmpDTO.getDeptId())){
            emp.setDeptId(adminEditEmpDTO.getDeptId());
        }

        //职位ID
        if(ObjectUtil.isNotNull(adminEditEmpDTO.getJobId())){
            emp.setJobId(adminEditEmpDTO.getJobId());
        }

        //入职时间
        if(ObjectUtil.isNotNull(adminEditEmpDTO.getHireDate())){
            emp.setHireDate(adminEditEmpDTO.getHireDate());
        }

        //更新
        empMapper.editEmp(emp);

        log.info("[管理员] 编辑员工：id={}",adminEditEmpDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAccountStatus(AdminUpdateAccountStatusDTO adminUpdateAccountStatusDTO) {
        //根据 id 查询员工是否存在
        Emp emp = empMapper.findById(adminUpdateAccountStatusDTO.getId());
        if(emp == null){
            throw new BusinessException(ResultCode.EMP_NOT_FOUND);
        }

        //账号状态
        if(ObjectUtil.isNotNull(adminUpdateAccountStatusDTO.getAccountStatus())){
            emp.setAccountStatus(adminUpdateAccountStatusDTO.getAccountStatus());
        }

        //更新
        empMapper.updateAccountStatus(emp);

        log.info("[管理员] 更新账号状态：id={},accountStatus={}",adminUpdateAccountStatusDTO.getId(),adminUpdateAccountStatusDTO.getAccountStatus());
    }
}
