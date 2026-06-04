package com.example.stock.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;

@Controller
public class ToolsController {

    @GetMapping("/tools")
    public String toolsPage() {
        return "forward:/tools.html";
    }

    /**
     * HTTP 请求代理 - 用于前端 HTTP 调试工具
     * 解决浏览器 CORS 限制
     */
    @PostMapping("/api/tools/http-proxy")
    @ResponseBody
    public Map<String, Object> httpProxy(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new LinkedHashMap<>();
        String urlStr = (String) params.getOrDefault("url", "");
        String method = ((String) params.getOrDefault("method", "GET")).toUpperCase();
        @SuppressWarnings("unchecked")
        Map<String, String> headers = (Map<String, String>) params.getOrDefault("headers", new HashMap<>());
        String body = (String) params.getOrDefault("body", "");

        result.put("success", false);
        result.put("status", 0);
        result.put("statusText", "");
        result.put("responseHeaders", new HashMap<>());
        result.put("body", "");
        result.put("timing", 0);

        if (urlStr.isEmpty()) {
            result.put("error", "URL is required");
            return result;
        }

        long startTime = System.currentTimeMillis();
        HttpURLConnection conn = null;
        try {
            URI uri = new URI(urlStr);
            URL url = uri.toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);

            // Set request headers
            if (headers != null) {
                for (Map.Entry<String, String> h : headers.entrySet()) {
                    if (h.getKey() != null && !h.getKey().isEmpty()) {
                        conn.setRequestProperty(h.getKey(), h.getValue());
                    }
                }
            }

            // Set body for POST/PUT/PATCH
            if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH")) {
                conn.setDoOutput(true);
                if (body != null && !body.isEmpty()) {
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(body.getBytes("UTF-8"));
                        os.flush();
                    }
                }
            }

            // Get response
            int statusCode = conn.getResponseCode();
            result.put("status", statusCode);
            result.put("statusText", getStatusText(statusCode));

            // Read response headers
            Map<String, String> responseHeaders = new LinkedHashMap<>();
            for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
                String key = entry.getKey();
                if (key != null && !entry.getValue().isEmpty()) {
                    responseHeaders.put(key, String.join(", ", entry.getValue()));
                }
            }
            result.put("responseHeaders", responseHeaders);

            // Read response body
            StringBuilder responseBody = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream(),
                            "UTF-8"
                    ))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line).append("\n");
                }
            }
            result.put("body", responseBody.toString().trim());

            long elapsed = System.currentTimeMillis() - startTime;
            result.put("timing", elapsed);
            result.put("success", true);

        } catch (Exception e) {
            result.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            long elapsed = System.currentTimeMillis() - startTime;
            result.put("timing", elapsed);
        } finally {
            if (conn != null) conn.disconnect();
        }

        return result;
    }

    private String getStatusText(int code) {
        switch (code) {
            case 200: return "OK";
            case 201: return "Created";
            case 204: return "No Content";
            case 301: return "Moved Permanently";
            case 302: return "Found";
            case 304: return "Not Modified";
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 408: return "Request Timeout";
            case 500: return "Internal Server Error";
            case 502: return "Bad Gateway";
            case 503: return "Service Unavailable";
            default: return "Unknown";
        }
    }
}
