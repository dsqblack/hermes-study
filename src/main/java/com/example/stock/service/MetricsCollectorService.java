package com.example.stock.service;

import com.example.stock.config.EcsConfig;
import com.example.stock.dto.MetricDTO;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 服务器指标采集服务
 * 定时通过 SSH 连接到阿里云 ECS，采集 CPU/内存/磁盘/负载指标，写入 Supabase
 */
@Service
@EnableScheduling
public class MetricsCollectorService {

    private static final Logger log = LoggerFactory.getLogger(MetricsCollectorService.class);

    private final EcsConfig ecsConfig;
    private final SupabaseClient supabase;
    private final MetricsStore localStore;

    public MetricsCollectorService(EcsConfig ecsConfig, SupabaseClient supabase, MetricsStore localStore) {
        this.ecsConfig = ecsConfig;
        this.supabase = supabase;
        this.localStore = localStore;
    }

    @PostConstruct
    public void init() {
        log.info("MetricsCollectorService initialized, target: {}@{}",
                ecsConfig.getUsername(), ecsConfig.getHost());
    }

    /**
     * 每 30 秒采集一次
     */
    @Scheduled(fixedRate = 30_000)
    public void collect() {
        try {
            MetricDTO dto = collectMetrics();
            // 写入本地缓存（同步，必定成功）
            localStore.add(dto);
            // 写入 Supabase（异步，失败不影响前端展示）
            supabase.insertMetric(dto);
            log.debug("Metrics collected: CPU={}%, Mem={}%, Disk={}%",
                    dto.getCpuUsage(), dto.getMemoryUsage(), dto.getDiskUsage());
        } catch (Exception e) {
            log.error("Metrics collection failed", e);
        }
    }

    /**
     * 每天凌晨 3:00 清理超过 7 天的数据
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanup() {
        log.info("Starting daily data cleanup...");
        localStore.cleanupOldData();
        supabase.cleanupOldData();
    }

    /**
     * SSH 到 ECS 执行采集命令，返回解析后的指标
     */
    public MetricDTO collectMetrics() throws Exception {
        String shellOutput = sshExec(buildCommand());

        MetricDTO dto = new MetricDTO();
        dto.setCreatedAtNow();

        // 按标记分段解析，避免跨段干扰
        String[] sections = shellOutput.split("---[A-Z]+---");

        // sections[0] = top -bn1 | head -5 输出
        if (sections.length > 0) {
            parseCpu(sections[0], dto);
        }
        // sections[1] = free -k 输出
        if (sections.length > 1) {
            parseMemory(sections[1], dto);
        }
        // sections[2] = df -h / 输出
        if (sections.length > 2) {
            parseDisk(sections[2], dto);
        }
        // sections[3] = cat /proc/loadavg 输出
        if (sections.length > 3) {
            parseLoad(sections[3], dto);
        }

        return dto;
    }

    private void parseCpu(String text, MetricDTO dto) {
        // top -bn1 输出: "%Cpu(s):  5.2 us,  2.1 sy,  0.0 ni, 92.7 id, ..."
        Pattern p = Pattern.compile("%Cpu\\(s\\):.*?([\\d.]+)\\s+id", Pattern.DOTALL);
        Matcher m = p.matcher(text);
        if (m.find()) {
            double idle = Double.parseDouble(m.group(1));
            dto.setCpuUsage(Math.round((100.0 - idle) * 100.0) / 100.0);
        }
    }

    private void parseMemory(String text, MetricDTO dto) {
        // free -k: "Mem:        1883508    851234    345678 ..."
        Pattern p = Pattern.compile("Mem:\\s+(\\d+)\\s+(\\d+)\\s+", Pattern.DOTALL);
        Matcher m = p.matcher(text);
        if (m.find()) {
            long totalKb = Long.parseLong(m.group(1));
            long usedKb = Long.parseLong(m.group(2));
            dto.setMemoryTotal(totalKb);
            dto.setMemoryUsed(usedKb);
            dto.setMemoryUsage(Math.round((double) usedKb / totalKb * 10000.0) / 100.0);
        }
    }

    private void parseDisk(String text, MetricDTO dto) {
        // df -h /: "/dev/vda1        40G   14G   27G  35% /"
        Pattern p = Pattern.compile("(/dev/\\S+)\\s+([\\d.]+[A-Za-z])\\s+([\\d.]+[A-Za-z])\\s+[\\d.]+[A-Za-z]\\s+([\\d]+)%");
        Matcher m = p.matcher(text);
        if (m.find()) {
            dto.setDiskUsage(Double.parseDouble(m.group(4)));
            dto.setDiskTotal(parseSizeToKb(m.group(2)));
            dto.setDiskUsed(parseSizeToKb(m.group(3)));
        }
    }

    private void parseLoad(String text, MetricDTO dto) {
        // /proc/loadavg: "0.85 0.62 0.45 1/234 56789"
        text = text.trim();
        String first = text.split("\\s+")[0];
        try {
            dto.setLoadAverage(Double.parseDouble(first));
        } catch (NumberFormatException e) {
            log.warn("parseLoad failed: {}", text);
        }
    }

    /** 构建 SSH 远程采集命令 */
    private String buildCommand() {
        return "top -bn1 | head -5 && echo '---MEM---' && free -k && echo '---DSK---' && df -h / && echo '---LOAD---' && cat /proc/loadavg";
    }

    /** 执行 SSH 命令并返回输出文本 */
    private String sshExec(String command) throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(ecsConfig.getUsername(), ecsConfig.getHost(), ecsConfig.getPort());
        session.setPassword(ecsConfig.getPassword());

        // 跳过 host key 检查（内网/可控环境）
        java.util.Properties props = new java.util.Properties();
        props.put("StrictHostKeyChecking", "no");
        session.setConfig(props);
        session.connect(10_000);

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        channel.setOutputStream(out);
        channel.setErrStream(err);
        channel.connect(10_000);

        // 等待命令完成
        while (!channel.isClosed()) {
            Thread.sleep(100);
        }
        channel.disconnect();
        session.disconnect();

        String output = out.toString("UTF-8");
        if (err.size() > 0) {
            log.warn("SSH stderr: {}", err.toString("UTF-8"));
        }
        return output;
    }

    /** 将 '14G', '1.2G', '512M' 等格式转成 KB */
    private long parseSizeToKb(String size) {
        try {
            size = size.trim().toUpperCase();
            if (size.endsWith("G")) {
                return (long) (Double.parseDouble(size.replace("G", "")) * 1024 * 1024);
            } else if (size.endsWith("M")) {
                return (long) (Double.parseDouble(size.replace("M", "")) * 1024);
            } else if (size.endsWith("K")) {
                return (long) Double.parseDouble(size.replace("K", ""));
            } else if (size.endsWith("T")) {
                return (long) (Double.parseDouble(size.replace("T", "")) * 1024 * 1024 * 1024);
            }
            return Long.parseLong(size);
        } catch (NumberFormatException e) {
            log.warn("parseSizeToKb failed: {}", size);
            return 0;
        }
    }
}
