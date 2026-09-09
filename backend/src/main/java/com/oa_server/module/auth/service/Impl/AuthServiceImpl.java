package com.oa_server.module.auth.service.Impl;

import cn.hutool.core.util.StrUtil;
import com.oa_server.common.exception.BusinessException;
import com.oa_server.common.result.ResultCode;
import com.oa_server.module.auth.dto.SendCodeDTO;
import com.oa_server.module.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
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

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration LIMIT_TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate stringRedisTemplate;
    private final JavaMailSender javaMailSender;

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
