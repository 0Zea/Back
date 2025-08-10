# Spring Legacy 애플리케이션을 위한 Dockerfile
FROM openjdk:17-jdk-slim

# 메타데이터
LABEL maintainer="OZEA Team"
LABEL version="1.0.0"
LABEL description="OZEA Spring Legacy Application"

# 작업 디렉토리 설정
WORKDIR /app

# 시스템 패키지 업데이트 및 필요한 도구 설치
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

# 애플리케이션 JAR 파일 복사
COPY build/libs/*.war /app/app.war

# 로그 디렉토리 생성
RUN mkdir -p /app/logs

# 환경 변수 설정
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE="production"
ENV SERVER_PORT=8080

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/monitoring/health || exit 1

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.war"] 