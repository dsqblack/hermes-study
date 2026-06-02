# AGENTS.md

## Project
Hermes Study — Spring Boot 3.2 + MyBatis + MySQL 个人学习实验项目
- GitHub: `dsqblack/hermes-study`
- Port: 8081
- DB: `hermes_data` (MySQL localhost:3306)

## Modules

### 1. 股市情报站 (`/stock`)
- `StockController.java` — `/api/stock/hot` 行情采集入口
- `StockService.java` — 腾讯指数 + 新浪板块/个股/新闻爬取
- `static/stock.html` — Tableau 风格前端页面

### 2. Memo Board (`/memo`)
- `MemoController.java` — `/api/memo/*` 备忘录 CRUD
- `MemoService.java` — 业务逻辑 + 标签/分类/作者管理
- `MemoMapper.java/xml` — MyBatis SQL
- `static/memo.html` — Linear/Notion 极简风格前端

### 3. 官网首页 (`/`)
- `static/index.html` — 项目门户，链接两个模块

## Rules
- 只做本地修改，不自动编译或启动服务
- 修改包含 SQL 时必须保留原 WHERE 条件
