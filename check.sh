#!/bin/bash
# check.sh — применяет форматирование, проверяет линтеры и запускает тесты

set -e

echo "1/4: Применяем Spotless..."
mvn spotless:apply

echo "2/4: Запуск Modernizer..."
mvn clean compile -am modernizer:modernizer

echo "3/4: Тесты в проекте bot..."
cd bot
mvn test
cd ..

echo "4/4: Тесты в проекте scrapper..."
cd scrapper
mvn test
cd ..

echo "Все проверки завершены успешно!"
