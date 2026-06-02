package com.example.stock.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
public class StockService {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper om = new ObjectMapper();

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

    // ---- News ----
    private List<Map<String, Object>> fetchNews() throws Exception {
        String url = "https://search-api-web.eastmoney.com/search/jsonp"
                + "?param=%7B%22uid%22%3A%22%22%2C%22keyword%22%3A%22%E8%B4%A2%E7%BB%8F%22%2C%22type%22%3A%5B%22cmsArticleWebOld%22%5D%2C%22client%22%3A%22web%22%2C%22clientType%22%3A%22web%22%2C%22clientVersion%22%3A%22curr%22%2C%22param%22%3A%7B%22cmsArticleWebOld%22%3A%7B%22searchScope%22%3A%22default%22%2C%22sort%22%3A%22default%22%2C%22pageIndex%22%3A1%2C%22pageSize%22%3A10%2C%22preTag%22%3A%22%22%2C%22postTag%22%3A%22%22%7D%7D%7D";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("User-Agent", "Mozilla/5.0")
                .header("Referer", "https://www.eastmoney.com/")
                .GET().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        String body = resp.body();

        // Strip JSONP padding: jQuery({...})
        int start = body.indexOf('(');
        int end = body.lastIndexOf(')');
        if (start >= 0 && end > start) {
            body = body.substring(start + 1, end);
        }

        JsonNode root = om.readTree(body);
        JsonNode articles = root.at("/result/cmsArticleWebOld");
        List<Map<String, Object>> list = new ArrayList<>();
        if (articles != null && articles.isArray()) {
            for (JsonNode item : articles) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("title", getText(item, "title"));
                m.put("source", getText(item, "mediaName"));
                m.put("time", getText(item, "date"));
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
