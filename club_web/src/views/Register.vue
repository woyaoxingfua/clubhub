<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-left">
        <img src="https://img.icons8.com/clouds/200/000000/school.png" alt="Logo" class="logo"/>
        <h2>校园社团管理系统</h2>
        <p>Campus Club Management System</p>
        <p class="desc">高效 · 便捷 · 数字化</p>
      </div>

      <div class="register-right">
        <h3>学生注册</h3>
        <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="80px">
          
          <el-form-item label="用户名" prop="username">
            <el-input 
              v-model="registerForm.username" 
              placeholder="请输入用户名（用于登录）" 
              size="large">
            </el-input>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              placeholder="请输入密码" 
              size="large"
              show-password>
            </el-input>
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input 
              v-model="registerForm.confirmPassword" 
              type="password" 
              placeholder="请再次输入密码" 
              size="large"
              show-password>
            </el-input>
          </el-form-item>

          <el-form-item label="真实姓名" prop="realName">
            <el-input 
              v-model="registerForm.realName" 
              placeholder="请输入真实姓名" 
              size="large">
            </el-input>
          </el-form-item>

          <el-form-item label="学号" prop="studentId">
            <el-input 
              v-model="registerForm.studentId" 
              placeholder="请输入学号" 
              size="large">
            </el-input>
          </el-form-item>

          <el-form-item label="院系" prop="deptId">
            <el-select 
              v-model="registerForm.deptId" 
              placeholder="请选择院系" 
              size="large" 
              style="width: 100%">
              <el-option 
                v-for="dept in deptList" 
                :key="dept.deptId" 
                :label="dept.deptName" 
                :value="dept.deptId" 
              />
            </el-select>
          </el-form-item>

          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="registerForm.gender" size="large">
              <el-radio :label="1">男</el-radio>
              <el-radio :label="2">女</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input 
              v-model="registerForm.phone" 
              placeholder="请输入手机号" 
              size="large">
            </el-input>
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input 
              v-model="registerForm.email" 
              placeholder="请输入邮箱" 
              size="large">
            </el-input>
          </el-form-item>

          <el-button type="primary" class="register-btn" :loading="loading" @click="handleRegister" size="large">
            立即注册
          </el-button>
          
          <div class="tips">
            <span>已有账号？<a @click="goToLogin" class="link">立即登录</a></span>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)
const deptList = ref([])

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  studentId: '',
  deptId: null,
  gender: null,
  phone: '',
  email: ''
})

// 自定义验证规则
const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度至少6位'))
  } else {
    if (registerForm.confirmPassword !== '') {
      registerFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback()
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入有效的手机号'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback()
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('请输入有效的邮箱地址'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在2-20个字符', trigger: 'blur' }
  ],
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  deptId: [
    { required: true, message: '请选择院系', trigger: 'change' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ]
}

const loadDepts = async () => {
  try {
    const res = await request.get('/dept/list')
    deptList.value = res.data || []
  } catch (error) {
    console.error('加载院系失败', error)
  }
}

const handleRegister = () => {
  registerFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      
      // 准备注册数据（不包含confirmPassword）
      const registerData = {
        username: registerForm.username,
        password: registerForm.password,
        realName: registerForm.realName,
        studentId: registerForm.studentId,
        deptId: registerForm.deptId,
        gender: registerForm.gender,
        phone: registerForm.phone || null,
        email: registerForm.email || null
      }
      
      // 发送注册请求
      request.post('/auth/register', registerData).then(res => {
        ElMessage.success('注册成功！即将跳转到登录页面')
        
        // 3秒后跳转到登录页
        setTimeout(() => {
          router.push('/login')
        }, 1500)
      }).catch(err => {
        ElMessage.error(err.message || '注册失败')
      }).finally(() => {
        loading.value = false
      })
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}

onMounted(() => {
  loadDepts()
})
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  width: 900px;
  min-height: 600px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
  display: flex;
  overflow: hidden;
}

.register-left {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.logo {
  width: 150px;
  height: 150px;
  margin-bottom: 20px;
}

.register-left h2 {
  font-size: 24px;
  margin: 10px 0;
}

.register-left p {
  margin: 5px 0;
  opacity: 0.9;
}

.desc {
  font-size: 14px;
  margin-top: 20px;
}

.register-right {
  flex: 1.5;
  padding: 40px;
  overflow-y: auto;
  max-height: 80vh;
}

.register-right h3 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

.register-btn {
  width: 100%;
  margin-top: 20px;
}

.tips {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.link {
  color: #667eea;
  cursor: pointer;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

/* 优化表单滚动条 */
.register-right::-webkit-scrollbar {
  width: 6px;
}

.register-right::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.register-right::-webkit-scrollbar-track {
  background-color: rgba(0, 0, 0, 0.05);
}
</style>
