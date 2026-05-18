<template>
  <div class="word-to-pdf-page">
    <div class="container">
      <!-- 返回按钮 -->
      <div class="back-button">
        <el-button icon="el-icon-back" @click="goBack" circle></el-button>
      </div>

      <!-- 标题 -->
      <h1 class="title">Word转PDF</h1>

      <!-- 上传区域 -->
      <div class="upload-container">
        <el-upload
          ref="wordUpload"
          class="upload-area"
          drag
          action="#"
          :before-upload="beforeUpload"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :on-exceed="handleExceed"
          :auto-upload="false"
          :limit="1"
          :file-list="fileList"
          accept=".doc,.docx"
        >
          <i class="el-icon-upload upload-icon"></i>
          <div class="upload-text">将Word文件拖到此处，或<em>点击上传</em></div>
          <div class="upload-tip">只支持 .doc 或 .docx 格式，文件大小不超过10MB</div>
        </el-upload>
      </div>

      <!-- 转换按钮 -->
      <div class="convert-button">
        <el-button
          type="primary"
          size="large"
          @click="convertFile"
          :loading="loading"
          :disabled="!selectedFile"
        >
          开始转换
        </el-button>
      </div>

      <!-- 结果区域 -->
      <div class="result-container" v-if="downloadUrl">
        <el-alert
          title="转换成功"
          type="success"
          :closable="false"
          show-icon
        ></el-alert>
        <div class="download-button">
          <el-button type="success" size="large" @click="downloadFile">
            <i class="el-icon-download"></i> 下载PDF文件
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { convertWordToPdf } from '@/api'

export default {
  name: 'WordToPdf',
  data() {
    return {
      selectedFile: null,
      fileList: [],
      loading: false,
      downloadUrl: ''
    }
  },
  methods: {
    goBack() {
      this.$router.push('/')
    },
    beforeUpload(file) {
      const lowerName = file.name.toLowerCase()
      const isWord = lowerName.endsWith('.doc') || lowerName.endsWith('.docx')
      const isWithin10M = file.size / 1024 / 1024 <= 10

      if (!isWord) {
        this.$message.error('只支持 .doc 或 .docx 格式的文件')
        return false
      }
      if (!isWithin10M) {
        this.$message.error('文件大小不能超过 10MB')
        return false
      }
      return true
    },
    handleFileChange(file, fileList) {
      this.selectedFile = file.raw
      this.fileList = fileList.slice(-1)
      this.downloadUrl = ''
    },
    handleFileRemove(file, fileList) {
      this.fileList = fileList
      this.selectedFile = fileList.length > 0 ? fileList[fileList.length - 1].raw : null
    },
    handleExceed() {
      this.$message.warning('当前已有文件，请先删除之前的文件再添加新文件')
    },
    resetUploadState() {
      this.selectedFile = null
      this.fileList = []
      if (this.$refs.wordUpload) {
        this.$refs.wordUpload.clearFiles()
      }
    },
    async convertFile() {
      if (!this.selectedFile) {
        this.$message.warning('请先选择文件')
        return
      }

      this.loading = true
      const formData = new FormData()
      formData.append('file', this.selectedFile)

      try {
        const res = await convertWordToPdf(formData)
        this.downloadUrl = res.data.downloadUrl
        this.$message.success('转换成功，可继续上传新文件')
        this.resetUploadState()
      } catch (error) {
        console.error(error)
      } finally {
        this.loading = false
      }
    },
    downloadFile() {
      window.open(this.downloadUrl, '_blank')
    }
  }
}
</script>

<style scoped>
.word-to-pdf-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.container {
  width: 100%;
  max-width: 700px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  position: relative;
}

.back-button {
  position: absolute;
  top: 20px;
  left: 20px;
}

.title {
  text-align: center;
  font-size: 32px;
  font-weight: 600;
  color: #333;
  margin: 0 0 40px 0;
}

.upload-container {
  margin: 0 auto 30px auto;
  max-width: 100%;
}

.upload-area {
  width: 100%;
}

.upload-icon {
  font-size: 67px;
  color: #C0C4CC;
  margin: 40px 0 16px;
}

.upload-text {
  color: #606266;
  font-size: 14px;
  text-align: center;
  margin-bottom: 12px;
}

.upload-text em {
  color: #409EFF;
  font-style: normal;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  text-align: center;
  margin-bottom: 20px;
}

.convert-button {
  text-align: center;
  margin-bottom: 20px;
}

.result-container {
  margin-top: 30px;
  padding-top: 30px;
  border-top: 2px solid #f0f0f0;
}

.download-button {
  text-align: center;
  margin-top: 20px;
}
</style>

<style>
/* 全局样式 - 确保 Element UI 上传组件居中 */
.upload-area .el-upload {
  width: 100%;
  display: block;
}

.upload-area .el-upload-dragger {
  width: 100%;
  height: auto;
  border: 2px dashed #d9d9d9;
  border-radius: 12px;
  background-color: #fafafa;
  transition: all 0.3s;
}

.upload-area .el-upload-dragger:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}
</style>
