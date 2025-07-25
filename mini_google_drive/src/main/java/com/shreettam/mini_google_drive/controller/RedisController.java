package com.shreettam.mini_google_drive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/redis/ping")
    public String test() {
        redisTemplate.opsForValue().set("ping", "pong");
        return redisTemplate.opsForValue().get("ping");
    }
}

