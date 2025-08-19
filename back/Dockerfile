# 빌드 스테이지: alpine 제거 (arm64 지원 태그 사용)
FROM gradle:8.7-jdk17 AS build
WORKDIR /src
COPY . .
RUN gradle clean war --no-daemon

# 런타임 스테이지: Tomcat jdk17 (이 태그는 multi-arch)
FROM tomcat:9.0-jdk17-temurin
WORKDIR /usr/local/tomcat
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8 TZ=Asia/Seoul
RUN rm -rf webapps/*
COPY --from=build /src/build/libs/*.war webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]