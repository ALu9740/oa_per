package com.oa_server.module.emp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oa_server.module.auth.dto.CompleteProfileDTO;
import com.oa_server.module.emp.entity.Emp;
import com.oa_server.module.emp.vo.EmpVO;

/**
 * 员工服务接口
 *
 * @author Alu
 * @date 2026-09-09
 */
public interface EmpService extends IService<Emp> {

    /**
     * 实体转 VO
     *
     * @param emp 员工实体
     * @return 员工 VO
     */
    EmpVO toVO(Emp emp);

    /**
     * 完善资料
     *
     * @param completeProfileDTO 完善资料DTO
     */
    void completeProfile(CompleteProfileDTO completeProfileDTO);
}
