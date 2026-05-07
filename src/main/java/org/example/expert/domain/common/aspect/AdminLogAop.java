package org.example.expert.domain.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AdminLogAop {

    private final ObjectMapper objectMapper;

    public AdminLogAop(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Around("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..)) || " +
            "execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    public Object logAdminApi(ProceedingJoinPoint joinPoint) throws Throwable {
        // 현재 HTTP 요청 정보를 가져온다.
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();

        HttpServletRequest request = servletRequestAttributes.getRequest();

        Long userId = (Long) request.getAttribute("userId");
        LocalDateTime requestTime = LocalDateTime.now();
        String requestUrl = request.getRequestURI();
        String requestBody = objectMapper.writeValueAsString(joinPoint.getArgs());

        log.info("[Admin API Request] userId={}, requestTime={}, requestUrl={}, requestBody={}",
                userId, requestTime, requestUrl, requestBody);

        // API 실행 후 정상 응답 또는 예외 정보를 로깅한다.
        try {
            Object result = joinPoint.proceed();

            String responseBody = objectMapper.writeValueAsString(result);
            log.info("[Admin API Response] userId={}, requestTime={}, requestUrl={}, responseBody={}",
                    userId, requestTime, requestUrl, responseBody);

            return result;
        } catch (Throwable t) {
            String errorMessage = t.getMessage();
            log.error("[Admin API Error] userId={}, requestTime={}, requestUrl={}, error={}",
                    userId, requestTime, requestUrl, errorMessage, t);
            throw t;
        }
    }
}
