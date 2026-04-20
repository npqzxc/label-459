<template>
  <div class="page-container">
    <el-card class="main-card">
      <div slot="header" class="card-header">
        <el-button icon="el-icon-back" @click="goBack" circle></el-button>
        <h2>文本加解密</h2>
        <div></div>
      </div>

      <div class="algorithm-section">
        <el-radio-group v-model="algorithm" @change="onAlgorithmChange">
          <el-radio label="base64">Base64</el-radio>
          <el-radio label="md5">MD5</el-radio>
          <el-radio label="aes">AES</el-radio>
        </el-radio-group>
      </div>

      <div class="input-section">
        <el-input
          type="textarea"
          v-model="inputText"
          placeholder="请输入要处理的文本..."
          :rows="6"
          maxlength="10000"
          show-word-limit
        ></el-input>
      </div>

      <div class="key-section" v-if="algorithm === 'aes'">
        <el-input
          v-model="aesKey"
          placeholder="请输入AES密钥（留空则自动填充）"
          clearable
        >
          <template slot="prepend">密钥</template>
        </el-input>
      </div>

      <div class="button-section">
        <template v-if="algorithm === 'base64'">
          <el-button type="primary" @click="process('encode')" :loading="loading">
            编码
          </el-button>
          <el-button type="success" @click="process('decode')" :loading="loading">
            解码
          </el-button>
        </template>
        <template v-else-if="algorithm === 'md5'">
          <el-button type="primary" @click="process('encrypt')" :loading="loading">
            加密
          </el-button>
        </template>
        <template v-else-if="algorithm === 'aes'">
          <el-button type="primary" @click="process('encrypt')" :loading="loading">
            加密
          </el-button>
          <el-button type="success" @click="process('decrypt')" :loading="loading">
            解密
          </el-button>
        </template>
      </div>

      <div class="result-section" v-if="result">
        <div class="result-header">
          <h3>处理结果</h3>
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
import { processCrypto } from '@/api'

export default {
  name: 'Crypto',
  data() {
    return {
      algorithm: 'base64',
      inputText: '',
      aesKey: '',
      result: '',
      loading: false
    }
  },
  methods: {
    goBack() {
      this.$router.push('/')
    },
    onAlgorithmChange() {
      this.result = ''
    },
    async process(action) {
      if (!this.inputText.trim()) {
        this.$message.warning('请输入文本内容')
        return
      }

      if (this.algorithm === 'aes' && !this.aesKey.trim()) {
        this.$message.warning('请输入AES密钥')
        return
      }

      this.loading = true
      try {
        const res = await processCrypto({
          text: this.inputText,
          algorithm: this.algorithm,
          action: action,
          key: this.aesKey
        })
        this.result = res.data.result
        this.$message.success('处理成功')
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

.algorithm-section {
  margin-bottom: 20px;
  text-align: center;
}

.input-section {
  margin-bottom: 20px;
}

.key-section {
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
  word-break: break-all;
  line-height: 1.6;
  color: #333;
}
</style>
