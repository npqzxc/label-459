<template>
  <div class="page-container">
    <el-card class="main-card">
      <div slot="header" class="card-header">
        <el-button icon="el-icon-back" @click="goBack" circle></el-button>
        <h2>英文大小写转换</h2>
        <div></div>
      </div>

      <div class="input-section">
        <el-input
          type="textarea"
          v-model="inputText"
          placeholder="请输入英文文本..."
          :rows="8"
          maxlength="10000"
          show-word-limit
        ></el-input>
      </div>

      <div class="button-section">
        <el-button type="primary" @click="convert('uppercase')" :loading="loading">
          转为大写
        </el-button>
        <el-button type="success" @click="convert('lowercase')" :loading="loading">
          转为小写
        </el-button>
        <el-button type="warning" @click="convert('titlecase')" :loading="loading">
          首字母大写
        </el-button>
      </div>

      <div class="result-section" v-if="result">
        <div class="result-header">
          <h3>转换结果</h3>
          <el-button size="small" @click="copyResult">
            <i class="el-icon-document-copy"></i> 复制结果
          </el-button>
        </div>
        <div class="result-content">
          {{ result }}
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { convertText } from '@/api'

export default {
  name: 'TextConvert',
  data() {
    return {
      inputText: '',
      result: '',
      loading: false
    }
  },
  methods: {
    goBack() {
      this.$router.push('/')
    },
    async convert(type) {
      if (!this.inputText.trim()) {
        this.$message.warning('请输入文本内容')
        return
      }

      this.loading = true
      try {
        const res = await convertText({
          text: this.inputText,
          type: type
        })
        this.result = res.data.result
        this.$message.success('转换成功')
      } catch (error) {
        console.error(error)
      } finally {
        this.loading = false
      }
    },
    copyResult() {
      const textarea = document.createElement('textarea')
      textarea.value = this.result
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      this.$message.success('复制成功')
    }
  }
}
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  padding: 40px 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.main-card {
  width: 100%;
  max-width: 800px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #333;
}

.input-section {
  margin-bottom: 20px;
}

.button-section {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.result-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.result-header h3 {
  margin: 0;
  color: #333;
}

.result-content {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  color: #333;
}
</style>
