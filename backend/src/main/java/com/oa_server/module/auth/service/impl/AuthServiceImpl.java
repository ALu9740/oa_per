package com.oa_server.module.auth.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.oa_server.common.exception.BusinessException;
import com.oa_server.common.result.ResultCode;
import com.oa_server.module.auth.dto.LoginDTO;
import com.oa_server.module.auth.dto.RegisterDTO;
import com.oa_server.module.auth.dto.ResetPasswordDTO;
import com.oa_server.module.auth.dto.SendCodeDTO;
import com.oa_server.module.auth.service.AuthService;
import com.oa_server.module.auth.vo.LoginVo;
import com.oa_server.module.emp.entity.Emp;
import com.oa_server.module.emp.enums.EmpAccountStatusEnum;
import com.oa_server.module.emp.enums.EmpRoleTypeEnum;
import com.oa_server.module.emp.mapper.EmpMapper;
import com.oa_server.module.emp.service.EmpService;
import com.oa_server.security.JwtUtil;
import com.oa_server.security.LoginEmp;
import com.oa_server.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private static final String TOKEN_BLACKLIST_PREFIX = "auth:token:blacklist:";
    private static final int LOGIN_MAX_FAIL = 5;

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration LIMIT_TTL = Duration.ofSeconds(60);
    private static final Duration LOGIN_FAIL_TTL = Duration.ofMinutes(15);
    private static final Duration LOGIN_LOCK_TTL = Duration.ofMinutes(15);


    private final StringRedisTemplate stringRedisTemplate;
    private final JavaMailSender javaMailSender;
    private final EmpMapper empMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmpService empService;

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
        emp.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        //设置账号状态为：待完善资料
        emp.setAccountStatus(EmpAccountStatusEnum.PENDING.getCode());

        emp.setEmpNo(String.valueOf(id));

        emp.setRoleType(EmpRoleTypeEnum.NORMAL.getCode());

        //设置时间
        emp.setCreatedAt(LocalDateTime.now());
        emp.setUpdatedAt(LocalDateTime.now());

        empMapper.insertEmp(emp);

        //删除已使用验证码
        stringRedisTemplate.delete(CODE_KEY_PREFIX + registerDTO.getEmail());

        return buildLoginVO(emp);
    }

    @Override
    public LoginVo login(LoginDTO loginDTO) {
        String email = loginDTO.getEmail();

        // 账号锁定检查（Redis 防爆破）
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(LOGIN_LOCK_PREFIX + email))) {
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
        }

        // 查询用户
        Emp emp = empMapper.findByEmail(email);

        if(emp == null || !passwordEncoder.matches(loginDTO.getPassword(), emp.getPassword())){
            recordLoginFailure(email);
            throw new BusinessException(ResultCode.EMAIL_OR_PASSWORD_ERROR);
        }

        if(emp.getAccountStatus() != null && emp.getAccountStatus() == EmpAccountStatusEnum.DISABLED.getCode()){
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        stringRedisTemplate.delete(LOGIN_FAIL_PREFIX + email);

        log.info("[登录] 用户登录成功: userId={}, email={}", emp.getId(), email);

        return buildLoginVO(emp);
    }

    @Override
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
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
        emp.setPassword(passwordEncoder.encode(resetPasswordDTO.getPassword()));
        //设置时间
        emp.setUpdatedAt(LocalDateTime.now());
        empMapper.resetPassword(emp);

        //删除已使用验证码
        stringRedisTemplate.delete(CODE_KEY_PREFIX + resetPasswordDTO.getEmail());

        log.info("[重置密码] 员工密码已重置: userId={}, email={}", emp.getId(), emp.getEmail());
    }

    @Override
    public LoginVo refresh(String refreshToken) {
        //校验 Token 签名和有效期
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 检查是否已被废弃（Redis 黑名单）
        String tokenHash = DigestUtil.sha256Hex(refreshToken);
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + tokenHash;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(blacklistKey))) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        //从 Token 解析出用户信息
        String email = jwtUtil.getEmailFromToken(refreshToken);
        Long empID = jwtUtil.getEmpIdFromToken(refreshToken);
        Emp emp = empMapper.findById(empID);
        if (emp == null || !email.equals(emp.getEmail())) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 把旧的 refreshToken 加入黑名单（有效期 = refreshToken剩余过期时间）
        long remainingTime = jwtUtil.getRemainingTime(refreshToken);
        if (remainingTime > 0) {
            stringRedisTemplate.opsForValue().set(blacklistKey, "1", Duration.ofMillis(remainingTime));
        }

        //生成新的 Token
        return buildLoginVO(emp);
    }

    @Override
    public void logout(String accessToken, String refreshTokenHeader) {
        Long empId = null;

        //从 Security 上下文获取登录人（accessToken 可能已过期，拿不到也继续处理黑名单）
        try {
            LoginEmp emp = SecurityUtils.getCurrentEmp();
            empId = emp.getId();
        } catch (Exception e) {
            log.warn("[登出] 无有效登录态，继续处理 token 黑名单: {}", e.getMessage());
        }

        //accessToken 加入黑名单
        if (StrUtil.isNotBlank(accessToken) && jwtUtil.validateToken(accessToken)) {
            addToBlacklist(accessToken);
        }

        //refreshToken 也加入黑名单
        if (StrUtil.isNotBlank(refreshTokenHeader)) {
            String refreshToken = refreshTokenHeader.startsWith("Bearer ")
                    ? refreshTokenHeader.substring(7) : refreshTokenHeader;
            if (jwtUtil.validateToken(refreshToken) && jwtUtil.isRefreshToken(refreshToken)) {
                addToBlacklist(refreshToken);
            }
        }

        log.info("[登出] 用户登出成功: userId={}", empId);
    }

    /**
     * 把 token 加入黑名单
     */
    private void addToBlacklist(String token) {
        long remainingTime = jwtUtil.getRemainingTime(token);
        if (remainingTime > 0) {
            String tokenHash = DigestUtil.sha256Hex(token);
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + tokenHash;
            stringRedisTemplate.opsForValue().set(blacklistKey, "1", Duration.ofMillis(remainingTime));
        }
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
     */
    private LoginVo buildLoginVO(Emp emp) {
        LoginVo vo = new LoginVo();
        vo.setAccessToken(jwtUtil.generateAccessToken(emp.getId(), emp.getEmail()));
        vo.setRefreshToken(jwtUtil.generateRefreshToken(emp.getId(), emp.getEmail()));
        vo.setExpiresIn(jwtUtil.getAccessTokenExpiration() / 1000);
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


}
