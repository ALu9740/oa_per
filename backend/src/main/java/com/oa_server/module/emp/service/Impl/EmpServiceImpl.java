package com.oa_server.module.emp.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa_server.common.exception.BusinessException;
import com.oa_server.common.result.ResultCode;
import com.oa_server.module.auth.dto.CompleteProfileDTO;
import com.oa_server.module.emp.entity.Emp;
import com.oa_server.module.emp.enums.EmpAccountStatusEnum;
import com.oa_server.module.emp.mapper.EmpMapper;
import com.oa_server.module.emp.service.EmpService;
import com.oa_server.module.emp.vo.EmpVO;
import com.oa_server.security.LoginEmp;
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
    public EmpVO toVO(Emp emp) {
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

        int rows = empMapper.updateProfile(
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
}
