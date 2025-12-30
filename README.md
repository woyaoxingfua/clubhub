# 校园社团管理系统 (Campus Club Management System)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring](https://img.shields.io/badge/Spring-6.1.14-green.svg)](https://spring.io/)
[![Vue](https://img.shields.io/badge/Vue.js-3.5-brightgreen.svg)](https://vuejs.org/)

一个基于 SSM（Spring + SpringMVC + MyBatis）和 Vue 3 的前后端分离校园社团管理系统，支持社团管理、活动发布、成员管理、招新管理、财务管理、数据统计等功能。

## 📋 目录

- [项目简介](#-项目简介)
- [核心功能](#-核心功能)
- [技术栈](#-技术栈)
- [系统架构](#-系统架构)
- [快速开始](#-快速开始)
- [项目结构](#-项目结构)
- [功能截图](#-功能截图)
- [开发文档](#-开发文档)
- [许可证](#-许可证)

## 🎯 项目简介

本系统旨在解决传统社团管理中的痛点问题：

- 📝 **信息分散**：社团信息、成员资料、活动记录等数据分散管理
- 🔄 **流程不规范**：活动审批、招新管理缺乏统一流程
- 📊 **统计困难**：缺少数据可视化，难以进行数据分析
- 💬 **沟通不便**：成员之间缺少便捷的站内沟通渠道

通过本系统，可以实现：

✅ 社团信息集中化管理  
✅ 活动审批流程规范化  
✅ 数据统计可视化分析  
✅ 成员通讯录和站内消息  
✅ 在线招新和简历管理  
✅ 财务收支透明化管理

## ✨ 核心功能

### 1. 数据统计 Dashboard
- 📊 系统总览：社团总数、成员总数、活动总数、招新申请
- 📈 成员增长趋势：近12个月的成员变化
- 🥧 社团成员分布：各社团规模对比
- 📊 活动统计：活动审批状态分析
- 💰 财务统计：收支情况可视化
- 📝 招新统计：招新转化率分析
- 🏆 社团活跃度排名：Top 10社团
- 📈 活动参与率趋势：学生参与度分析

### 2. 社团管理
- 创建和编辑社团信息
- 上传社团 Logo
- 管理社团荣誉
- 指定指导老师
- 社团状态管理（正常/注销）

### 3. 成员管理
- 三级权限体系（admin/leader/member）
- 成员花名册（在社+离社）
- 部门和职务管理
- 成员状态变更（退出、开除、转入）
- 社长转让功能

### 4. 活动管理
- 三级审批流程（社长 → 指导老师 → 院系管理员）
- 活动发布和报名
- 活动日历展示
- 报名名单管理
- 活动状态追踪

### 5. 通讯录与站内消息
- 社团成员通讯录（搜索、筛选、导出）
- 站内消息收发（收件箱、发件箱）
- 未读消息提醒（徽章显示）
- 批量操作（批量已读、批量删除）

### 6. 招新管理
- 发布招新计划
- 在线简历上传（支持 PDF、Word、图片）
- 招新申请审批
- 录用后自动加入成员表

### 7. 财务管理
- 收入和支出记录
- 上传财务凭证
- 财务审批流程
- 财务统计报表

### 8. 公告管理
- 发布社团公告
- 公告置顶功能
- 公告编辑和删除

### 9. 社团门户
- 社团黄页（分类展示、搜索筛选）
- 社团详情页（Logo、简介、荣誉、活动）
- 活动日历（日历视图展示所有活动）
- 双布局支持（登录/未登录）

### 10. 用户管理
- 学生注册和登录
- 四种用户角色（管理员、指导老师、社长、学生）
- 个人资料管理
- 密码修改

## 🛠 技术栈

### 后端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring | 6.1.14 | IoC 容器 |
| SpringMVC | 6.1.14 | MVC 框架 |
| MyBatis | 3.5.16 | ORM 框架 |
| MySQL | 8.0.33 | 数据库 |
| Druid | 1.2.23 | 数据库连接池 |
| Jackson | 2.17.0 | JSON 处理 |
| JWT | 0.11.5 | 身份认证 |
| Lombok | 1.18.30 | 简化代码 |
| Logback | 1.4.14 | 日志框架 |
| Maven | 3.x | 项目管理 |

### 前端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.25 | 前端框架 |
| Vue Router | 4.6.3 | 路由管理 |
| Element Plus | 2.13.0 | UI 组件库 |
| ECharts | 6.0.0 | 数据可视化 |
| Axios | 1.13.2 | HTTP 客户端 |
| Vite | 7.2.4 | 构建工具 |

## 🏗 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                      Browser                             │
└──────────────────────┬──────────────────────────────────┘
                       │
                       │ HTTP/HTTPS
                       │
┌──────────────────────▼──────────────────────────────────┐
│                   Vue 3 Frontend                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Vue Router │  │ Element Plus │  │   ECharts    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└──────────────────────┬──────────────────────────────────┘
                       │
                       │ RESTful API
                       │
┌──────────────────────▼──────────────────────────────────┐
│               SpringMVC Backend                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Controller  │  │   Service    │  │     DAO      │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└──────────────────────┬──────────────────────────────────┘
                       │
                       │ JDBC
                       │
┌──────────────────────▼──────────────────────────────────┐
│                   MySQL Database                         │
└─────────────────────────────────────────────────────────┘
```

## 🚀 快速开始

### 环境要求

- **JDK**: 17 或更高版本
- **Maven**: 3.6 或更高版本
- **Node.js**: 20.19.0 或 22.12.0 及以上
- **MySQL**: 8.0 或更高版本
- **Tomcat**: 10.x（支持 Jakarta EE）

### 后端部署

1. **克隆项目**
```bash
git clone https://github.com/woyaoxingfua/clubhub.git
cd clubhub
```

2. **创建数据库**
```sql
CREATE DATABASE club_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **导入数据库表结构**
```bash
mysql -u root -p club_management < database/schema.sql
```

4. **配置数据库连接**

编辑 `club-system/src/main/resources/jdbc.properties`：
```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/club_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
jdbc.username=root
jdbc.password=your_password
```

5. **构建项目**
```bash
cd club-system
mvn clean package
```

6. **部署到 Tomcat**
将 `target/club-system.war` 部署到 Tomcat 的 `webapps` 目录

7. **启动 Tomcat**
```bash
# Windows
catalina.bat start

# Linux/Mac
./catalina.sh start
```

后端服务将运行在 `http://localhost:8080/club-system`

### 前端部署

1. **安装依赖**
```bash
cd club_web
npm install
```

2. **配置后端 API 地址**

编辑 `club_web/src/utils/request.js`：
```javascript
const service = axios.create({
  baseURL: 'http://localhost:8080/club-system',
  timeout: 5000
})
```

3. **启动开发服务器**
```bash
npm run dev
```

前端服务将运行在 `http://localhost:5173`

4. **构建生产版本**
```bash
npm run build
```

生成的静态文件在 `dist` 目录，可部署到 Nginx 等 Web 服务器

### 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 系统管理员 |
| 指导老师 | teacher | teacher123 | 指导老师 |
| 社长 | president | president123 | 社团社长 |
| 学生 | student | student123 | 普通学生 |

## 📁 项目结构

### 后端结构
```
club-system/
├── src/main/java/com/tlh/
│   ├── controller/        # 控制器层
│   ├── service/          # 业务逻辑层
│   ├── dao/              # 数据访问层
│   ├── entity/           # 实体类
│   ├── vo/               # 视图对象
│   ├── utils/            # 工具类
│   └── config/           # 配置类
├── src/main/resources/
│   ├── mapper/           # MyBatis 映射文件
│   ├── spring/           # Spring 配置文件
│   ├── jdbc.properties   # 数据库配置
│   └── logback.xml       # 日志配置
└── pom.xml               # Maven 配置
```

### 前端结构
```
club_web/
├── src/
│   ├── views/            # 页面组件
│   ├── router/           # 路由配置
│   ├── utils/            # 工具函数
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── public/               # 静态资源
├── package.json          # npm 配置
└── vite.config.js        # Vite 配置
```

## 📸 功能截图

> 待添加：可以在这里添加系统截图

## 📚 开发文档

项目包含详细的开发文档：

- [项目亮点总结](项目亮点总结-答辩必看.md)
- [快速开始指南](README-v0.10.0快速开始.md)
- [项目交接说明](README-项目交接说明.md)
- [项目状态](PROJECT_STATUS.md)
- [功能实现对照](功能实现对照与交接文档.md)

更多开发文档请查看项目根目录下的 `.md` 文件

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 👥 作者

- **项目作者**: tlh
- **联系方式**: [待添加]

## 🙏 致谢

- [Spring Framework](https://spring.io/)
- [MyBatis](https://mybatis.org/)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [Apache ECharts](https://echarts.apache.org/)

## 📊 项目统计

- **代码量**: ~17,000 行（前端 8,000 行 + 后端 9,000 行）
- **数据库表**: 12 张表
- **API 接口**: 90+ 个
- **功能模块**: 11 个核心模块
- **开发周期**: v0.1.0 → v0.11.0
- **完成度**: 98%

---

⭐ 如果这个项目对你有帮助，请给一个 Star！