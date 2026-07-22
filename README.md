🌐 DevServer — локальный HTTP-сервер (разработка)
Быстрый и гибкий HTTP-сервер для локальной разработки с поддержкой маршрутизации, статических файлов, CORS, логирования и REST API.
Идеален для тестирования фронтенда, прототипирования бэкенда и отладки.
Реализован на 7 языках программирования для демонстрации подходов к созданию сетевых серверов.

https://img.shields.io/github/repo-size/yourname/devserver
https://img.shields.io/github/stars/yourname/devserver?style=social
https://img.shields.io/badge/License-MIT-blue.svg

🧠 Концепция
DevServer — это легковесный HTTP-сервер для разработки. Он позволяет:

✅ Отдавать статические файлы из указанной директории (HTML, CSS, JS, изображения).

✅ Поддерживать REST API — обработка GET, POST, PUT, DELETE запросов.

✅ Включать CORS для удобства работы с фронтендом.

✅ Логировать все запросы с временными метками и статусами.

✅ Настраивать порт и корневую папку через аргументы командной строки.

✅ Обрабатывать ошибки (404, 500) с информативными ответами.

✅ Поддерживать заголовки и параметры запроса.

✅ Быть кроссплатформенным — работает на Windows, Linux, macOS.

🚀 Как запустить
Каждая версия самодостаточна. Установите зависимости (если есть) и запустите сервер.

Python
bash
python server_python.py --port 8080 --root ./public
C++
bash
# Требуется библиотека httplib (cpp-httplib) — скачайте header
g++ -std=c++17 server_cpp.cpp -o server -pthread
./server --port 8080 --root ./public
Java
bash
# Требуется Java 8+ (стандартный HttpServer)
javac server_java.java && java server_java --port 8080 --root ./public
C# (.NET Core)
bash
dotnet new console -n DevServer -f net6.0
dotnet add package Microsoft.AspNetCore.App
dotnet run -- --port 8080 --root ./public
Go
bash
go run server_go.go --port 8080 --root ./public
Rust
bash
cargo build --release && ./target/release/server_rs --port 8080 --root ./public
JavaScript (Node.js)
bash
npm install
node server_js.js --port 8080 --root ./public
🧩 Пример использования
bash
$ python server_python.py --port 8000 --root ./static
🌐 DevServer запущен на http://localhost:8000
Корневая папка: ./static
[2025-01-15 10:00:00] GET /index.html -> 200
[2025-01-15 10:00:05] GET /api/users -> 200
[2025-01-15 10:00:12] POST /api/users -> 201
В браузере: http://localhost:8000 → отображается index.html из папки ./static.
API: GET /api/users возвращает JSON с пользователями.

📦 Содержимое репозитория
Файл	Язык	Особенности
server_python.py	Python	http.server, argparse, логирование, CORS, API
server_cpp.cpp	C++	cpp-httplib, многопоточность, CLI, CORS
server_java.java	Java	com.sun.net.httpserver, обработка статики, API
server_cs.cs	C#	Microsoft.AspNetCore, минимальный API, CORS
server_go.go	Go	net/http, горутины, обработка статики, API
server_rs.rs	Rust	hyper или actix-web (для простоты — hyper), CLI
server_js.js	JavaScript	Express, CORS, статика, логирование
🔮 Расширенные функции
Поддержка WebSocket (в планах).

Автоматическая перезагрузка при изменении файлов.

Сжатие Gzip для статики.

Виртуальные пути (алиасы).

📜 Лицензия
MIT — свободно используйте, модифицируйте и распространяйте.

🤝 Вклад
Приветствуются пул-реквесты с улучшениями, поддержкой новых платформ и расширением функциональности.
