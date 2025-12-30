// src/utils/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 导出baseURL供其他组件使用
export const baseURL = '/club_system_war_exploded/api'

const request = axios.create({
    // 注意：这里填你后端的端口 8080，路径加上 context path
    baseURL: baseURL, 
    timeout: 5000
})

// 请求拦截器：每次请求都添加 Authorization 请求头
request.interceptors.request.use(config => {
    // 从 localStorage 获取 Token
    const token = localStorage.getItem('token')
    // 兼容某些场景下 headers 可能未初始化
    if (!config.headers) config.headers = {}
    if (token) {
        // 添加到请求头 (Bearer Schema)
        config.headers['Authorization'] = 'Bearer ' + token
    }
    
    // 为 AI 相关的请求增加超时时间（因为 AI 处理时间较长）
    if (config.url && config.url.includes('/ai/')) {
        config.timeout = 30000 // 30 秒用于 AI 请求
    }
    
    return config
}, error => {
    return Promise.reject(error)
})

// 响应拦截器：帮我们统一处理后端返回的 Result
request.interceptors.response.use(
    response => {
        const res = response.data
        // 如果后端返回 code 不是 200，说明出错了
        if (res.code !== 200) {
            ElMessage.error(res.msg || '系统错误')
            return Promise.reject(new Error(res.msg || 'Error'))
        }
        return res
    },
    error => {
        // 处理 HTTP 状态码错误
        if (error.response && error.response.status === 401) {
            ElMessage.error('登录已过期，请重新登录')
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            localStorage.removeItem('userId') // 清理旧数据
            // 跳转回登录页 (简单做法：刷新页面或使用 router push)
            setTimeout(() => {
                window.location.href = '/login'
            }, 1000)
            return Promise.reject(error)
        }
        
        console.log('err' + error)
        ElMessage.error(error.message)
        return Promise.reject(error)
    }
)

export default request