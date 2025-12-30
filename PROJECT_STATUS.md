# 校园社团管理系统 - 开发进度与交接文档 (Development Handover Doc)

**最后更新**: 2025年12月27日
**当前版本**: v0.9.0 (通讯录和站内信系统)

---

## 1. 项目概览 (Project Overview)

本项目是一个基于前后端分离架构的校园社团管理系统，旨在为学生、社团管理员、指导老师和系统管理员提供一体化的管理平台。

### 1.1 技术栈 (Tech Stack)
*   **后端**: JDK 17+, Spring Boot 3.x, MyBatis, MySQL 8.0.
*   **前端**: Vue 3 (Composition API), Vite, Element Plus, Axios, Vue Router.
*   **开发工具**: IntelliJ IDEA, Maven, Node.js 18+.

### 1.2 目录结构说明
*   `club-system/`: 后端工程根目录
    *   `src/main/java/com/tlh/club_system/`: 源码
        *   `controller/`: REST API 控制器
        *   `entity/`: 数据库实体 (MyBatis Generator生成)
        *   `mapper/`: 数据持久层接口
        *   `service/`: 业务逻辑层 (接口与实现)
    *   `src/main/resources/mapper/`: MyBatis XML 映射文件
*   `club_web/`: 前端工程根目录 (Vite项目)
    *   **成员管理**: `src/views/member/MemberManagement.vue`
    *   **通讯录**: `src/views/member/MemberContacts.vue` ✨ **v0.9.0新增**
    *   **站内信**: `src/views/message/MessageList.vue` ✨ **v0.9.0新增**
    *   `src/utils/`: 工具类 (request.js 封装 Axios)

---

## 2. 核心业务逻辑与实现状态 (Core Logic Implementation)

### 2.1 角色与权限体系 (RBAC Assumption)
目前系统主要基于 `roleKey` 字段进行前端权限控制（后端暂未集成 Spring Security，依赖前端隐藏与业务层校验）。
*   **SYS_ADMIN (系统管理员)**: 拥有所有权限。
*   **DEPT_ADMIN (院系管理员)**: 类似系统管理员，管理院系下社团。
*   **TEACHER (教师)**: 可被选为社团指导老师。
*   **STUDENT (学生)**: 普通用户，可申请加入社团、报名活动。

### 2.2 已完成模块详解

#### A. 社团管理 (Club Management)
*   **功能**: 社团的增删改查、成员管理。
*   **关键逻辑**:
    *   **创建社团**: 管理员需指定“社长”和“指导老师”。前端已实现**过滤逻辑**，指导老师下拉框仅显示 `roleKey` 为 `TEACHER` 或 `SYS_ADMIN` 的用户。
    *   **成员加入**: 提供两种方式。
        1.  **直接加入**: 学生在列表点击“申请加入”，后端 (`ClubMemberController`) 直接插入记录（`insertSelective`），状态默认为 1 (在社)。
        2.  **招新流程**: 见下文。
    *   **数据一致性**: 后端已修复 `insert` 为 `insertSelective`，防止因数据库默认值或非空约束导致的 500 错误。

#### B. 招新系统 (Recruitment System)
*   **功能**: 招新计划发布、入社申请、审批录用。
*   **关键逻辑**:
    *   **计划发布**: 管理员发布计划时必须**关联社团** (`clubId`)。后台自动设置开始时间，若前端未传结束时间，默认设置为1个月后。
    *   **申请提交**: 学生填写自我介绍提交申请。后端 (`RecruitServiceImpl`) 会校验**重复申请**，防止同一计划重复提交。
    *   **审批流 (Transaction)**: 
        *   管理员点击“录用” (`auditApplication`)。
        *   **事务控制**: 方法加了 `@Transactional`。
        *   **自动入社**: 审批通过后，系统自动在 `tb_club_member` 表中创建成员记录。若该用户曾退社，则自动恢复其状态。

#### C. 活动管理 (Event System)
*   **功能**: 活动申请、审批、撤销、发布、学生报名。
*   **权限视图**:
    *   **学生**: 只能看到 *自己发起的待审核活动* 或 *已发布的活动*。隐藏审批按钮。
    *   **管理员**: 可以看到所有活动，并进行“通过/驳回”操作。
*   **报名逻辑**:
    *   学生点击“立即报名”，后端 (`EventSignupController`) 记录报名信息。
    *   前端 `loadData` 会自动获取当前用户的报名状态，动态切换“立即报名”/“取消报名”按钮。

#### D. 公告系统 (Notice System)
*   **功能**: 管理员发布通知，全校/本院/本社可见。
*   **实现细节**:
    *   **后端**: 采用标准的 Controller-Service-Mapper 分层架构。新增 `NoticeController`, `NoticeService`, `NoticeMapper`。
    *   **前端**: 
        *   **管理页**: `src/views/notice/NoticeList.vue` 提供公告的增删改查。
        *   **首页展示**: `Home.vue` 集成 Top 5 最新公告展示卡片。
    *   **API**: 
        *   `GET /api/notice/list`: 获取列表
        *   `POST /api/notice/add`: 发布
        *   `PUT /api/notice/edit`: 修改
        *   `DELETE /api/notice/delete/{id}`: 删除

#### E. 成员管理系统 (Member Management) ✅ **已完成**
*   **功能**: 成员花名册、部门职务管理、权限组分配、状态变更。
*   **核心特性**:
    *   **花名册管理**: 完整的成员列表，包含在社和已离社成员
    *   **部门职务**: 灵活的部门分配和职务设置
    *   **权限组系统**: 三级权限控制（admin/leader/member）
    *   **状态变更**: 支持主动退出、被开除、转入（重新激活）
*   **数据库增强**:
    *   新增字段：`department`（部门）、`role_group`（权限组）、`quit_type`（退出类型）、`quit_reason`（退出原因）、`quit_time`（退出时间）
    *   创建索引：优化查询性能
*   **权限机制**:
    *   **三层判断**: Club.presidentId（最高） → role_group（辅助） → position（显示）
    *   **数据同步**: 提供 `fix_president_data.sql` 确保社长数据一致性
*   **前端页面**: `src/views/member/MemberManagement.vue`
    *   筛选功能：按状态、部门、姓名搜索
    *   批量操作：编辑、分配权限、退出、开除、转入
    *   统计分析：成员统计、部门分布
*   **API接口**: 
    *   `GET /api/member/roster/{clubId}`: 获取完整花名册
    *   `GET /api/member/filter/{clubId}`: 按条件筛选
    *   `PUT /api/member/update`: 更新成员信息
    *   `PUT /api/member/assign-role`: 分配权限组
    *   `POST /api/member/transfer-in/{memberId}`: 转入
    *   `PUT /api/member/quit`: 退出
    *   `PUT /api/member/dismiss`: 开除
    *   `GET /api/member/statistics/{clubId}`: 统计信息
    *   `GET /api/member/my-memberships`: **[v0.9.0新增]** 获取我加入的社团

#### F. 通讯录系统 (Contacts System) ✨ **v0.9.0新增**
*   **功能**: 社团成员通讯录查看和导出。
*   **核心特性**:
    *   **成员通讯录**: 显示成员完整联系方式（姓名、手机、邮箱、学号）
    *   **搜索筛选**: 按姓名、部门、职务筛选
    *   **一键复制**: 快速复制联系方式
    *   **导出功能**: 导出通讯录为CSV文件
    *   **多社团支持**: 支持切换查看不同社团通讯录
*   **权限控制**:
    *   只有社团成员（包括社长）可以查看本社团通讯录
*   **前端页面**: `src/views/member/MemberContacts.vue`
*   **API接口**:
    *   `GET /api/member/my-memberships`: 获取我加入的所有社团
    *   `GET /api/member/filter/{clubId}`: 按条件筛选成员

#### G. 站内信系统 (Messaging System) ✨ **v0.9.0新增**
*   **功能**: 社团成员之间发送和接收站内消息。
*   **核心特性**:
    *   **发送消息**: 选择收件人，输入主题和内容
    *   **收件箱**: 查看收到的消息，未读消息提醒
    *   **发件箱**: 查看发送的消息，显示对方阅读状态
    *   **消息状态**: 未读/已读状态追踪
    *   **批量操作**: 批量标记已读、批量删除
    *   **关联社团**: 消息可关联社团（可选）
*   **数据库表**: `tb_message`
    *   字段：message_id, sender_id, receiver_id, club_id, subject, content, is_read, send_time, read_time
    *   索引：sender, receiver, is_read, club_id
*   **后端实现**:
    *   Entity: `com.tlh.club_system.entity.Message`
    *   Mapper: `com.tlh.club_system.mapper.MessageMapper`
    *   Service: `com.tlh.club_system.service.MessageService`
    *   Controller: `com.tlh.club_system.controller.MessageController`
*   **前端页面**: `src/views/message/MessageList.vue`
*   **API接口**:
    *   `POST /api/message/send`: 发送消息
    *   `GET /api/message/received`: 获取收件箱
    *   `GET /api/message/sent`: 获取发件箱
    *   `PUT /api/message/read/{messageId}`: 标记已读
    *   `PUT /api/message/read/batch`: 批量标记已读
    *   `DELETE /api/message/delete/{messageId}`: 删除消息
    *   `GET /api/message/unread/count`: 获取未读数量
    *   `GET /api/message/unread/recent`: 获取最近未读消息

---

## 3. 待开发模块详细说明 (Remaining Modules)

以下模块是项目完工的关键拼图，需按顺序开发。

### 3.1 财务管理模块 (Finance) ✅ **已完成**
*   **状态**: ✅ 已实现完整的财务管理功能
*   **已实现功能**:
    1.  **列表页**: 展示财务流水（ID、社团、金额、类型-收入/支出、描述、时间）。
    2.  **录入页**: 社团管理员录入一笔开支或收入。
    3.  **统计**: 计算当前社团余额、总收入、总支出。
    4.  **审批流程**: 社长添加的财务记录需要管理员审批。
*   **文件位置**:
    *   后端：`FinanceController`, `FinanceService`, `FinanceMapper`
    *   前端：`src/views/finance/FinanceList.vue`

### 3.2 ~~通讯录系统 (Contacts)~~ ✅ **已完成 (v0.9.0)**
*   **状态**: ✅ 已实现完整的通讯录功能
*   详见上文"已完成模块详解 - F. 通讯录系统"

### 3.3 ~~站内信系统 (Messaging)~~ ✅ **已完成 (v0.9.0)**
*   **状态**: ✅ 已实现完整的站内信功能
*   详见上文"已完成模块详解 - G. 站内信系统"

### 3.2 文件服务 (File Service) ✅ 已完成
*   **状态**: ✅ 已实现完整的文件上传功能
*   **已实现接口**:
    1.  `/api/file/upload/logo` - Logo图片上传
    2.  `/api/file/upload` - 普通文件上传
    3.  `/api/file/upload/resume` - **[新增]** 简历文件上传
*   **存储方案**: 
    *   本地磁盘：`%USERPROFILE%/club-system-uploads/`
    *   目录结构：`logos/`、`files/`、`resumes/`
    *   静态资源映射：`WebConfig.java` 配置 `/uploads/**` 映射
*   **应用场景**: 
    *   ✅ 社团 Logo 上传
    *   ✅ 活动海报上传
    *   ✅ 招新简历上传

---

## 4. 接口与数据字典速查 (Quick Reference)

### 4.1 关键 API
| 模块 | 方法 | 路径 | 说明 |
| :--- | :--- | :--- | :--- |
| **Member** | GET | `/api/member/roster/{clubId}` | **[新增]** 获取完整花名册 |
| **Member** | PUT | `/api/member/update` | **[新增]** 更新成员信息 |
| **Member** | PUT | `/api/member/assign-role` | **[新增]** 分配权限组 |
| **Member** | PUT | `/api/member/quit` | **[新增]** 成员退出 |
| **Member** | PUT | `/api/member/dismiss` | **[新增]** 开除成员 |
| **File** | POST | `/api/file/upload/resume` | 简历文件上传 |
| **File** | POST | `/api/file/upload/logo` | Logo图片上传 |
| **Notice** | GET | `/api/notice/list` | 公告列表 |
| **Recruit** | POST | `/api/recruit/apply` | 提交入社申请（含简历） |
| **Auth** | POST | `/api/auth/login` | 登录 |
| **Club** | GET | `/api/club/list` | 社团列表 |

### 4.2 常见问题排查
1.  **500 错误**: 通常检查 Entity 字段是否为 `null` 但数据库要求 `NOT NULL`。使用 `insertSelective` 可规避大部分默认值问题。
2.  **权限不对**: 检查浏览器的 `localStorage` 中 `user` 对象的 `roleKey`。

---

## 5. 后续开发计划 (Next Steps Roadmap)

1.  ~~**完善财务 (Finance)**: 完成社团管理的最后一块拼图。~~ ✅ **已完成**
2.  ~~**文件服务 (File Service)**: 实现图片/文件上传功能。~~ ✅ **已完成**
    - ✅ Logo 上传功能已实现
    - ✅ 简历上传功能已实现
    - ✅ 静态资源映射已配置
3.  ~~**成员管理 (Member Management)**: 成员花名册、状态变更、权限组分配。~~ ✅ **已完成**
    - ✅ 完整花名册管理
    - ✅ 部门职务管理
    - ✅ 权限组系统（三级权限）
    - ✅ 状态变更（退出/开除/转入）
    - ✅ 统计分析
4.  ~~**社长权限系统修复**~~ ✅ **已完成 (v0.8.1 + v0.8.2)**
    - ✅ 禁止社长随意设置"社长"职位
    - ✅ 创建专门的社长转让功能（仅管理员可用）
    - ✅ 确保权限判断基于 Club.presidentId
    - ✅ 前后端双重保护机制
    - ✅ **v0.8.2新增**：职位与权限组强绑定
    - ✅ **v0.8.2新增**：移除独立权限组分配功能
    - ✅ **v0.8.2新增**：管理员可见"社长"选项并自动同步presidentId
5.  ~~**紧急BUG修复**~~ ✅ **已完成 (v0.8.3)**
    - ✅ 修复新社长无法发布活动的问题
    - ✅ 添加"刷新权限"功能
    - ✅ 修复可以重复申请加入社团的BUG
    - ✅ 优化用户体验
6.  ~~**通讯录和站内信系统**~~ ✅ **已完成 (v0.9.0)**
    - ✅ 社团成员通讯录（搜索、筛选、导出）
    - ✅ 站内消息收发（收件箱、发件箱、批量操作）
    - ✅ 未读消息提醒
    - ✅ 消息状态追踪
7.  **数据统计Dashboard** (待开发):
    *   社团成员增长趋势
    *   活动参与度分析
    *   财务收支图表
    *   使用 ECharts 可视化
8.  **UI/UX 优化**:
    *   为 `ClubList` 和 `EventList` 增加分页 (Pagination)。
9.  **安全加固**: 后端引入拦截器或 Spring Security，验证 Token，防止越权调用 API。

---

## 📚 相关开发文档

### 最新更新（v0.9.0）
*   **通讯录和站内信系统开发完成-v0.9.0.md** - 通讯录和站内信功能完整文档

### v0.8.3
*   **活动发布和重复加入BUG修复说明-v0.8.3.md** - 新社长活动发布和重复加入修复

### v0.8.2
*   **社长权限系统深度修复说明-v0.8.2.md** - 职位与权限组强绑定

### v0.8.1
*   **社长权限系统修复说明.md** - 社长转让功能和权限修复
*   **社长权限修复-快速测试指南.md** - 15分钟快速测试
*   **社长权限修复-开发完成总结.md** - 第一轮修复总结

### v0.8.0
*   **成员管理功能开发完成.md** - 成员管理系统详细文档
*   **成员管理功能交接文档.md** - 快速交接指南
*   **成员管理功能开发总结.md** - 功能总结

### v0.7.0
*   **在线招新简历上传功能开发完成(已完结).md** - 详细的功能开发文档
*   **简历上传功能测试指南.md** - 快速测试指南和问题排查
*   **简历上传功能开发总结.md** - 功能总结和使用说明

### 历史文档
*   **社团门户功能开发完成报告(已完结).md** - 社团门户功能
*   **公告置顶功能升级指南（已完结）.md** - 公告系统
*   **招新权限调试指引(已完结).md** - 招新权限修复
*   **文件上传功能修改说明.md** - 文件上传基础配置

---
*文档编写人: AI Assistant*
*日期: 2025-12-26*