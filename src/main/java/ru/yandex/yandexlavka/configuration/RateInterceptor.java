package ru.yandex.yandexlavka.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentMap;

@Slf4j
public class RateInterceptor implements HandlerInterceptor {
    private final ConcurrentMap<String, PathRateLimiter> limiters;

    public RateInterceptor(ConcurrentMap<String, PathRateLimiter> limiters) {
        this.limiters = limiters;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        var remoteAddr = request.getRemoteAddr();

        if (!limiters.containsKey(remoteAddr))
            limiters.put(remoteAddr, new PathRateLimiter());

        if (limiters.get(remoteAddr).tryConsume(request.getServletPath(), request.getMethod()))
            return HandlerInterceptor.super.preHandle(request, response, handler);

        log.warn("Request limit is exceeded!");
        response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests!");

        return false;
    }
}
