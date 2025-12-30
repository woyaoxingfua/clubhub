# 校园社团管理系统 - 后端 (Backend)

基于 SSM（Spring + SpringMVC + MyBatis）架构的校园社团管理系统后端服务

## 技术栈

- **Java**: 17
- **Spring Framework**: 6.1.14
- **SpringMVC**: 6.1.14
- **MyBatis**: 3.5.16
- **MySQL**: 8.0.33
- **Druid**: 1.2.23 (数据库连接池)
- **Jackson**: 2.17.0 (JSON处理)
- **JWT**: 0.11.5 (身份认证)
- **Lombok**: 1.18.30
- **Logback**: 1.4.14 (日志)
- **Maven**: 3.x

## 项目结构

```
club-system/
├── src/main/java/com/tlh/
│   ├── controller/        # 控制器层 - 处理 HTTP 请求
│   ├── service/          # 业务逻辑层 - 业务处理
│   │   └── impl/         # Service 实现类
│   ├── dao/              # 数据访问层 - 数据库操作
│   ├── entity/           # 实体类 - 数据库表映射
│   ├── vo/               # 视图对象 - 数据传输对象
│   ├── utils/            # 工具类 - 通用工具方法
│   └── config/           # 配置类 - Spring 配置
├── src/main/resources/
│   ├── mapper/           # MyBatis 映射文件
│   ├── spring/           # Spring 配置文件
│   │   ├── applicationContext.xml      # Spring 核心配置
│   │   ├── spring-mvc.xml               # SpringMVC 配置
│   │   └── spring-mybatis.xml           # MyBatis 配置
│   ├── jdbc.properties   # 数据库连接配置
│   └── logback.xml       # 日志配置
├── src/main/webapp/
│   └── WEB-INF/
│       └── web.xml       # Web 应用配置
└── pom.xml               # Maven 依赖配置
```

## 快速开始

### 环境要求

- JDK 17 或更高版本
- Maven 3.6 或更高版本
- MySQL 8.0 或更高版本
- Tomcat 10.x（支持 Jakarta EE）

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/woyaoxingfua/clubhub.git
cd clubhub/club-system
```

2. **创建数据库**
```sql
CREATE DATABASE club_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **配置数据库连接**

编辑 `src/main/resources/jdbc.properties`：
```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/club_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
jdbc.username=root
jdbc.password=your_password
```

4. **编译打包**
```bash
mvn clean package
```

5. **部署到 Tomcat**
将 `target/club-system.war` 复制到 Tomcat 的 `webapps` 目录

6. **启动服务**
```bash
# Windows
catalina.bat start

# Linux/Mac
./catalina.sh start
```

服务将运行在：`http://localhost:8080/club-system`

## API 接口文档

### 认证相关
- `POST /auth/login` - 用户登录
- `POST /auth/register` - 学生注册
- `POST /auth/logout` - 用户登出

### 社团管理
- `GET /clubs` - 获取社团列表
- `POST /clubs` - 创建社团
- `PUT /clubs/{id}` - 更新社团信息
- `DELETE /clubs/{id}` - 删除社团
- `POST /clubs/{id}/logo` - 上传社团 Logo

### 成员管理
- `GET /clubs/{clubId}/members` - 获取成员列表
- `POST /clubs/{clubId}/members` - 添加成员
- `PUT /members/{id}` - 更新成员信息
- `DELETE /members/{id}` - 删除成员

### 活动管理
- `GET /activities` - 获取活动列表
- `POST /activities` - 创建活动
- `PUT /activities/{id}` - 更新活动
- `POST /activities/{id}/approve` - 审批活动

### 招新管理
- `GET /recruitments` - 获取招新计划
- `POST /recruitments` - 创建招新计划
- `POST /applications` - 提交招新申请
- `POST /applications/{id}/approve` - 审批申请

### 财务管理
- `GET /finances` - 获取财务记录
- `POST /finances` - 添加财务记录
- `POST /finances/{id}/voucher` - 上传财务凭证

### 数据统计
- `GET /statistics/overview` - 系统总览数据
- `GET /statistics/members/trend` - 成员增长趋势
- `GET /statistics/clubs/distribution` - 社团分布统计
- `GET /statistics/activities` - 活动统计

> 完整的 API 文档可使用 Postman 或 Swagger 查看

## 数据库表结构

| 表名 | 说明 |
|------|------|
| sys_user | 用户表 |
| club | 社团表 |
| club_member | 社团成员表 |
| activity | 活动表 |
| activity_registration | 活动报名表 |
| recruitment_plan | 招新计划表 |
| recruitment_application | 招新申请表 |
| finance_record | 财务记录表 |
| announcement | 公告表 |
| message | 站内消息表 |
| department | 部门表 |
| position | 职务表 |

## 开发规范

### 代码规范
- 使用 4 空格缩进
- 类名使用大驼峰命名法（PascalCase）
- 方法名和变量名使用小驼峰命名法（camelCase）
- 常量使用全大写下划线分隔（UPPER_CASE）

### 分层职责
- **Controller 层**：处理 HTTP 请求，参数验证，调用 Service
- **Service 层**：业务逻辑处理，事务控制
- **DAO 层**：数据库操作，SQL 映射
- **Entity 层**：数据库表映射
- **VO 层**：视图对象，数据传输

### 事务管理
在 Service 层使用 `@Transactional` 注解管理事务：
```java
@Transactional(rollbackFor = Exception.class)
public void createActivity(Activity activity) {
    // 业务逻辑
}
```

## 常见问题

### 1. 编译错误：找不到 jakarta.servlet
确保使用 JDK 17 和 Tomcat 10.x

### 2. 数据库连接失败
检查 `jdbc.properties` 配置是否正确

### 3. Lombok 注解不生效
确保 IDE 安装了 Lombok 插件

## 许可证

本项目采用 MIT 许可证