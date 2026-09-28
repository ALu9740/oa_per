package com.oa_server.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa_server.module.auth.entity.AgreementRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 协议签署记录 Mapper
 *
 * @author Alu
 * @date 2026-09-28
 */
@Mapper
public interface AgreementRecordMapper extends BaseMapper<AgreementRecord> {
}