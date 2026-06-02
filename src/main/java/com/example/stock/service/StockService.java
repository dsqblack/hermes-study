package com.example.stock.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private static final Charset GBK = Charset.forName("GBK");

    private final HttpClient http;
    private final ObjectMapper om = new ObjectMapper();

    public StockService() {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10));

        String httpsProxy = System.getenv("https_proxy");
        if (httpsProxy == null || httpsProxy.isEmpty()) {
            httpsProxy = System.getenv("HTTPS_PROXY");
        }
        if (httpsProxy != null && !httpsProxy.isEmpty()) {
            try {
                URI proxyUri = URI.create(httpsProxy);
                String host = proxyUri.getHost();
                int port = proxyUri.getPort();
                if (port <= 0) port = 7890;
                if (host != null) {
                    builder.proxy(ProxySelector.of(new InetSocketAddress(host, port)));
                }
            } catch (Exception e) {
                log.warn("Failed to parse proxy {}, skipping", httpsProxy, e);
            }
        }

        this.http = builder.build();
    }

    public Map<String, Object> fetchHotData() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            result.put("indices", fetchIndices());
        } catch (Exception e) {
            log.error("fetchIndices failed", e);
            result.put("indices", List.of());
        }
        // 资金流向 — 免费接口已基本失效，返回空占位
        result.put("fundFlow", Map.of());
        try {
            result.put("sectors", fetchSectors());
        } catch (Exception e) {
            log.error("fetchSectors failed", e);
            result.put("sectors", List.of());
        }
        try {
            result.put("stocks", fetchHotStocks());
        } catch (Exception e) {
            log.error("fetchHotStocks failed", e);
            result.put("stocks", List.of());
        }
        try {
            result.put("news", fetchNews());
        } catch (Exception e) {
            log.error("fetchNews failed", e);
            result.put("news", List.of());
        }
        result.put("code", 0);
        return result;
    }

    // ============ Indices (Tencent qt.gtimg.cn, GBK charset) ============
    private List<Map<String, Object>> fetchIndices() throws Exception {
        String url = "https://qt.gtimg.cn/q=sh000001,sz399001,sz399006,sh000688,sh000300,sh000016,sh000905";
        String resp = httpGetGBK(url);
        List<Map<String, Object>> list = new ArrayList<>();
        for (String line : resp.split("\n")) {
            line = line.trim();
            if (!line.startsWith("v_")) continue;
            int q1 = line.indexOf('"');
            int q2 = line.lastIndexOf('"');
            if (q1 < 0 || q2 <= q1) continue;
            String raw = line.substring(q1 + 1, q2);
            String[] f = raw.split("~");
            if (f.length < 33) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", safeStr(f, 1));
            m.put("code", safeStr(f, 2));
            m.put("price", parseDouble(safeStr(f, 3)));
            m.put("change", parseDouble(safeStr(f, 31)));
            m.put("changePct", parseDouble(safeStr(f, 32)));
            list.add(m);
        }
        return list;
    }

    // ============ Sectors (Sina) ============
    private List<Map<String, Object>> fetchSectors() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        // "hy" = 行业板块 (industry), sort by changepercent descending
        String url = "https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData"
                + "?page=1&num=15&sort=changepercent&asc=0&node=hs_s";
        String resp = httpGet(url);
        JsonNode arr = om.readTree(resp);
        if (arr != null && arr.isArray()) {
            for (JsonNode item : arr) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name", getText(item, "name"));
                m.put("code", getText(item, "code"));
                m.put("price", getDouble(item, "trade"));
                m.put("change", getDouble(item, "pricechange"));
                m.put("changePct", getDouble(item, "changepercent"));
                list.add(m);
            }
        } else {
            log.warn("fetchSectors: unexpected response type: {}", arr);
        }
        return list;
    }

    // ============ Hot Stocks (Sina Market_Center) ============
    private List<Map<String, Object>> fetchHotStocks() throws Exception {
        String url = "https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData"
                + "?page=1&num=30&sort=changepercent&asc=0&node=hs_a&symbol=&_=1";
        String resp = httpGet(url);
        JsonNode arr = om.readTree(resp);
        List<Map<String, Object>> list = new ArrayList<>();
        if (arr != null && arr.isArray()) {
            for (JsonNode item : arr) {
                String code = getText(item, "code");
                // 过滤北交所股票 (92/8开头)
                if (code.startsWith("92") || code.startsWith("8")) continue;
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name", getText(item, "name"));
                m.put("code", code);
                m.put("price", getDouble(item, "trade"));
                m.put("change", getDouble(item, "pricechange"));
                m.put("changePct", getDouble(item, "changepercent"));
                list.add(m);
                if (list.size() >= 15) break;
            }
        } else {
            log.warn("fetchHotStocks: unexpected response type: {}", arr);
        }
        return list;
    }

    // ============ News (scraped from Sina Finance homepage) ============
    private static final Pattern NEWS_PATTERN = Pattern.compile(
            "target=\"_blank\" href=\"(https?://finance\\.sina\\.com\\.cn/[^\"]+)\">([^<]{8,})");

    private List<Map<String, Object>> fetchNews() throws Exception {
        // Sina feed API (feed.mix.sina.com.cn) is blocked on some servers,
        // fallback to scraping finance.sina.com.cn homepage
        String url = "https://finance.sina.com.cn/";
        String html = httpGet(url);
        Matcher m = NEWS_PATTERN.matcher(html);
        List<Map<String, Object>> list = new ArrayList<>();
        Set<String> seenUrls = new HashSet<>();
        while (m.find() && list.size() < 10) {
            String newsUrl = m.group(1);
            String title = m.group(2).trim();
            if (title.isEmpty() || seenUrls.contains(newsUrl)) continue;
            seenUrls.add(newsUrl);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("title", title);
            item.put("source", "新浪财经");
            item.put("time", new SimpleDateFormat("MM-dd HH:mm").format(new Date()));
            item.put("url", newsUrl);
            list.add(item);
        }
        if (list.isEmpty()) {
            log.warn("fetchNews: no news items found on Sina finance page");
        }
        return list;
    }

    // ============ HTTP Helpers ============

    /** 通用HTTP GET，默认UTF-8解码（适用于新浪JSON接口） */
    private String httpGet(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .GET().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }

    /** 专门用于腾讯 qt.gtimg.cn 接口（GBK编码） */
    private String httpGetGBK(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .GET().build();
        HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
        return new String(resp.body(), GBK);
    }

    private String safeStr(String[] arr, int idx) {
        return idx < arr.length ? (arr[idx] == null ? "" : arr[idx]) : "";
    }

    private double parseDouble(String s) {
        if (s == null || s.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String getText(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? "" : v.asText();
    }

    private double getDouble(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? 0.0 : v.asDouble();
    }
}
