# server_python.py — локальный HTTP-сервер (разработка) на Python

import http.server
import socketserver
import os
import argparse
import json
import datetime
from urllib.parse import parse_qs, urlparse

class DevHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=server_root, **kwargs)

    def do_GET(self):
        parsed = urlparse(self.path)
        path = parsed.path
        if path.startswith('/api/'):
            self.handle_api(path, 'GET')
        else:
            super().do_GET()
        self.log_request(self.send_response_only(200, "OK"))

    def do_POST(self):
        if self.path.startswith('/api/'):
            content_length = int(self.headers.get('Content-Length', 0))
            body = self.rfile.read(content_length).decode('utf-8')
            self.handle_api(self.path, 'POST', body)
        else:
            self.send_error(404, "Not found")

    def do_PUT(self):
        if self.path.startswith('/api/'):
            content_length = int(self.headers.get('Content-Length', 0))
            body = self.rfile.read(content_length).decode('utf-8')
            self.handle_api(self.path, 'PUT', body)
        else:
            self.send_error(404, "Not found")

    def do_DELETE(self):
        if self.path.startswith('/api/'):
            self.handle_api(self.path, 'DELETE')
        else:
            self.send_error(404, "Not found")

    def handle_api(self, path, method, body=None):
        # Простое API: /api/users
        if path == '/api/users':
            if method == 'GET':
                self.send_response(200)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                users = [{"id": 1, "name": "Alice"}, {"id": 2, "name": "Bob"}]
                self.wfile.write(json.dumps(users).encode())
            elif method == 'POST':
                self.send_response(201)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                self.wfile.write(json.dumps({"status": "created", "data": body}).encode())
            elif method == 'PUT':
                self.send_response(200)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                self.wfile.write(json.dumps({"status": "updated", "data": body}).encode())
            elif method == 'DELETE':
                self.send_response(204)
                self.end_headers()
        else:
            self.send_error(404, "API endpoint not found")

    def log_request(self, code='-', size='-'):
        timestamp = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        print(f"[{timestamp}] {self.address_string()} {self.command} {self.path} -> {code}")

def run(port, root):
    global server_root
    server_root = root
    os.chdir(root)
    handler = DevHTTPRequestHandler
    with socketserver.TCPServer(("", port), handler) as httpd:
        print(f"🌐 DevServer запущен на http://localhost:{port}")
        print(f"Корневая папка: {root}")
        print("Нажмите Ctrl+C для остановки")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n🛑 Сервер остановлен.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Локальный HTTP-сервер")
    parser.add_argument("--port", type=int, default=8000, help="Порт (по умолчанию 8000)")
    parser.add_argument("--root", default=".", help="Корневая папка для статики")
    args = parser.parse_args()
    run(args.port, args.root)
