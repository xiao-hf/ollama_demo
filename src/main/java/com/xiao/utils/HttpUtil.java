package com.xiao.utils;

import jakarta.annotation.Resource;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * HTTP请求工具类
 */
@Slf4j
@Component
public class HttpUtil {
    @Resource
    CloseableHttpClient httpClient;

    /**
     * 发送GET请求
     *
     * @param url         请求URL
     * @param headers     请求头(可为null)
     * @param params      请求参数(可为null)
     * @return            响应内容字符串
     */
    public String doGet(String url, Map<String, String> headers, Map<String, String> params) {
        try {
            // 构建带参数的URI
            URIBuilder builder = new URIBuilder(url);
            if (params != null && !params.isEmpty()) {
                for (Map.Entry<String, String> param : params.entrySet()) {
                    builder.addParameter(param.getKey(), param.getValue());
                }
            }
            URI uri = builder.build();

            // 创建HttpGet请求
            HttpGet httpGet = new HttpGet(uri);

            // 添加请求头
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    httpGet.addHeader(header.getKey(), header.getValue());
                }
            }

            // 执行请求并获取响应
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();

            // 转换响应内容为字符串并返回
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, "UTF-8");
            }

            // 关闭资源
            httpClient.close();

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 发送POST请求
     *
     * @param url 请求URL
     * @param headers 请求头
     * @param requestBody 请求体
     * @return 响应字符串
     */
    public String doPost(String url, Map<String, String> headers, String requestBody) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            
            // 设置请求头
            if (headers != null && !headers.isEmpty()) {
                headers.forEach(httpPost::setHeader);
            }
            
            // 设置请求体
            if (requestBody != null) {
                StringEntity entity = new StringEntity(requestBody, StandardCharsets.UTF_8);
                httpPost.setEntity(entity);
            }
            
            // 执行请求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                // 获取响应体
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                
                // 检查响应状态
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    return responseBody;
                } else {
                    log.error("HTTP请求失败, URL: {}, 状态码: {}, 响应: {}", url, statusCode, responseBody);
                    throw new RuntimeException("HTTP请求失败: " + statusCode);
                }
            }
        } catch (Exception e) {
            log.error("HTTP请求异常, URL: {}", url, e);
            throw new RuntimeException("HTTP请求异常: " + e.getMessage(), e);
        }
    }

    /**
     * 发送PUT请求，请求体为JSON字符串，内部处理异常不向外抛出
     *
     * @param url         请求URL
     * @param headers     请求头(可为null)
     * @param jsonBody    JSON格式的请求体
     * @return            响应内容字符串，出错时返回null
     */
    public String doPut(String url, Map<String, String> headers, String jsonBody) {
        String result = null;
        try {
            // 创建HttpPut请求
            HttpPut httpPut = new HttpPut(url);
            // 设置JSON请求体
            if (jsonBody != null && !jsonBody.isEmpty()) {
                StringEntity entity = new StringEntity(jsonBody, "UTF-8");
                entity.setContentType("application/json");
                httpPut.setEntity(entity);
            }
            // 添加请求头
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    httpPut.addHeader(header.getKey(), header.getValue());
                }
            }
            // 如果没有明确设置Content-Type头，则默认设置为application/json
            if (headers == null || !headers.containsKey("Content-Type")) {
                httpPut.addHeader("Content-Type", "application/json");
            }
            // 执行请求并获取响应
            HttpResponse response = httpClient.execute(httpPut);
            HttpEntity entity = response.getEntity();
            // 转换响应内容为字符串
            if (entity != null) {
                result = EntityUtils.toString(entity, "UTF-8");
            }
        } catch (Exception e) {
            // 记录异常但不抛出
            e.printStackTrace();
            // 或者使用日志框架记录
            // logger.error("PUT请求发生异常: " + e.getMessage(), e);
        } finally {
            // 确保关闭资源
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    // 或者使用日志框架记录
                    // logger.error("关闭HttpClient时发生异常: " + e.getMessage(), e);
                }
            }
        }

        return result;
    }

    /**
     * 发送DELETE请求，内部处理异常不向外抛出
     *
     * @param url         请求URL
     * @param headers     请求头(可为null)
     * @return            响应内容字符串，出错时返回null
     */
    public String doDelete(String url, Map<String, String> headers) {
        String result = null;
        try {
            // 创建HttpClient实例
            httpClient = HttpClients.createDefault();
            // 创建HttpDelete请求
            HttpDelete httpDelete = new HttpDelete(url);
            // 添加请求头
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    httpDelete.addHeader(header.getKey(), header.getValue());
                }
            }
            // 执行请求并获取响应
            HttpResponse response = httpClient.execute(httpDelete);
            HttpEntity entity = response.getEntity();
            // 转换响应内容为字符串
            if (entity != null) {
                result = EntityUtils.toString(entity, "UTF-8");
            }
        } catch (Exception e) {
            // 记录异常但不抛出
            e.printStackTrace();
            // 或者使用日志框架记录
            // logger.error("DELETE请求发生异常: " + e.getMessage(), e);
        } finally {
            // 确保关闭资源
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    // 或者使用日志框架记录
                    // logger.error("关闭HttpClient时发生异常: " + e.getMessage(), e);
                }
            }
        }
        return result;
    }
}