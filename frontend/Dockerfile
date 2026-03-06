# 构建阶段
FROM node:16-alpine AS builder
WORKDIR /app

# 复制package.json并安装依赖
COPY package*.json ./
RUN npm config set registry https://registry.npmmirror.com && \
    npm install

# 复制源代码并构建
COPY . .
RUN npm run build

# 生产阶段
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# 删除默认的nginx内容
RUN rm -rf ./*

# 复制构建产物
COPY --from=builder /app/dist .

# 复制nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
