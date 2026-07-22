// server_go.go — локальный HTTP-сервер (разработка) на Go

package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"time"
)

var rootDir string

func main() {
	port := flag.Int("port", 8080, "Порт")
	root := flag.String("root", ".", "Корневая папка")
	flag.Parse()
	rootDir = *root

	http.HandleFunc("/", staticHandler)
	http.HandleFunc("/api/users", apiHandler)

	// Middleware для логирования и CORS
	loggedHandler := loggingMiddleware(corsMiddleware(http.DefaultServeMux))
	server := &http.Server{
		Addr:         fmt.Sprintf(":%d", *port),
		Handler:      loggedHandler,
		ReadTimeout:  5 * time.Second,
		WriteTimeout: 10 * time.Second,
	}

	fmt.Printf("🌐 DevServer запущен на http://localhost:%d\n", *port)
	fmt.Printf("Корневая папка: %s\n", rootDir)
	fmt.Println("Нажмите Ctrl+C для остановки")
	log.Fatal(server.ListenAndServe())
}

func staticHandler(w http.ResponseWriter, r *http.Request) {
	path := r.URL.Path
	if path == "/" {
		path = "/index.html"
	}
	fullPath := filepath.Join(rootDir, path)
	info, err := os.Stat(fullPath)
	if err != nil || info.IsDir() {
		http.NotFound(w, r)
		return
	}
	http.ServeFile(w, r, fullPath)
}

func apiHandler(w http.ResponseWriter, r *http.Request) {
	switch r.Method {
	case "GET":
		users := []map[string]interface{}{{"id": 1, "name": "Alice"}, {"id": 2, "name": "Bob"}}
		json.NewEncoder(w).Encode(users)
	case "POST":
		w.WriteHeader(http.StatusCreated)
		fmt.Fprintf(w, `{"status":"created"}`)
	case "PUT":
		fmt.Fprintf(w, `{"status":"updated"}`)
	case "DELETE":
		w.WriteHeader(http.StatusNoContent)
	default:
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
	}
}

func corsMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type")
		if r.Method == "OPTIONS" {
			w.WriteHeader(http.StatusOK)
			return
		}
		next.ServeHTTP(w, r)
	})
}

func loggingMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		start := time.Now()
		// Обёртка для записи статуса
		lw := &loggingResponseWriter{ResponseWriter: w, statusCode: 200}
		next.ServeHTTP(lw, r)
		duration := time.Since(start)
		log.Printf("[%s] %s %s -> %d (%v)", start.Format("2006-01-02 15:04:05"), r.Method, r.URL.Path, lw.statusCode, duration)
	})
}

type loggingResponseWriter struct {
	http.ResponseWriter
	statusCode int
}

func (lw *loggingResponseWriter) WriteHeader(code int) {
	lw.statusCode = code
	lw.ResponseWriter.WriteHeader(code)
}
