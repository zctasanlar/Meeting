# syntax=docker/dockerfile:1.4

# -----------------------------------------------------
# STAGE 1: Builder (Derleme Ortamı)
# -----------------------------------------------------
FROM --platform=$BUILDPLATFORM maven:3.8.5-eclipse-temurin-17 AS builder
WORKDIR /workdir/server
# 1. pom.xml ve kaynak kod kopyalanır.
COPY pom.xml /workdir/server/pom.xml
RUN mvn dependency:go-offline
COPY src /workdir/server/src
# 2. Uygulama derlenir (mvn package daha uygun bir hedef)
RUN mvn package -DskipTests

# -----------------------------------------------------
# STAGE 2: Prepare Production (Katmanları Çıkarma)
# -----------------------------------------------------
FROM builder as prepare-production
WORKDIR /workdir/server
# jar-mode layertools ile .jar dosyasını açarak katmanları çıkarıyoruz.
# Bu, BOOT-INF/classes ve lib klasörlerini target/dependency dizinine çıkaracaktır.
RUN java -Djarmode=layertools -jar target/*.jar extract --destination target/dependency


# -----------------------------------------------------
# STAGE 3: Final Production Image (Çalışma Ortamı)
# -----------------------------------------------------
FROM eclipse-temurin:17-jre-focal

EXPOSE 8080
VOLUME /tmp
ARG DEPENDENCY=/workdir/server/target/dependency

# 1. Bağımlılıkları kopyala (En az değişen katman)
COPY --from=prepare-production ${DEPENDENCY}/BOOT-INF/lib /app/lib

# 2. Meta veriyi kopyala
COPY --from=prepare-production ${DEPENDENCY}/META-INF /app/META-INF

# 3. Uygulama sınıflarını kopyala (En sık değişen katman)
# Hata veren bu BOOT-INF/classes dizini artık kesinlikle var olacaktır.
COPY --from=prepare-production ${DEPENDENCY}/BOOT-INF/classes /app

# 4. Başlatıcı (Launcher) dosyalarını kopyala
COPY --from=prepare-production ${DEPENDENCY}/BOOT-INF/layers/spring-boot-loader /app/loader

# Entrypoint'i Spring Boot'un katmanlı yapısına uygun şekilde ayarla
# Dikkat: cp'ye ek olarak BOOT-INF/classes ve BOOT-INF/lib'i eklemiyoruz,
# çünkü Spring Boot Loader bunları otomatik olarak yükler.
# Sadece loader'ı ve main class'ı çalıştırıyoruz.

# Daha standart bir Spring Boot Loader Entrypoint kullanımı:
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-cp", "/app/loader", "org.springframework.boot.loader.JarLauncher"]

# -----------------------------------------------------
# DEV ENV (Geliştirme Ortamı) - Değiştirilmedi
# -----------------------------------------------------
FROM builder AS dev-envs
RUN <<EOF
apt-get update
apt-get install -y --no-install-recommends git
EOF

RUN <<EOF
useradd -s /bin/bash -m vscode
groupadd docker
usermod -aG docker vscode
EOF
COPY --from=gloursdocker/docker / /
CMD ["mvn", "spring-boot:run"]