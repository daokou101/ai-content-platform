# =============================================
# Frontend Dockerfile — 多阶段构建
#
# 阶段 1: 使用 Node 镜像构建 Vue 项目
# 阶段 2: 使用 Nginx 镜像运行（静态文件服务 + API 反向代理）
# =============================================

# ---- 阶段 1: 构建 ----
FROM node:20-alpine AS builder

WORKDIR /app

# 先复制 package.json 和 package-lock.json
# 利用 Docker 缓存避免每次构建都重新安装依赖
COPY package.json package-lock.json ./
RUN npm ci

# 复制源码并构建
COPY . .
RUN npm run build

# ---- 阶段 2: 运行 ----
FROM nginx:1.25-alpine

# 复制构建产物到 Nginx 的 HTML 目录
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制自定义 Nginx 配置文件
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露 80 端口
EXPOSE 80

# Nginx 默认的 CMD 就是启动 nginx，不需要额外指定
