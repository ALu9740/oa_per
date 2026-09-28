package com.oa_server.module.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.oa_server.common.exception.BusinessException;
import com.oa_server.common.result.ResultCode;
import com.oa_server.module.auth.dto.LoginDTO;
import com.oa_server.module.auth.dto.RegisterDTO;
import com.oa_server.module.auth.dto.ResetPasswordDTO;
import com.oa_server.module.auth.dto.SendCodeDTO;
import com.oa_server.module.auth.entity.AgreementRecord;
import com.oa_server.module.auth.mapper.AgreementRecordMapper;
import com.oa_server.module.auth.service.AuthService;
import com.oa_server.module.auth.service.CaptchaValidator;
import com.oa_server.module.auth.vo.LoginVo;
import com.oa_server.module.emp.entity.Emp;
import com.oa_server.module.emp.enums.EmpAccountStatusEnum;
import com.oa_server.module.emp.enums.EmpRoleTypeEnum;
import com.oa_server.module.emp.mapper.EmpMapper;
import com.oa_server.module.emp.service.EmpService;
import com.oa_server.security.LoginEmp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;


/**
 * 认证服务实现
 *
 * @author Alu
 * @date 2026-09-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String CODE_KEY_PREFIX = "auth:sendCode:code:";
    private static final String LIMIT_KEY_PREFIX = "auth:sendCode:limit:";
    private static final String LOGIN_LOCK_PREFIX = "auth:login:lock";
    private static final String LOGIN_FAIL_PREFIX = "auth:login:fail:";
    private static final int LOGIN_MAX_FAIL = 5;
    private static final String AGREEMENT_VERSION = "v1";

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration LIMIT_TTL = Duration.ofSeconds(60);
    private static final Duration LOGIN_FAIL_TTL = Duration.ofMinutes(15);
    private static final Duration LOGIN_LOCK_TTL = Duration.ofMinutes(15);


    private final StringRedisTemplate stringRedisTemplate;
    private final JavaMailSender javaMailSender;
    private final EmpMapper empMapper;
    private final EmpService empService;
    private final CaptchaValidator captchaValidator;
    private final AgreementRecordMapper agreementRecordMapper;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public void sendCode(SendCodeDTO sendCodeDTO) {
        //获取邮箱
        String email = sendCodeDTO.getEmail();
        //校验是否频繁操作
        String limitKey = LIMIT_KEY_PREFIX + email;
        Boolean exist = stringRedisTemplate.hasKey(limitKey);
        if (Boolean.TRUE.equals(exist)) {
            throw new BusinessException(ResultCode.CODE_SEND_TOO_FREQUENT);
        }
        //生成6位验证码
        String code = generateCode(6);

        //先将验证码缓存到redis
        stringRedisTemplate.opsForValue().set(CODE_KEY_PREFIX + email, code, CODE_TTL);

        //先发邮件，成功后再写限流标记
        sendVerificationEmail(email, code);
        stringRedisTemplate.opsForValue().set(limitKey, "1", LIMIT_TTL);
        log.info("[验证码] 验证码已发送: email={}", email);
    }

    @Override
    public Boolean verifyCode(String email, String code) {
        if (StrUtil.hasBlank(email, code)) {
            return false;
        }
        String cached = stringRedisTemplate.opsForValue().get(CODE_KEY_PREFIX + email);
        if (cached == null) {
            throw new BusinessException(ResultCode.CODE_INVALID);
        }
        return cached.equals(code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVo register(RegisterDTO registerDTO) {
        // 滑块验证码二次校验
        captchaValidator.check(registerDTO.getCaptchaToken());
        //校验验证码
        if(!verifyCode(registerDTO.getEmail(),registerDTO.getCode())){
            throw new BusinessException(ResultCode.CODE_NOT_MATCH);
        }
        //校验邮箱是否存在
        Emp exitEmp = empMapper.findByEmail(registerDTO.getEmail());
        if(exitEmp!=null){
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }
        //创建员工
        Emp emp = new Emp();
        long id = IdWorker.getId();
        emp.setId(id);
        emp.setEmail(registerDTO.getEmail());
        emp.setPassword(BCrypt.hashpw(registerDTO.getPassword()));


        //设置账号状态为：待完善资料
        emp.setAccountStatus(EmpAccountStatusEnum.PENDING.getCode());

        emp.setEmpNo(String.valueOf(id));

        emp.setRoleType(EmpRoleTypeEnum.NORMAL.getCode());

        //设置时间
        emp.setCreatedAt(LocalDateTime.now());
        emp.setUpdatedAt(LocalDateTime.now());

        empMapper.insertEmp(emp);

        // 记录协议签署
        AgreementRecord record = new AgreementRecord();
        record.setEmpId(emp.getId());
        record.setEmail(emp.getEmail());
        record.setAgreementVersion(AGREEMENT_VERSION);
        record.setUserIp(getClientIp());
        record.setAgreedAt(LocalDateTime.now());
        agreementRecordMapper.insert(record);

        //删除已使用验证码
        stringRedisTemplate.delete(CODE_KEY_PREFIX + registerDTO.getEmail());

        // 登录员工LoginEmp 存入SaSession
        StpUtil.login(emp.getId());
        StpUtil.getSession().set("loginEmp", new LoginEmp(emp));

        return buildLoginVO(emp);
    }

    @Override
    public LoginVo login(LoginDTO loginDTO) {
        // 滑块验证码二次校验
        captchaValidator.check(loginDTO.getCaptchaToken());

        String email = loginDTO.getEmail();

        // 账号锁定检查（Redis 防爆破）
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(LOGIN_LOCK_PREFIX + email))) {
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
        }

        // 查询用户
        Emp emp = empMapper.findByEmail(email);

        if(emp == null || !BCrypt.checkpw(loginDTO.getPassword(), emp.getPassword())){
            recordLoginFailure(email);
            throw new BusinessException(ResultCode.EMAIL_OR_PASSWORD_ERROR);
        }

        if(emp.getAccountStatus() != null && emp.getAccountStatus() == EmpAccountStatusEnum.DISABLED.getCode()){
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        stringRedisTemplate.delete(LOGIN_FAIL_PREFIX + email);

        //Sa-Token 登录员工,LoginEmp 存入SaSession
        StpUtil.login(emp.getId());
        StpUtil.getSession().set("loginEmp", new LoginEmp(emp));

        log.info("[登录] 用户登录成功: userId={}, email={}", emp.getId(), email);

        return buildLoginVO(emp);
    }

    @Override
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        // 滑块验证码二次校验
        captchaValidator.check(resetPasswordDTO.getCaptchaToken());
        //校验验证码
        if(!verifyCode(resetPasswordDTO.getEmail(),resetPasswordDTO.getCode())){
            throw new BusinessException(ResultCode.CODE_NOT_MATCH);
        }
        //校验邮箱是否存在
        Emp emp = empMapper.findByEmail(resetPasswordDTO.getEmail());
        if(emp == null){
            throw new BusinessException(ResultCode.EMAIL_NOT_FOUND);
        }
        //更新密码
        emp.setPassword(BCrypt.hashpw(resetPasswordDTO.getPassword()));
        //设置时间
        emp.setUpdatedAt(LocalDateTime.now());
        empMapper.resetPassword(emp);

        //删除已使用验证码
        stringRedisTemplate.delete(CODE_KEY_PREFIX + resetPasswordDTO.getEmail());

        log.info("[重置密码] 员工密码已重置: userId={}, email={}", emp.getId(), emp.getEmail());
    }

    @Override
    public LoginVo refresh(String refreshToken) {
        // token 必须还在有效期内（7 天）
        Object loginId = StpUtil.getLoginIdByToken(refreshToken);
        if (loginId == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        Long empId = Long.parseLong(loginId.toString());
        Emp emp = empMapper.findById(empId);
        if (emp == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 滚动续期：把 token 有效期重置为 7 天
        StpUtil.renewTimeout(refreshToken, 604800);

        return buildLoginVO(emp);
    }

    @Override
    public void logout(String accessToken, String refreshTokenHeader) {
        String token = accessToken;
        if (StrUtil.isBlank(token) && StrUtil.isNotBlank(refreshTokenHeader)) {
            token = refreshTokenHeader.startsWith("Bearer ")
                    ? refreshTokenHeader.substring(7) : refreshTokenHeader;
        }
        if (StrUtil.isBlank(token)) {
            return;
        }
        // 从 Sa-Token 中获取登录态
        Object loginId = StpUtil.getLoginIdByToken(token);
        //从 Sa-Token 中移除登录态
        StpUtil.logout(token);
        log.info("[登出] 用户登出成功: userId={}", loginId);
    }

    /**
     * 记录登录失败次数，超阈值则锁定账号
     */
    private void recordLoginFailure(String email) {
        String failKey = LOGIN_FAIL_PREFIX + email;
        Long count = stringRedisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(failKey, LOGIN_FAIL_TTL);
        }
        if (count != null && count >= LOGIN_MAX_FAIL) {
            stringRedisTemplate.opsForValue().set(LOGIN_LOCK_PREFIX + email, "1", LOGIN_LOCK_TTL);
            stringRedisTemplate.delete(failKey);
            log.warn("[登录] 账号多次失败被锁定: email={}, count={}", email, count);
        }
    }

    /**
     * 构建登录返回对象
     * @param emp 员工信息
     * @return LoginVo 登录VO
     */
    private LoginVo buildLoginVO(Emp emp) {
        LoginVo vo = new LoginVo();
        // 从 Sa-Token 中获取 token
        String token = StpUtil.getTokenValue();
        vo.setAccessToken(token);
        vo.setRefreshToken(token);
        vo.setExpiresIn(7200L);
        vo.setEmpVO(empService.empToEmpVO(emp));
        return vo;
    }

    /**
     * 发送验证码邮件
     * @param email 收件人邮箱
     * @param code 验证码
     */
    private void sendVerificationEmail(String email, String code) {
        try {
            // 创建邮件消息对象
            SimpleMailMessage message = new SimpleMailMessage();
            // 设置发件人
            message.setFrom(senderEmail);
            // 设置收件人
            message.setTo(email);
            // 设置邮件主题
            message.setSubject("【OA系统】您的邮箱验证码");
            // 设置邮件内容
            message.setText("您的验证码是：" + code + "，该验证码 5 分钟内有效。如果不是您本人操作，请忽略此邮件。");
            // 发送邮件
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("邮件发送异常", e);
            throw new BusinessException(ResultCode.EMAIL_SEND_FAILED);
        }
    }

    /**
     * 生成指定长度的随机验证码
     * @param length 验证码长度
     * @return 验证码字符串
     */
    private String generateCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 获取客户端IP（协议签署留痕用）
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return JakartaServletUtil.getClientIP(attrs.getRequest());
            }
        } catch (Exception e) {
            log.warn("[注册] 获取客户端IP失败: {}", e.getMessage());
        }
        return null;
    }
}
