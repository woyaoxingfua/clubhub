import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/Register.vue')
    },
    {
      path: '/public',
      name: 'PublicLanding',
      component: () => import('../views/PublicLanding.vue')
    },
    // --- 公开门户模块（无需登录，使用PortalLayout）---
    {
      path: '/portal',
      component: () => import('../views/PortalLayout.vue'),
      children: [
        {
          path: 'clubs',
          name: 'ClubPortal',
          component: () => import('../views/portal/ClubPortal.vue')
        },
        {
          path: 'club/:clubId',
          name: 'ClubDetail',
          component: () => import('../views/portal/ClubDetail.vue')
        },
        {
          path: 'calendar',
          name: 'EventCalendar',
          component: () => import('../views/portal/EventCalendar.vue')
        }
      ]
    },
    {
      // 主布局路由（需要登录）
      path: '/',
      component: () => import('../views/Layout.vue'),
      redirect: (to) => {
        // 检查用户是否登录
        const user = localStorage.getItem('user')
        return user ? '/home' : '/public'
      },
      children: [
        {
          path: 'home',
          name: 'Home',
          component: () => import('../views/Home.vue')
        },
        {
          path: 'event/list',
          name: 'EventList',
          component: () => import('../views/event/EventList.vue')
        },
        {
          path: 'notice/list',
          name: 'NoticeList',
          component: () => import('../views/notice/NoticeList.vue')
        },
        {
          path: 'club/list',
          name: 'ClubList',
          component: () => import('../views/club/ClubList.vue')
        },
        // --- 招新模块 ---
        {
          path: 'recruit/plans',
          name: 'RecruitPlanList',
          component: () => import('../views/recruit/RecruitPlanList.vue')
        },
        {
          path: 'recruit/applications',
          name: 'RecruitApplicationList',
          component: () => import('../views/recruit/RecruitApplicationList.vue')
        },
        // --- 成员管理模块 ---
        {
          path: 'member/management',
          name: 'MemberManagement',
          component: () => import('../views/member/MemberManagement.vue')
        },
        {
          path: 'member/contacts',
          name: 'MemberContacts',
          component: () => import('../views/member/MemberContacts.vue')
        },
        // --- 财务模块 ---
        {
          path: 'finance/list',
          name: 'FinanceList',
          component: () => import('../views/finance/FinanceList.vue')
        },
        // --- 消息模块 ---
        {
          path: 'message/list',
          name: 'MessageList',
          component: () => import('../views/message/MessageList.vue')
        },
        // --- 数据统计模块 ---
        {
          path: 'statistics/dashboard',
          name: 'Dashboard',
          component: () => import('../views/statistics/Dashboard.vue')
        },
        // --- 门户浏览（在Layout中）---
        {
          path: 'view/clubs',
          name: 'ViewClubs',
          component: () => import('../views/portal/ClubPortal.vue')
        },
        {
          path: 'view/club/:clubId',
          name: 'ViewClubDetail',
          component: () => import('../views/portal/ClubDetail.vue')
        },
        {
          path: 'view/calendar',
          name: 'ViewCalendar',
          component: () => import('../views/portal/EventCalendar.vue')
        }
      ]
    }
  ]
})

// 【新增】路由守卫：根据登录状态智能处理portal页面访问
router.beforeEach((to, from, next) => {
  const user = localStorage.getItem('user')
  
  // 公开路径：不需要登录即可访问
  const publicPaths = ['/login', '/register', '/public', '/portal']
  const isPublicPath = publicPaths.some(path => to.path.startsWith(path))
  
  if (isPublicPath) {
    next()
    return
  }
  
  // 需要登录的页面
  if (!user && to.path !== '/') {
    next('/login')
  } else {
    next()
  }
})

export default router