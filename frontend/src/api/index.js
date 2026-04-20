import request from './request'

export function convertText(data) {
  return request({
    url: '/text/convert',
    method: 'post',
    data
  })
}

export function convertWordToPdf(formData) {
  return request({
    url: '/word/convert',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function processCrypto(data) {
  return request({
    url: '/crypto/process',
    method: 'post',
    data
  })
}

export function processJson(data) {
  return request({
    url: '/json/process',
    method: 'post',
    data
  })
}
