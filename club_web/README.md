# 校园社团管理系统 - 前端 (Frontend)

基于 Vue 3 + Vite + Element Plus 的校园社团管理系统前端应用

## 技术栈

- **Vue**: 3.5.25 (Composition API)
- **Vue Router**: 4.6.3 (路由管理)
- **Element Plus**: 2.13.0 (UI 组件库)
- **ECharts**: 6.0.0 (数据可视化)
- **Axios**: 1.13.2 (HTTP 客户端)
- **Vite**: 7.2.4 (构建工具)
- **Node.js**: 20.19.0+ / 22.12.0+

## 项目结构

```
club_web/
├── src/
│   ├── views/                 # 页面组件
│   │   ├── Login.vue         # 登录页面
│   │   ├── Register.vue      # 注册页面
│   │   ├── Home.vue          # 首页
│   │   ├── Dashboard.vue     # 数据统计面板
│   │   ├── ClubManage.vue    # 社团管理
│   │   ├── MemberManage.vue  # 成员管理
│   │   ├── ActivityManage.vue # 活动管理
│   │   ├── RecruitManage.vue # 招新管理
│   │   ├── FinanceManage.vue # 财务管理
│   │   ├── Portal/           # 公共门户页面
│   │   │   ├── ClubList.vue  # 社团黄页
│   │   │   └── ClubDetail.vue # 社团详情
│   │   └── ...
│   ├── router/
│   │   └── index.js          # 路由配置
│   ├── utils/
│   │   └── request.js        # Axios 封装
│   ├── App.vue               # 根组件
│   └── main.js               # 入口文件
├── public/                    # 静态资源
├── package.json              # npm 依赖配置
├── vite.config.js            # Vite 配置
└── index.html                # HTML 模板
```

## 快速开始

### 环境要求

- Node.js 20.19.0 或 22.12.0 及以上
- npm 或 yarn

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/woyaoxingfua/clubhub.git
cd clubhub/club_web
```

2. **安装依赖**
```bash
npm install
```

3. **配置后端 API 地址**

编辑 `src/utils/request.js`：
```javascript
const service = axios.create({
  baseURL: 'http://localhost:8080/club-system',  // 后端 API 地址
  timeout: 5000
})
```

4. **启动开发服务器**
```bash
npm run dev
```

应用将运行在：`http://localhost:5173`

5. **构建生产版本**
```bash
npm run build
```

生成的静态文件在 `dist` 目录，可部署到 Nginx 等 Web 服务器

## 功能模块

### 1. 用户认证
- **登录** (`/login`) - 用户登录
- **注册** (`/register`) - 学生注册
- **首页** (`/home`) - 系统首页

### 2. 数据统计
- **Dashboard** (`/dashboard`) - 数据可视化面板
  - 系统总览（4个卡片）
  - 成员增长趋势（折线图）
  - 社团成员分布（饼图）
  - 活动统计（柱状图）
  - 财务统计
  - 招新统计
  - 社团活跃度排名
  - 活动参与率趋势

### 3. 社团管理
- **社团列表** - 查看所有社团
- **创建社团** - 新建社团
- **编辑社团** - 修改社团信息
- **上传 Logo** - 社团 Logo 上传
- **荣誉管理** - 社团荣誉编辑

### 4. 成员管理
- **成员花名册** - 查看所有成员
- **添加成员** - 添加新成员
- **部门管理** - 部门和职务设置
- **权限组** - 三级权限分配
- **社团通讯录** - 成员联系方式

### 5. 活动管理
- **活动列表** - 查看所有活动
- **创建活动** - 发布新活动
- **审批活动** - 三级审批流程
- **报名管理** - 活动报名名单
- **活动日历** - 日历视图展示

### 6. 招新管理
- **招新计划** - 发布招新计划
- **申请列表** - 查看招新申请
- **简历上传** - 上传个人简历
- **审批申请** - 审批招新申请

### 7. 财务管理
- **财务记录** - 收入和支出记录
- **凭证上传** - 上传财务凭证
- **财务统计** - 收支统计报表

### 8. 通讯录与消息
- **社团通讯录** - 查看成员联系方式
- **站内消息** - 发送和接收消息
- **收件箱** - 查看收到的消息
- **发件箱** - 查看发送的消息

### 9. 公告管理
- **公告列表** - 查看社团公告
- **发布公告** - 发布新公告
- **置顶公告** - 公告置顶功能

### 10. 社团门户
- **社团黄页** (`/portal/clubs`) - 公开社团列表
- **社团详情** (`/portal/clubs/:id`) - 社团详情页
- **活动日历** (`/portal/calendar`) - 公开活动日历

## 路由配置

### 主要路由
```javascript
const routes = [
  { path: '/login', component: Login },              // 登录
  { path: '/register', component: Register },        // 注册
  { path: '/home', component: Home },                // 首页
  { path: '/dashboard', component: Dashboard },      // 数据统计
  { path: '/clubs', component: ClubManage },         // 社团管理
  { path: '/members', component: MemberManage },     // 成员管理
  { path: '/activities', component: ActivityManage },// 活动管理
  { path: '/recruit', component: RecruitManage },    // 招新管理
  { path: '/finance', component: FinanceManage },    // 财务管理
  { path: '/messages', component: MessageManage },   // 站内消息
  { path: '/announcements', component: AnnouncementManage }, // 公告管理
  { path: '/portal/clubs', component: ClubList },    // 社团黄页
  { path: '/portal/clubs/:id', component: ClubDetail }, // 社团详情
]
```

### 路由守卫
在 `router/index.js` 中配置了路由守卫，用于：
- 检查用户登录状态
- 验证用户权限
- 重定向未登录用户到登录页

## 开发规范

### 代码规范
- 使用 2 空格缩进
- 组件名使用大驼峰命名法（PascalCase）
- 方法名和变量名使用小驼峰命名法（camelCase）
- 使用 Composition API 编写组件

### 组件示例
```vue
<template>
  <div class="container">
    <h1>{{ title }}</h1>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const title = ref('页面标题')

const fetchData = async () => {
  try {
    const res = await request.get('/api/data')
    console.log(res.data)
  } catch (error) {
    ElMessage.error('数据加载失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.container {
  padding: 20px;
}
</style>
```

### API 调用示例
```javascript
// GET 请求
const response = await request.get('/clubs')

// POST 请求
const response = await request.post('/clubs', {
  name: '社团名称',
  description: '社团简介'
})

// PUT 请求
const response = await request.put('/clubs/1', {
  name: '新名称'
})

// DELETE 请求
const response = await request.delete('/clubs/1')
```

## 常见问题

### 1. 安装依赖失败
尝试清除缓存后重新安装：
```bash
npm cache clean --force
npm install
```

### 2. 启动开发服务器失败
检查 Node.js 版本是否符合要求（20.19.0+ / 22.12.0+）

### 3. 跨域问题
在 `vite.config.js` 中配置代理：
```javascript
export default {
  server: {
    proxy: {
      '/club-system': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
}
```

### 4. Element Plus 图标不显示
确保安装了 `@element-plus/icons-vue`

## 生产部署

### Nginx 配置示例
```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/dist;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /club-system {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 许可证

本项目采用 MIT 许可证