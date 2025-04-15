FROM openjdk:21-jdk-slim

# Создаем группу и пользователя
RUN addgroup --system app && adduser --system --ingroup app app

# Переключаемся на нового пользователя
USER app

# Копируем собранный JAR файл в контейнер
COPY target/*.jar app.jar

# Запуск приложения
ENTRYPOINT ["java", "-jar", "/app.jar"]
