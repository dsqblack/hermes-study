# Hermes Study

> 个人学习实验项目，集成 A 股行情、备忘录管理、桌面音乐播放器和开发者工具箱四大模块。

## 技术栈

| 层级       | 技术                                   |
| ---------- | -------------------------------------- |
| 后端框架   | Spring Boot 3.2                        |
| 持久层     | MyBatis + MySQL 8.0                    |
| JDK        | Java 17                                |
| 前端       | 原生 HTML / CSS / JavaScript（无框架） |
| 构建工具   | Maven                                  |
| 音乐播放器 | Python (PyQt5 + pygame)                |
| 服务端口   | 8081                                   |

## 项目结构

```
hermes-study/
├── src/main/java/com/example/stock/
│   ├── StockApplication.java          # 启动入口
│   ├── config/
│   │   └── WebConfig.java             # Web MVC 配置
│   ├── controller/
│   │   ├── StockController.java       # 股市行情 API
│   │   ├── MemoController.java        # 备忘录 CRUD API
│   │   ├── MusicPlayerController.java # 音乐播放器页面
│   │   └── ToolsController.java       # 开发者工具箱页面
│   ├── service/
│   │   ├── StockService.java          # 股票数据采集与处理
│   │   └── MemoService.java           # 备忘录业务逻辑
│   ├── mapper/
│   │   └── MemoMapper.java            # MyBatis Mapper 接口
│   ├── entity/                        # 数据实体（Memo, MemoCategory, MemoTag 等）
│   └── dto/                           # 数据传输对象（ApiResult, MemoDetailDTO）
├── src/main/resources/
│   ├── application.yml                # 应用配置（端口、数据库连接）
│   ├── mapper/MemoMapper.xml          # MyBatis SQL 映射
│   └── static/                        # 前端页面
│       ├── index.html                 # 首页门户
│       ├── stock.html                 # 股市情报站
│       ├── memo.html                  # Memo Board
│       ├── music-player.html          # 音乐播放器 Web 版
│       └── tools.html                 # 开发者工具箱
├── tools/                             # 工具箱设计与文档
│   ├── tools.md                       # 工具功能规格清单
│   └── UI设计方案.md                   # 工具箱 UI 设计方案
├── music-player/                      # 桌面音乐播放器
│   ├── Music-player.exe               # 可执行文件（PyQt5 打包）
│   ├── README.md                       # 播放器详细文档
│   └── music-plaer-design/            # 播放器设计稿
├── downloads/                         # 下载资源目录
└── pom.xml                            # Maven 项目配置
```

## 功能模块

### 1. 股市情报站 — `/stock`

A 股实时行情聚合展示，接入腾讯/新浪数据源。

- 指数行情（上证指数、深证成指）
- 个股详情与涨跌数据
- 板块行情与资金流向
- 热搜股票排行
- 新闻资讯聚合

**API 端点**: `GET /api/stock/hot`

### 2. Memo Board — `/memo`

个人备忘录管理系统，支持分类和标签体系。

- 备忘录 CRUD（创建、编辑、删除、查询）
- 分类管理（自定义分类）
- 标签系统（多标签关联）
- 作者标识
- 前端采用 Linear / Notion 极简风格

**API 端点**: `GET/POST/PUT/DELETE /api/memo/*`

### 3. Cyber Music Player — `/music-player`

桌面音乐播放器（独立 Python 应用），Web 版提供介绍页。

- Spotify 深色风格界面
- 7 种音频格式支持（OGG / MP3 / FLAC / MFLAC / WAV / M4A / WMA）
- MFLAC 自动解密（QQ音乐加密格式）
- 智能推荐引擎（基于标签匹配 + 新鲜度 + 疲劳衰减）
- 粒子动画封面（随音乐律动）
- 窗口缩放自适应

详见 [music-player/README.md](music-player/README.md)

### 4. 开发者工具箱 — `/tools`

17 款纯前端开发效率工具，零后端依赖，单 HTML 文件实现。

| 分类     | 工具                                                         |
| -------- | ------------------------------------------------------------ |
| 开发调试 | JSON/XML/YAML 格式化互转、正则表达式测试器、SQL 格式化、Cron 表达式生成器、时间戳互转 |
| 文本编码 | Base64/URL/HTML 编解码、文本对比(Diff)、进制转换、哈希计算(MD5/SHA)、字符串工具 |
| 网络/API | IP/DNS 查询、HTTP 请求调试(简版 Postman)、UUID/密码生成器    |
| 系统实用 | 番茄钟(Pomodoro)、Markdown 实时预览                          |
| 娱乐趣味 | 打字速度测试、彩虹屁/毒鸡汤生成器                            |

详见 [tools/tools.md](tools/tools.md) 和 [tools/UI设计方案.md](tools/UI设计方案.md)

## 快速开始

### 环境要求

- Java 17+
- MySQL 8.0+
- Maven 3.6+

### 配置数据库

```sql
CREATE DATABASE hermes_data DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hermes_data?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

### 启动项目

```bash
# 克隆项目
git clone https://github.com/dsqblack/hermes-study.git
cd hermes-study

# 编译并启动
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/stock-collector-1.0.0.jar
```

启动后访问：`http://localhost:8081`

## 前端设计

项目采用统一的 **Tableau 浅色风格** 设计语言：

- 画布背景：`#f5f6f8`
- 卡片容器：白色 + 轻微阴影
- 主强调色：`#2563eb`（蓝色）
- 内置浅色/深色主题切换
- 所有页面为纯静态 HTML，无前端构建工具依赖

首页采用 **Bento Grid** 布局，Glass-morphism 毛玻璃卡片风格，支持动态光球背景和入场动画。

## License

MIT
