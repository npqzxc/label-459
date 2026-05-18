# 构建阶段
FROM maven:3.8-eclipse-temurin-8 AS build
WORKDIR /app

# 复制pom.xml并下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段 - 使用 Java 8 并安装字体以降低乱码概率
FROM eclipse-temurin:8-jre
WORKDIR /app

# 安装中文字体支持（增加 Noto CJK，覆盖更多字符）
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    fontconfig \
    fonts-noto-cjk \
    fonts-wqy-zenhei \
    fonts-wqy-microhei \
    fonts-dejavu \
    && fc-cache -fv \
    && rm -rf /var/lib/apt/lists/*

# 创建临时文件目录
RUN mkdir -p /tmp/uploads

# 复制jar文件
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
