package ru.yandex.yandexlavka.configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class PathRateLimiter {
    private final Map<String, Bucket> getBucket = new HashMap<>();
    private final Map<String, Bucket> postBucket = new HashMap<>();

    public boolean tryConsume(String path, String method) {
        Map<String, Bucket> map = null;

        if (HttpMethod.GET.matches(method))
            map = getBucket;
        else if (HttpMethod.POST.matches(method))
            map = postBucket;
        else
            return false;

        if (!map.containsKey(path))
            map.put(path, Bucket.builder().addLimit(Bandwidth.simple(10, Duration.ofSeconds(1))).build());

        return map.get(path).tryConsume(1);
    }
}
