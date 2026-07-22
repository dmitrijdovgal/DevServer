// server_cpp.cpp — локальный HTTP-сервер (разработка) на C++ (cpp-httplib)

#include <httplib.h>
#include <iostream>
#include <fstream>
#include <sstream>
#include <filesystem>
#include <chrono>
#include <iomanip>
#include <ctime>

using namespace httplib;
namespace fs = std::filesystem;

std::string read_file(const std::string& path) {
    std::ifstream file(path, std::ios::binary);
    if (!file) return "";
    std::stringstream ss;
    ss << file.rdbuf();
    return ss.str();
}

std::string get_mime(const std::string& path) {
    if (path.ends_with(".html")) return "text/html";
    if (path.ends_with(".css")) return "text/css";
    if (path.ends_with(".js")) return "application/javascript";
    if (path.ends_with(".png")) return "image/png";
    if (path.ends_with(".jpg") || path.ends_with(".jpeg")) return "image/jpeg";
    return "application/octet-stream";
}

int main(int argc, char* argv[]) {
    int port = 8080;
    std::string root = ".";
    for (int i = 1; i < argc; ++i) {
        std::string arg = argv[i];
        if (arg == "--port" && i+1 < argc) port = std::stoi(argv[++i]);
        else if (arg == "--root" && i+1 < argc) root = argv[++i];
    }

    Server svr;

    // Логирование
    auto log = [](const Request& req, const Response& res) {
        auto now = std::chrono::system_clock::now();
        auto time = std::chrono::system_clock::to_time_t(now);
        std::cout << "[" << std::put_time(std::localtime(&time), "%Y-%m-%d %H:%M:%S")
                  << "] " << req.method << " " << req.path << " -> " << res.status << std::endl;
    };
    svr.set_logger(log);

    // Статика
    svr.Get("/.*", [&](const Request& req, Response& res) {
        std::string path = root + req.path;
        if (req.path == "/") path = root + "/index.html";
        if (fs::exists(path) && fs::is_regular_file(path)) {
            std::string content = read_file(path);
            if (!content.empty()) {
                res.set_content(content, get_mime(path));
                res.status = 200;
                return;
            }
        }
        res.status = 404;
        res.set_content("404 Not Found", "text/plain");
    });

    // API: /api/users
    svr.Get("/api/users", [](const Request& req, Response& res) {
        res.set_content(R"([{"id":1,"name":"Alice"},{"id":2,"name":"Bob"}])", "application/json");
    });
    svr.Post("/api/users", [](const Request& req, Response& res) {
        res.set_content(R"({"status":"created"})", "application/json");
        res.status = 201;
    });
    svr.Put("/api/users", [](const Request& req, Response& res) {
        res.set_content(R"({"status":"updated"})", "application/json");
    });
    svr.Delete("/api/users", [](const Request& req, Response& res) {
        res.status = 204;
    });

    // CORS
    svr.Options("/.*", [](const Request& req, Response& res) {
        res.set_header("Access-Control-Allow-Origin", "*");
        res.set_header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        res.set_header("Access-Control-Allow-Headers", "Content-Type");
    });
    svr.set_pre_routing_handler([](const Request& req, Response& res) {
        res.set_header("Access-Control-Allow-Origin", "*");
        return Server::HandlerResponse::Next;
    });

    std::cout << "🌐 DevServer запущен на http://localhost:" << port << std::endl;
    std::cout << "Корневая папка: " << root << std::endl;
    std::cout << "Нажмите Ctrl+C для остановки" << std::endl;
    svr.listen("0.0.0.0", port);
    return 0;
}
