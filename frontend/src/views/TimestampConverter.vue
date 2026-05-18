<template>
  <div class="page-container">
    <el-card class="main-card">
      <div slot="header" class="card-header">
        <el-button icon="el-icon-back" @click="goBack" circle></el-button>
        <h2>时间戳转换</h2>
        <div></div>
      </div>

      <el-tabs v-model="activeTab" type="border-card" @tab-change="handleTabChange">
        <el-tab-pane label="时间戳转日期" name="timestamp_to_date">
          <div class="input-section">
            <el-input
              v-model="inputValue"
              placeholder="请输入时间戳（10位秒级或13位毫秒级），例如：1700000000 或 1700000000000"
              class="timestamp-input"
            >
              <el-button slot="append" @click="getCurrentTimestamp" :loading="loading">
                <i class="el-icon-time"></i> 获取当前
              </el-button>
            </el-input>
          </div>

          <div class="format-section">
            <span class="format-label">输出格式：</span>
            <el-select v-model="outputFormat" placeholder="选择日期格式" style="width: 220px">
              <el-option label="yyyy-MM-dd HH:mm:ss" value="yyyy-MM-dd HH:mm:ss"></el-option>
              <el-option label="yyyy/MM/dd HH:mm:ss" value="yyyy/MM/dd HH:mm:ss"></el-option>
              <el-option label="yyyy-MM-dd" value="yyyy-MM-dd"></el-option>
              <el-option label="yyyy/MM/dd" value="yyyy/MM/dd"></el-option>
              <el-option label="yyyy年MM月dd日 HH时mm分ss秒" value="yyyy年MM月dd日 HH时mm分ss秒"></el-option>
              <el-option label="yyyyMMddHHmmss" value="yyyyMMddHHmmss"></el-option>
            </el-select>
          </div>

          <div class="button-section">
            <el-button type="primary" @click="convertTimestampToDate" :loading="loading">
              <i class="el-icon-right"></i> 转换
            </el-button>
            <el-button @click="clearAll">
              <i class="el-icon-delete"></i> 清空
            </el-button>
          </div>
        </el-tab-pane>

        <el-tab-pane label="日期转时间戳" name="date_to_timestamp">
          <div class="input-section">
            <el-date-picker
              v-model="dateValue"
              type="datetime"
              placeholder="选择日期时间"
              format="yyyy-MM-dd HH:mm:ss"
              value-format="yyyy-MM-dd HH:mm:ss"
              style="width: 100%"
              :clearable="true"
            >
            </el-date-picker>
          </div>

          <div class="or-section">
            <span class="or-text">或手动输入日期：</span>
          </div>

          <div class="input-section">
            <el-input
              v-model="inputValue"
              placeholder="请输入日期，例如：2023-11-15 10:30:00 或 2023/11/15"
            >
              <el-button slot="append" @click="getCurrentTimestamp" :loading="loading">
                <i class="el-icon-time"></i> 当前
              </el-button>
            </el-input>
          </div>

          <div class="hint-section">
            <el-alert
              title="支持的日期格式：yyyy-MM-dd HH:mm:ss、yyyy-MM-dd、yyyy/MM/dd HH:mm:ss、yyyy/MM/dd、yyyyMMddHHmmss、yyyyMMdd"
              type="info"
              :closable="false"
              show-icon
            ></el-alert>
          </div>

          <div class="button-section">
            <el-button type="primary" @click="convertDateToTimestamp" :loading="loading">
              <i class="el-icon-right"></i> 转换
            </el-button>
            <el-button @click="clearAll">
              <i class="el-icon-delete"></i> 清空
            </el-button>
          </div>
        </el-tab-pane>

        <el-tab-pane label="当前时间戳" name="current">
          <div class="current-timestamp-section">
            <el-button type="primary" @click="getCurrentTimestamp" :loading="loading" size="large">
              <i class="el-icon-refresh"></i> 获取当前时间戳
            </el-button>
          </div>
        </el-tab-pane>
      </el-tabs>

      <div class="result-section" v-if="showResult">
        <div class="result-header">
          <h3>
            <i class="el-icon-success" style="color: #67c23a"></i>
            转换结果
          </h3>
          <div class="result-actions">
            <el-button size="small" @click="copyResult('result')" v-if="resultData.result">
              <i class="el-icon-document-copy"></i> 复制
            </el-button>
            <el-button size="small" @click="copyResult('timestamp_ms')" v-if="resultData.timestamp_ms">
              <i class="el-icon-document-copy"></i> 复制毫秒
            </el-button>
            <el-button size="small" @click="copyResult('timestamp_s')" v-if="resultData.timestamp_s">
              <i class="el-icon-document-copy"></i> 复制秒
            </el-button>
          </div>
        </div>

        <div class="result-content" v-if="resultData.result">
          <div class="result-item" v-if="activeTab === 'timestamp_to_date' || activeTab === 'current'">
            <span class="result-label">日期时间：</span>
            <span class="result-value highlight">{{ resultData.result }}</span>
          </div>
          <div class="result-item" v-if="resultData.unit">
            <span class="result-label">时间戳单位：</span>
            <span class="result-value">{{ resultData.unit }}</span>
          </div>
          <div class="result-item" v-if="resultData.timestamp_ms">
            <span class="result-label">毫秒时间戳：</span>
            <span class="result-value highlight" @click="copyResult('timestamp_ms')">{{ resultData.timestamp_ms }}</span>
          </div>
          <div class="result-item" v-if="resultData.timestamp_s">
            <span class="result-label">秒级时间戳：</span>
            <span class="result-value highlight" @click="copyResult('timestamp_s')">{{ resultData.timestamp_s }}</span>
          </div>
          <div class="result-item" v-if="resultData.format">
            <span class="result-label">输出格式：</span>
            <span class="result-value">{{ resultData.format }}</span>
          </div>
          <div class="result-item" v-if="resultData.matchedFormat">
            <span class="result-label">匹配格式：</span>
            <span class="result-value">{{ resultData.matchedFormat }}</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { processTimestamp } from '@/api'

export default {
  name: 'TimestampConverter',
  data() {
    return {
      activeTab: 'timestamp_to_date',
      inputValue: '',
      dateValue: '',
      outputFormat: 'yyyy-MM-dd HH:mm:ss',
      loading: false,
      showResult: false,
      resultData: {}
    }
  },
  methods: {
    goBack() {
      this.$router.push('/')
    },
    handleTabChange(tabName) {
      this.showResult = false
      this.resultData = {}
      if (tabName === 'current') {
        this.getCurrentTimestamp()
      }
    },
    async convertTimestampToDate() {
      if (!this.inputValue.trim()) {
        this.$message.warning('请输入时间戳')
        return
      }

      this.loading = true
      try {
        const res = await processTimestamp({
          input: this.inputValue.trim(),
          action: 'timestamp_to_date',
          format: this.outputFormat
        })
        
        this.resultData = res.data
        this.showResult = true
        this.$message.success('转换成功')
      } catch (error) {
        console.error(error)
        this.$message.error(error.message || '转换失败')
      } finally {
        this.loading = false
      }
    },
    async convertDateToTimestamp() {
      let input = this.dateValue || this.inputValue
      
      if (!input || (!this.dateValue && !this.inputValue.trim())) {
        this.$message.warning('请选择或输入日期')
        return
      }

      this.loading = true
      try {
        const res = await processTimestamp({
          input: input,
          action: 'date_to_timestamp',
          format: this.outputFormat
        })
        
        this.resultData = res.data
        this.showResult = true
        this.$message.success('转换成功')
      } catch (error) {
        console.error(error)
        this.$message.error(error.message || '转换失败')
      } finally {
        this.loading = false
      }
    },
    async getCurrentTimestamp() {
      this.loading = true
      try {
        const res = await processTimestamp({
          input: '',
          action: 'current',
          format: this.outputFormat
        })
        
        this.resultData = res.data
        this.showResult = true
        
        if (this.activeTab === 'timestamp_to_date') {
          this.inputValue = String(res.data.timestamp_ms)
        } else if (this.activeTab === 'date_to_timestamp') {
          this.dateValue = res.data.datetime
          this.inputValue = res.data.datetime
        }
        
        this.$message.success('获取成功')
      } catch (error) {
        console.error(error)
        this.$message.error(error.message || '获取失败')
      } finally {
        this.loading = false
      }
    },
    copyResult(key) {
      let value = ''
      if (key === 'result') {
        value = this.resultData.result
      } else if (key === 'timestamp_ms') {
        value = String(this.resultData.timestamp_ms)
      } else if (key === 'timestamp_s') {
        value = String(this.resultData.timestamp_s)
      }
      
      if (!value) return
      
      const textarea = document.createElement('textarea')
      textarea.value = value
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      this.$message.success('复制成功')
    },
    clearAll() {
      this.inputValue = ''
      this.dateValue = ''
      this.showResult = false
      this.resultData = {}
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
  align-items: flex-start;
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

.timestamp-input {
  font-family: 'Monaco', 'Menlo', monospace;
}

.input-section {
  margin-bottom: 20px;
}

.format-section {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.format-label {
  font-weight: 500;
  color: #606266;
}

.or-section {
  margin-bottom: 10px;
}

.or-text {
  color: #909399;
  font-size: 14px;
}

.hint-section {
  margin-bottom: 20px;
}

.current-timestamp-section {
  text-align: center;
  padding: 40px 0;
}

.button-section {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.result-section {
  margin-top: 20px;
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
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-actions {
  display: flex;
  gap: 8px;
}

.result-content {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.result-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
}

.result-item:last-child {
  margin-bottom: 0;
}

.result-label {
  font-weight: 500;
  color: #606266;
  min-width: 100px;
}

.result-value {
  color: #303133;
  font-family: 'Monaco', 'Menlo', monospace;
}

.result-value.highlight {
  background: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  color: #409eff;
  font-size: 16px;
  cursor: pointer;
}

.result-value.highlight:hover {
  background: #ecf5ff;
}

@media (max-width: 768px) {
  .button-section {
    flex-direction: column;
  }
  
  .result-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .format-section {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
