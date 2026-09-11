package com.oa_server.module.emp.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa_server.common.exception.BusinessException;
import com.oa_server.common.result.ResultCode;
import com.oa_server.module.auth.dto.CompleteProfileDTO;
import com.oa_server.module.emp.dto.UpdateProfileDTO;
import com.oa_server.module.emp.entity.Emp;
import com.oa_server.module.emp.enums.EmpAccountStatusEnum;
import com.oa_server.module.emp.enums.EmpRoleTypeEnum;
import com.oa_server.module.emp.mapper.EmpMapper;
import com.oa_server.module.emp.service.EmpService;
import com.oa_server.module.emp.vo.EmpVO;
import com.oa_server.security.LoginEmp;
import com.oa_server.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 员工服务实现
 *
 * @author Alu
 * @date 2026-09-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmpServiceImpl extends ServiceImpl<EmpMapper, Emp> implements EmpService {

    private final EmpMapper empMapper;

    @Override
    public EmpVO empToEmpVO(Emp emp) {
        EmpVO empVO = new EmpVO();
        BeanUtil.copyProperties(emp, empVO, "password");
        return empVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeProfile(CompleteProfileDTO completeProfileDTO) {
        //从安全上下文拿当前登录人
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginEmp loginEmp)) {
            throw new BusinessException(ResultCode.LOGIN_EXPIRED);
        }

        //用 id 查最新账号
        Emp emp = empMapper.findById(loginEmp.getId());
        if (emp == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        int rows = empMapper.completeProfile(
                emp.getId(),
                completeProfileDTO.getName(),
                completeProfileDTO.getGender(),
                completeProfileDTO.getPhone(),
                EmpAccountStatusEnum.NORMAL.getCode());

        if (rows == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        log.info("[完善资料] id={} 完善成功，账号状态置为正常", emp.getId());
    }

    @Override
    public EmpVO getEmpInfo(Long empId) {
        // 拿到当前登录用户
        LoginEmp loginEmp = SecurityUtils.getCurrentEmp();
        Long loginEmpId = loginEmp.getId();
        Integer loginRoleType = loginEmp.getRoleType();
        Emp emp = empMapper.findById(empId);
        if(emp == null){
            throw new BusinessException(ResultCode.EMP_NOT_FOUND);
        }
        // 普通员工只能查看自己的资料
        if (loginRoleType == EmpRoleTypeEnum.NORMAL.getCode() && !empId.equals(loginEmpId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        EmpVO empVO = empToEmpVO(emp);
        log.info("[根据员工ID获取员工资料] id={} 员工资料={}", empId, empVO);
        return empVO;
    }

    @Override
    public EmpVO updateProfile(UpdateProfileDTO updateProfileDTO) {
        // 拿到当前登录用户
        Long loginEmpId = SecurityUtils.getCurrentEmpId();
        Emp emp = empMapper.findById(loginEmpId);
        // 检查员工是否存在
        if (emp == null) {
            throw new BusinessException(ResultCode.EMP_NOT_FOUND);
        }
        // 更新员工资料
        //姓名
        if (StrUtil.isNotBlank(updateProfileDTO.getName())){
            emp.setName(updateProfileDTO.getName());
        }
        //性别
        if (updateProfileDTO.getGender() != null){
            emp.setGender(updateProfileDTO.getGender());
        }
        //手机号
        if (StrUtil.isNotBlank(updateProfileDTO.getPhone())){
            emp.setPhone(updateProfileDTO.getPhone());
        }
        int rows = empMapper.updateProfile(emp.getId(),
                emp.getName(),
                emp.getGender(),
                emp.getPhone());

        if (rows == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        // 返回更新后的员工资料
        EmpVO empVO = empToEmpVO(emp);
        log.info("[更新员工资料] id={} 员工资料={}", emp.getId(), empVO);
        return empVO;
    }
}


