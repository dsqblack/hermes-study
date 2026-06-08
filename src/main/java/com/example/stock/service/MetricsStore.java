package com.example.stock.service;

import com.example.stock.dto.MetricDTO;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 本地内存缓存，存储最近 7 天的指标数据
 * 作为 Supabase 的前置缓存，即使 Supabase 不可用也能正常展示趋势
 */
@Component
public class MetricsStore {

    private static final Logger log = LoggerFactory.getLogger(MetricsStore.class);

    /** 最多缓存 7 天 × 24h × 120 条/h（30秒采样间隔） */
    private static final int MAX_SIZE = 7 * 24 * 120;

    private final List<MetricDTO> buffer = Collections.synchronizedList(new ArrayList<>());

    @PostConstruct
    public void init() {
        log.info("MetricsStore initialized, max capacity={}", MAX_SIZE);
    }

    /** 添加一条记录，超过容量自动淘汰最旧的 */
    public void add(MetricDTO dto) {
        if (dto.getCreatedAt() == null) {
            dto.setCreatedAtNow();
        }
        buffer.add(dto);
        if (buffer.size() > MAX_SIZE) {
            // 批量移除最旧的 10%
            int toRemove = buffer.size() - MAX_SIZE;
            synchronized (buffer) {
                for (int i = 0; i < toRemove && !buffer.isEmpty(); i++) {
                    buffer.remove(0);
                }
            }
        }
    }

    /** 获取最新一条记录 */
    public MetricDTO getLatest() {
        if (buffer.isEmpty()) return null;
        return buffer.get(buffer.size() - 1);
    }

    /** 获取最近 N 小时的历史记录 */
    public List<MetricDTO> getHistory(int hours) {
        if (buffer.isEmpty()) return List.of();
        ZonedDateTime cutoff = ZonedDateTime.now().minusHours(hours);
        synchronized (buffer) {
            return buffer.stream()
                    .filter(m -> {
                        try {
                            ZonedDateTime t = ZonedDateTime.parse(m.getCreatedAt(),
                                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                            return t.isAfter(cutoff);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    /** 当前缓存数量 */
    public int size() {
        return buffer.size();
    }
}
