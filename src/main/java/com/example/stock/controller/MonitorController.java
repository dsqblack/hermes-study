package com.example.stock.controller;

import com.example.stock.dto.ApiResult;
import com.example.stock.dto.MetricDTO;
import com.example.stock.service.MetricsCollectorService;
import com.example.stock.service.MetricsStore;
import com.example.stock.service.SupabaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 服务器监控控制器
 * /monitor — 监控页面
 * /api/monitor/* — REST 数据接口
 */
@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    private static final Logger log = LoggerFactory.getLogger(MonitorController.class);

    @Autowired
    private SupabaseClient supabase;

    @Autowired
    private MetricsStore metricsStore;

    @Autowired
    private MetricsCollectorService collector;

    /**
     * 获取最新一条指标
     */
    @GetMapping("/current")
    public ApiResult<MetricDTO> current() {
        MetricDTO latest = metricsStore.getLatest();
        if (latest == null) {
            // fallback: 从 Supabase 查
            latest = supabase.queryLatest();
        }
        if (latest == null) {
            return ApiResult.error("暂无数据");
        }
        return ApiResult.success(latest);
    }

    /**
     * 获取历史趋势数据
     * @param hours 最近几小时，默认1小时
     */
    @GetMapping("/history")
    public ApiResult<List<MetricDTO>> history(@RequestParam(defaultValue = "1") int hours) {
        // 优先从 Supabase 读（有完整历史），失败再 fallback 本地缓存
        List<MetricDTO> list = supabase.queryHistory(hours);
        if (list.isEmpty()) {
            list = metricsStore.getHistory(hours);
        }
        return ApiResult.success(list);
    }

    /**
     * 手动触发一次采集（调试用）
     */
    @PostMapping("/collect")
    public ApiResult<MetricDTO> collect() {
        try {
            MetricDTO dto = collector.collectMetrics();
            log.info("Collected: CPU={}, Mem={}, Disk={}, Load={}",
                    dto.getCpuUsage(), dto.getMemoryUsage(), dto.getDiskUsage(), dto.getLoadAverage());
            metricsStore.add(dto);
            supabase.insertMetric(dto);
            return ApiResult.success(dto);
        } catch (Exception e) {
            log.error("Collect failed", e);
            return ApiResult.error("采集失败: " + e.getMessage());
        }
    }

    /**
     * 手动清理 7 天前的数据
     */
    @PostMapping("/cleanup")
    public ApiResult<String> cleanup() {
        try {
            log.info("Manual cleanup triggered");
            metricsStore.cleanupOldData();
            supabase.cleanupOldData();
            return ApiResult.success("清理完成，已删除 7 天前的过期数据");
        } catch (Exception e) {
            log.error("Cleanup failed", e);
            return ApiResult.error("清理失败: " + e.getMessage());
        }
    }
}
