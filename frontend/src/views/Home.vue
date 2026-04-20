<template>
  <div class="home-container">
    <header class="header">
      <h1 class="title">在线工具平台</h1>
      <p class="subtitle">简单、快捷、实用的在线工具集合</p>
    </header>

    <main class="tools-grid">
      <el-card
        v-for="tool in tools"
        :key="tool.id"
        class="tool-card"
        shadow="hover"
        @click.native="goToTool(tool.path)"
      >
        <div class="tool-icon">
          <i :class="tool.icon"></i>
        </div>
        <h3 class="tool-title">{{ tool.name }}</h3>
        <p class="tool-description">{{ tool.description }}</p>
      </el-card>
    </main>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      tools: [
        {
          id: 1,
          name: '英文大小写转换',
          description: '快速转换英文文本的大小写格式',
          icon: 'el-icon-edit',
          path: '/text-convert'
        },
        {
          id: 2,
          name: 'Word转PDF',
          description: '将Word文档转换为PDF格式',
          icon: 'el-icon-document',
          path: '/word-to-pdf'
        },
        {
          id: 3,
          name: '文本加解密',
          description: '支持Base64、MD5、AES等加解密算法',
          icon: 'el-icon-lock',
          path: '/crypto'
        },
        {
          id: 4,
          name: 'JSON格式化校验',
          description: 'JSON格式化、压缩、语法校验工具',
          icon: 'el-icon-data-analysis',
          path: '/json-format'
        }
      ]
    }
  },
  methods: {
    goToTool(path) {
      this.$router.push(path)
    }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  padding: 40px 20px;
}

.header {
  text-align: center;
  margin-bottom: 60px;
  color: #fff;
}

.title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 16px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.subtitle {
  font-size: 18px;
  opacity: 0.9;
}

.tools-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  padding: 0 20px;
}

.tool-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.tool-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.tool-icon {
  font-size: 48px;
  color: #667eea;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.tool-card:hover .tool-icon {
  transform: scale(1.1);
  color: #764ba2;
}

.tool-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.tool-description {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .title {
    font-size: 36px;
  }

  .tools-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}
</style>
