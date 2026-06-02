package com.example.stock.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
public class StockService {

    private final HttpClient http;
    private final ObjectMapper om = new ObjectMapper();

    public StockService() {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10));

        // 读取环境变量中的代理设置 (WSL/公司网络需要)
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
            } catch (Exception ignored) {
                // 解析失败就不设代理
            }
        }

        this.http = builder.build();
    }

    public Map<String, Object> fetchHotData() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            result.put("indices", fetchIndices());
        } catch (Exception e) {
            result.put("indices", List.of());
        }
        try {
            result.put("fundFlow", fetchFundFlow());
        } catch (Exception e) {
            result.put("fundFlow", Map.of());
        }
        try {
            result.put("sectors", fetchSectors());
        } catch (Exception e) {
            result.put("sectors", List.of());
        }
        try {
            result.put("stocks", fetchHotStocks());
        } catch (Exception e) {
            result.put("stocks", List.of());
        }
        try {
            result.put("news", fetchNews());
        } catch (Exception e) {
            result.put("news", List.of());
        }
        result.put("code", 0);
        return result;
    }

    // ---- Indices ----
    private List<Map<String, Object>> fetchIndices() throws Exception {
        String url = "https://push2.eastmoney.com/api/qt/ulist.np/get?fltt=2"
                + "&fields=f2,f3,f4,f12,f14"
                + "&secids=1.000001,0.399001,0.399006,1.000688,1.000300,1.000016,1.000905";
        JsonNode data = getJsonArray(url);
        List<Map<String, Object>> list = new ArrayList<>();
        if (data == null) return list;
        for (JsonNode item : data) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", getText(item, "f14"));
            m.put("code", getText(item, "f12"));
            m.put("price", getDouble(item, "f2"));
            m.put("change", getDouble(item, "f4"));
            m.put("changePct", getDouble(item, "f3"));
            list.add(m);
        }
        return list;
    }

    // ---- Fund Flow ----
    private Map<String, Object> fetchFundFlow() throws Exception {
        String url = "https://push2.eastmoney.com/api/qt/ulist.np/get?fltt=2"
                + "&fields=f168,f169,f170,f171"
                + "&secids=1.000001";
        JsonNode arr = getJsonArray(url);
        Map<String, Object> m = new LinkedHashMap<>();
        if (arr != null && arr.size() > 0) {
            JsonNode item = arr.get(0);
            m.put("mainForce", getDouble(item, "f168"));
            m.put("mainForcePct", getDouble(item, "f169"));
            m.put("retail", getDouble(item, "f170"));
            m.put("retailPct", getDouble(item, "f171"));
        }
        return m;
    }

    // ---- Sectors ----
    private List<Map<String, Object>> fetchSectors() throws Exception {
        String url = "https://push2.eastmoney.com/api/qt/clist/get?pn=1&pz=10&po=0&np=1"
                + "&fltt=2&invid=0&fid=f3"
                + "&fs=m:90+t:2"
                + "&fields=f12,f14,f2,f3,f4";
        JsonNode arr = getClistArray(url);
        List<Map<String, Object>> list = new ArrayList<>();
        if (arr == null) return list;
        for (JsonNode item : arr) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", getText(item, "f14"));
            m.put("code", getText(item, "f12"));
            m.put("price", getDouble(item, "f2"));
            m.put("change", getDouble(item, "f4"));
            m.put("changePct", getDouble(item, "f3"));
            list.add(m);
        }
        return list;
    }

    // ---- Hot Stocks ----
    private List<Map<String, Object>> fetchHotStocks() throws Exception {
        String url = "https://push2.eastmoney.com/api/qt/clist/get?pn=1&pz=20&po=0&np=1"
                + "&fltt=2&invid=0&fid=f3"
                + "&fs=m:0+t:6+f:!50"
                + "&fields=f12,f14,f2,f3,f4";
        JsonNode arr = getClistArray(url);
        List<Map<String, Object>> list = new ArrayList<>();
        if (arr == null) return list;
        for (JsonNode item : arr) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", getText(item, "f14"));
            m.put("code", getText(item, "f12"));
            m.put("price", getDouble(item, "f2"));
            m.put("change", getDouble(item, "f4"));
            m.put("changePct", getDouble(item, "f3"));
            list.add(m);
        }
        return list;
    }
    // ---- News (from Sina Finance) ----
    private List<Map<String, Object>> fetchNews() throws Exception {
        String url = "https://feed.mix.sina.com.cn/api/roll/get"
                + "?pageid=153&lid=2509&k=&num=10&page=1";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("User-Agent", "Mozilla/5.0")
                .GET().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode root = om.readTree(resp.body());
        JsonNode arts = root.at("/result/data");
        List<Map<String, Object>> list = new ArrayList<>();
        if (arts != null && arts.isArray()) {
            for (JsonNode item : arts) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("title", getText(item, "title"));
                m.put("source", getText(item, "media_name"));
                // ctime is unix timestamp in seconds
                long ctime = (long) getDouble(item, "ctime");
                if (ctime > 0) {
                    m.put("time", new java.text.SimpleDateFormat("MM-dd HH:mm")
                            .format(new java.util.Date(ctime * 1000)));
                } else {
                    m.put("time", "");
                }
                m.put("url", getText(item, "url"));
                list.add(m);
            }
        }
        return list;
    }

    // ---- Helpers ----
    private JsonNode getJsonArray(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("User-Agent", "Mozilla/5.0")
                .GET().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode root = om.readTree(resp.body());
        JsonNode data = root.get("data");
        if (data == null) return null;
        JsonNode diff = data.get("diff");
        return (diff != null && diff.isArray()) ? diff : null;
    }

    private JsonNode getClistArray(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("User-Agent", "Mozilla/5.0")
                .GET().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode root = om.readTree(resp.body());
        JsonNode data = root.get("data");
        if (data == null) return null;
        JsonNode diff = data.get("diff");
        return (diff != null && diff.isArray()) ? diff : null;
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
