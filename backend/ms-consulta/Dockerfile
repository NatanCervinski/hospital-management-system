# Use uma imagem base oficial do OpenJDK com Alpine Linux para ser leve
FROM openjdk:21-jdk-slim

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o arquivo JAR de sua aplicação para o diretório de trabalho do contêiner
# Certifique-se de que o caminho do JAR está correto, baseado na saída do seu Maven build (target/)
# O nome do seu JAR é ms-consulta-0.0.1-SNAPSHOT.jar
COPY target/ms-consulta-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta em que sua aplicação Spring Boot está rodando
EXPOSE 8085

# Define o comando que será executado quando o contêiner for iniciado
ENTRYPOINT ["java", "-jar", "app.jar"]
