<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-left">
        <img src="https://img.icons8.com/clouds/200/000000/school.png" alt="Logo" class="logo"/>
        <h2>校园社团管理系统</h2>
        <p>Campus Club Management System</p>
        <p class="desc">高效 · 便捷 · 数字化</p>
      </div>

      <div class="login-right">
        <h3>欢迎登录</h3>
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
          
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入学号/工号" 
              size="large"
              :prefix-icon="User">
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码" 
              size="large"
              show-password
              :prefix-icon="Lock">
            </el-input>
          </el-form-item>

          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin" size="large">
            立即登录
          </el-button>
          
          <div class="tips">
            <span>还没有账号？<a @click="goToRegister" class="register-link">立即注册</a></span>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import request from '@/utils/request' // 导入我们之前封装的axios
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

    const handleLogin = () => {
      loginFormRef.value.validate((valid) => {
        if (valid) {
          loading.value = true
          request.post('/auth/login', loginForm).then(res => {
            // res.data 包含 { token: "...", user: {...} }
            const data = res.data
            
            // 存储 Token 和 用户信息
            localStorage.setItem('token', data.token)
            localStorage.setItem('user', JSON.stringify(data.user))
            localStorage.setItem('userId', data.user.userId) // 兼容旧代码，保留 userId
            
            ElMessage.success('登录成功')
            router.push('/')
          }).catch(() => {
            // 错误已由 request.js 处理
          }).finally(() => {
            loading.value = false
          })
        }
      })
    }

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  /* 漂亮的渐变背景 */
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 800px;
  height: 400px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
  display: flex;
  overflow: hidden;
}

.login-left {
  width: 50%;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 20px;
}

.login-left img {
  width: 100px;
  margin-bottom: 20px;
}

.login-left h2 {
  color: #333;
  margin-bottom: 5px;
}

.desc {
  color: #999;
  font-size: 14px;
  margin-top: 10px;
}

.login-right {
  width: 50%;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-right h3 {
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

.login-btn {
  width: 100%;
  margin-top: 10px;
  font-weight: bold;
}

.tips {
  margin-top: 20px;
  text-align: center;
  font-size: 12px;
  color: #999;
}

.register-link {
  color: #667eea;
  cursor: pointer;
  text-decoration: none;
  margin-left: 5px;
}

.register-link:hover {
  text-decoration: underline;
}
</style>