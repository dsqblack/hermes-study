package com.example.stock.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 服务器指标数据传输对象
 * 映射 Supabase server_metrics 表的字段（蛇形→驼峰）
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetricDTO {
    private Long id;

    @JsonProperty("cpu_usage")
    private Double cpuUsage;

    @JsonProperty("memory_usage")
    private Double memoryUsage;

    @JsonProperty("memory_total")
    private Long memoryTotal;

    @JsonProperty("memory_used")
    private Long memoryUsed;

    @JsonProperty("disk_usage")
    private Double diskUsage;

    @JsonProperty("disk_total")
    private Long diskTotal;

    @JsonProperty("disk_used")
    private Long diskUsed;

    @JsonProperty("load_average")
    private Double loadAverage;

    @JsonProperty("created_at")
    private String createdAt;

    /** 设置当前时间为 ISO 字符串 */
    public void setCreatedAtNow() {
        this.createdAt = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    // getters / setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(Double cpuUsage) { this.cpuUsage = cpuUsage; }

    public Double getMemoryUsage() { return memoryUsage; }
    public void setMemoryUsage(Double memoryUsage) { this.memoryUsage = memoryUsage; }

    public Long getMemoryTotal() { return memoryTotal; }
    public void setMemoryTotal(Long memoryTotal) { this.memoryTotal = memoryTotal; }

    public Long getMemoryUsed() { return memoryUsed; }
    public void setMemoryUsed(Long memoryUsed) { this.memoryUsed = memoryUsed; }

    public Double getDiskUsage() { return diskUsage; }
    public void setDiskUsage(Double diskUsage) { this.diskUsage = diskUsage; }

    public Long getDiskTotal() { return diskTotal; }
    public void setDiskTotal(Long diskTotal) { this.diskTotal = diskTotal; }

    public Long getDiskUsed() { return diskUsed; }
    public void setDiskUsed(Long diskUsed) { this.diskUsed = diskUsed; }

    public Double getLoadAverage() { return loadAverage; }
    public void setLoadAverage(Double loadAverage) { this.loadAverage = loadAverage; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
