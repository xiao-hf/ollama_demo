package com.xiao.service.impl;

import com.alibaba.fastjson.JSON;
import com.xiao.dto.OllamaRequestDTO;
import com.xiao.service.AskService;
import com.xiao.utils.HttpUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AskServiceImpl implements AskService {

    @Resource
    HttpUtil httpUtil;

    @Override
    public String ask(String text) {
        String url = "http://localhost:11434/api/generate";
        String modelName = "deepseek-r1:8b";
        Map<String, String> headers = new HashMap<>();
        headers.put("model", modelName);
        headers.put("prompt", "你好");
        String resStr = httpUtil.doPost(url, headers, null);
        OllamaRequestDTO res = JSON.parseObject(resStr, OllamaRequestDTO.class);
        return res.toString();
    }
}
