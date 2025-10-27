# 基础镜像
FROM openjdk:21-jdk-slim

ARG APP_HOME=/opt/smart-blog
ENV APP_HOME=${APP_HOME}
ENV BLOG_LOG_PATH=/var/log/smart-blog

WORKDIR ${APP_HOME}

# 复制主机jar包至镜像内，复制的目录需放置在 Dockerfile 文件同级目录下
COPY target/smart-blog-backend-0.0.1.jar app.jar

# 预创建默认日志目录，方便直接挂载
RUN mkdir -p ${BLOG_LOG_PATH}

# 对外暴露的端口号
EXPOSE 8888

# 容器启动执行命令
ENTRYPOINT ["sh", "-c", "mkdir -p \"$BLOG_LOG_PATH\" && exec java -jar \"$APP_HOME/app.jar\""]
