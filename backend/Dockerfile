# =============================================
# Backend Dockerfile — 多阶段构建
#
# 阶段 1: 使用 Maven 镜像编译打包
# 阶段 2: 使用 JDK 镜像运行
#
# 为什么要多阶段构建？
#   最终镜像只包含运行时的 JDK 和 jar 包，
#   不包含 Maven 和编译期依赖，体积减少约 80%
# =============================================

# ---- 阶段 1: 构建 ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 先复制 pom.xml，利用 Docker 缓存层
# 只要 pom.xml 不变，即使源码变了也不会重新下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并编译打包
COPY src ./src
RUN mvn package -DskipTests -B

# ---- 阶段 2: 运行 ----
FROM eclipse-temurin:17-jre

WORKDIR /app

# 从 builder 阶段复制构建好的 jar 包
COPY --from=builder /app/target/*.jar app.jar

# 暴露 8080 端口（Spring Boot 默认端口）
EXPOSE 8080

# 启动命令
# 使用 exec 格式（JSON 数组），确保能正确接收 SIGTERM 信号
# --spring.profiles.active=docker 可以指定 Docker 环境配置
ENTRYPOINT ["java", "-jar", "app.jar"]
