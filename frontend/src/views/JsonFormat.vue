<template>
  <div class="page-container">
    <el-card class="main-card">
      <div slot="header" class="card-header">
        <el-button icon="el-icon-back" @click="goBack" circle></el-button>
        <h2>JSON格式化校验</h2>
        <div></div>
      </div>

      <div class="input-section">
        <el-input
          type="textarea"
          v-model="inputJson"
          placeholder="请输入JSON字符串..."
          :rows="10"
          maxlength="50000"
          show-word-limit
          class="json-input"
        ></el-input>
      </div>

      <div class="button-section">
        <el-button type="primary" @click="processJson('format')" :loading="loading">
          <i class="el-icon-magic-stick"></i> 格式化
        </el-button>
        <el-button type="success" @click="processJson('compress')" :loading="loading">
          <i class="el-icon-zoom-out"></i> 压缩
        </el-button>
        <el-button type="warning" @click="processJson('validate')" :loading="loading">
          <i class="el-icon-circle-check"></i> 校验
        </el-button>
        <el-button @click="clearAll" :loading="loading">
          <i class="el-icon-delete"></i> 清空
        </el-button>
      </div>

      <div class="result-section" v-if="result">
        <div class="result-header">
          <h3 :class="{'error-title': !isValid}">
            <i :class="isValid ? 'el-icon-success' : 'el-icon-error'"></i>
            {{ isValid ? '处理成功' : 'JSON格式错误' }}
          </h3>
          <div class="result-actions" v-if="isValid && actionType !== 'validate'">
            <el-button size="small" @click="copyResult">
              <i class="el-icon-document-copy"></i> 复制结果
            </el-button>
          </div>
        </div>
        
        <div v-if="errorMessage" class="error-message">
          <el-alert
            :title="errorMessage"
            type="error"
            :closable="false"
            show-icon
          ></el-alert>
        </div>

        <div v-if="isValid && actionType !== 'validate'" class="result-content">
          <pre><code>{{ result }}</code></pre>
        </div>

        <div v-if="isValid && actionType === 'validate'" class="validate-success">
          <el-alert
            title="JSON格式正确，语法有效"
            type="success"
            :closable="false"
            show-icon
          ></el-alert>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { processJson } from '@/api'

export default {
  name: 'JsonFormat',
  data() {
    return {
      inputJson: '',
      result: '',
      loading: false,
      isValid: true,
      errorMessage: '',
      actionType: ''
    }
  },
  methods: {
    goBack() {
      this.$router.push('/')
    },
    async processJson(action) {
      if (!this.inputJson.trim()) {
        this.$message.warning('请输入JSON内容')
        return
      }

      this.loading = true
      this.actionType = action
      try {
        const res = await processJson({
          json: this.inputJson,
          action: action
        })
        
        this.isValid = res.data.valid
        
        if (this.isValid) {
          this.result = res.data.result
          this.errorMessage = ''
          if (action === 'validate') {
            this.$message.success('JSON格式正确')
          } else {
            this.$message.success('处理成功')
          }
        } else {
          this.result = ''
          this.errorMessage = res.data.errorMessage
          this.$message.error('JSON格式错误')
        }
      } catch (error) {
        console.error(error)
        this.$message.error('处理失败，请稍后重试')
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
    },
    clearAll() {
      this.inputJson = ''
      this.result = ''
      this.isValid = true
      this.errorMessage = ''
      this.actionType = ''
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
  max-width: 900px;
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

.json-input >>> textarea {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
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
  flex-wrap: wrap;
  gap: 12px;
}

.result-header h3 {
  margin: 0;
  color: #67c23a;
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-header .error-title {
  color: #f56c6c;
}

.result-content {
  padding: 16px;
  background: #282c34;
  border-radius: 8px;
  overflow-x: auto;
}

.result-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.result-content code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
  color: #abb2bf;
}

.error-message {
  margin-bottom: 16px;
}

.validate-success {
  margin-top: 16px;
}

@media (max-width: 768px) {
  .button-section {
    flex-direction: column;
  }
  
  .result-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
