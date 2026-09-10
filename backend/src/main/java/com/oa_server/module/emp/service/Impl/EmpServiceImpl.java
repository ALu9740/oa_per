package com.oa_server.module.emp.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa_server.module.emp.entity.Emp;
import com.oa_server.module.emp.mapper.EmpMapper;
import com.oa_server.module.emp.service.EmpService;
import com.oa_server.module.emp.vo.EmpVO;
import org.springframework.stereotype.Service;

/**
 * 员工服务实现
 *
 * @author Alu
 * @date 2026-09-09
 */
@Service
public class EmpServiceImpl extends ServiceImpl<EmpMapper, Emp> implements EmpService {


    @Override
    public EmpVO toVO(Emp emp) {
        EmpVO empVO = new EmpVO();
        BeanUtil.copyProperties(emp, empVO, "password");
        return empVO;
    }
}
