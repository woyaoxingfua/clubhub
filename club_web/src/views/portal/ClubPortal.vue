<template>
  <div class="club-portal-container">
    <!-- 顶部搜索栏 -->
    <div class="search-section">
      <h2>🏛️ 社团黄页</h2>
      <el-input 
        v-model="searchKeyword" 
        placeholder="搜索社团名称或简介..." 
        size="large"
        clearable
        @clear="handleSearch"
        @keyup.enter="handleSearch"
        class="search-input">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="handleSearch" type="primary">搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- 分类标签 -->
    <div class="category-section">
      <div class="category-title">社团分类</div>
      <div class="category-tabs">
        <el-tag 
          v-for="cat in categories" 
          :key="cat.value"
          :type="selectedCategory === cat.value ? 'primary' : 'info'"
          :effect="selectedCategory === cat.value ? 'dark' : 'plain'"
          size="large"
          class="category-tag"
          @click="selectCategory(cat.value)">
          {{ cat.label }} <span class="count">({{ cat.count }})</span>
        </el-tag>
      </div>
    </div>

    <!-- 社团卡片网格 -->
    <div class="clubs-section">
      <div class="section-header">
        <h3>
          <el-icon><Grid /></el-icon>
          <span>社团列表</span>
        </h3>
        <span class="result-count">共 {{ clubList.length }} 个社团</span>
      </div>
      
      <el-row :gutter="20" v-loading="loading">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="club in clubList" :key="club.clubId" style="margin-bottom: 20px">
          <el-card class="club-card" shadow="hover" @click="goToDetail(club.clubId)">
            <div class="club-logo">
              <img :src="club.logoUrl || 'https://img.icons8.com/clouds/200/000000/community.png'" :alt="club.clubName" />
            </div>
            <div class="club-info">
              <h3>{{ club.clubName }}</h3>
              <el-tag size="small" type="success">{{ club.category || '综合类' }}</el-tag>
              <p class="club-desc">{{ club.description || '暂无简介' }}</p>
              <div class="club-stats">
                <span>
                  <el-icon><User /></el-icon> 
                  {{ club.memberCount || 0 }} 名成员
                </span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <el-empty v-if="!loading && clubList.length === 0" description="暂无符合条件的社团" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '@/utils/request'
import { Search, User, Grid } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const allClubs = ref([])
const searchKeyword = ref('')
const selectedCategory = ref('全部')

const categories = computed(() => {
  const catMap = new Map()
  catMap.set('全部', 0)
  
  allClubs.value.forEach(club => {
    const cat = club.category || '综合类'
    catMap.set(cat, (catMap.get(cat) || 0) + 1)
    catMap.set('全部', catMap.get('全部') + 1)
  })
  
  return [
    { label: '全部', value: '全部', count: catMap.get('全部') || 0 },
    { label: '科技类', value: '科技类', count: catMap.get('科技类') || 0 },
    { label: '艺术类', value: '艺术类', count: catMap.get('艺术类') || 0 },
    { label: '体育类', value: '体育类', count: catMap.get('体育类') || 0 },
    { label: '公益类', value: '公益类', count: catMap.get('公益类') || 0 },
    { label: '综合类', value: '综合类', count: catMap.get('综合类') || 0 }
  ]
})

const clubList = computed(() => {
  let list = allClubs.value
  
  // 分类筛选
  if (selectedCategory.value && selectedCategory.value !== '全部') {
    list = list.filter(c => (c.category || '综合类') === selectedCategory.value)
  }
  
  // 关键词搜索
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(c => 
      (c.clubName && c.clubName.toLowerCase().includes(kw)) ||
      (c.description && c.description.toLowerCase().includes(kw))
    )
  }
  
  return list
})

const loadData = () => {
  loading.value = true
  request.get('/club/portal').then(res => {
    allClubs.value = res.data || []
  }).finally(() => {
    loading.value = false
  })
}

const selectCategory = (cat) => {
  selectedCategory.value = cat
}

const handleSearch = () => {
  // 搜索逻辑已在 computed 中实现
}

const goToDetail = (clubId) => {
  const inPortal = route.path.startsWith('/portal')
  const target = inPortal ? `/portal/club/${clubId}` : `/view/club/${clubId}`
  router.push(target)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.club-portal-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.search-section {
  background: white;
  padding: 25px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.search-section h2 {
  margin: 0 0 20px 0;
  font-size: 24px;
  color: #303133;
}

.search-input {
  max-width: 600px;
}

.category-section {
  background: white;
  padding: 20px 25px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.category-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 15px;
  font-weight: 500;
}

.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.category-tag {
  cursor: pointer;
  transition: all 0.3s;
  padding: 8px 16px;
  font-size: 14px;
}

.category-tag:hover {
  transform: translateY(-2px);
}

.category-tag .count {
  font-weight: bold;
  margin-left: 4px;
}

.clubs-section {
  background: white;
  padding: 20px 25px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-count {
  font-size: 14px;
  color: #909399;
}

.club-card {
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
  border-radius: 8px;
}

.club-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}

.club-logo {
  text-align: center;
  margin-bottom: 15px;
  padding: 10px 0;
}

.club-logo img {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #f0f0f0;
  transition: all 0.3s;
}

.club-card:hover .club-logo img {
  border-color: #409eff;
}

.club-info h3 {
  margin: 10px 0;
  font-size: 18px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 600;
}

.club-desc {
  color: #909399;
  font-size: 13px;
  margin: 10px 0;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.6;
}

.club-stats {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  font-size: 13px;
  color: #606266;
}

.club-stats span {
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: 500;
}

.club-stats .el-icon {
  color: #409eff;
}
</style>
