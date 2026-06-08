package com.example.stock.service;

import com.example.stock.config.SupabaseConfig;
import com.example.stock.dto.MetricDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Supabase REST API 客户端
 * 使用 Spring RestTemplate 替代 java.net.http.HttpClient（更稳定的 SSL 处理）
 */
@Component
public class SupabaseClient {

    private static final Logger log = LoggerFactory.getLogger(SupabaseClient.class);

    private final SupabaseConfig config;
    private final RestTemplate rest;
    private final ObjectMapper om;

    public SupabaseClient(SupabaseConfig config) {
        this.config = config;
        this.rest = new RestTemplate();
        this.om = new ObjectMapper();
    }

    /** 插入一条指标记录 */
    public void insertMetric(MetricDTO dto) {
        try {
            String json = om.writeValueAsString(dto);
            String url = config.getUrl() + "/rest/v1/server_metrics";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("apikey", config.getServiceRoleKey());
            headers.add("Authorization", "Bearer " + config.getServiceRoleKey());
            headers.add("Prefer", "return=minimal");

            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            ResponseEntity<String> resp = rest.exchange(url, HttpMethod.POST, entity, String.class);

            if (resp.getStatusCode().isError()) {
                log.warn("Supabase insert failed: {} for data: {}", resp.getStatusCode(), json);
            } else {
                log.debug("Supabase insert OK: status={}, data={}", resp.getStatusCode(), json);
            }
        } catch (Exception e) {
            log.error("Supabase insert error", e);
        }
    }

    /** 查询最新的一条指标 */
    public MetricDTO queryLatest() {
        try {
            String url = config.getUrl() + "/rest/v1/server_metrics"
                    + "?select=*&order=created_at.desc.nullslast&limit=1";

            List<MetricDTO> list = doQuery(url);
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            log.error("Supabase queryLatest error", e);
            return null;
        }
    }

    /** 查询最近 N 小时的指标历史 */
    public List<MetricDTO> queryHistory(int hours) {
        try {
            ZonedDateTime since = ZonedDateTime.now(java.time.ZoneOffset.UTC).minusHours(hours);
            String sinceStr = since.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));

            String url = config.getUrl() + "/rest/v1/server_metrics"
                    + "?select=*"
                    + "&created_at=gte." + sinceStr
                    + "&order=created_at.asc";

            return doQuery(url);
        } catch (Exception e) {
            log.error("Supabase queryHistory error", e);
            return Collections.emptyList();
        }
    }

    /**
     * 清理 7 天前的过期数据
     * 每天由定时任务调用一次
     */
    public void cleanupOldData() {
        try {
            ZonedDateTime cutoff = ZonedDateTime.now(ZoneOffset.UTC).minusDays(7);
            String cutoffStr = cutoff.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));

            String url = config.getUrl() + "/rest/v1/server_metrics"
                    + "?created_at=lt." + cutoffStr;

            HttpHeaders headers = new HttpHeaders();
            headers.add("apikey", config.getServiceRoleKey());
            headers.add("Authorization", "Bearer " + config.getServiceRoleKey());
            headers.add("Prefer", "return=minimal");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> resp = rest.exchange(url, HttpMethod.DELETE, entity, String.class);

            log.info("Supabase cleanup: deleted records older than {} days, status={}", 7, resp.getStatusCode());
        } catch (Exception e) {
            log.error("Supabase cleanup error", e);
        }
    }

    /** 通用 GET 查询 */
    private List<MetricDTO> doQuery(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("apikey", config.getServiceRoleKey());
            headers.add("Authorization", "Bearer " + config.getServiceRoleKey());
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> resp = rest.exchange(url, HttpMethod.GET, entity, String.class);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                log.warn("Supabase query failed: {}", resp.getStatusCode());
                return Collections.emptyList();
            }
            return om.readValue(resp.getBody(), new TypeReference<List<MetricDTO>>() {});
        } catch (Exception e) {
            log.error("Supabase doQuery error: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
