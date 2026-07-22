// server_rs.rs — локальный HTTP-сервер (разработка) на Rust (hyper)

use hyper::service::{make_service_fn, service_fn};
use hyper::{Body, Request, Response, Server, StatusCode};
use hyper::body::Buf;
use std::convert::Infallible;
use std::net::SocketAddr;
use std::path::Path;
use tokio::fs;
use tokio::io::AsyncReadExt;
use std::time::SystemTime;
use std::collections::HashMap;
use std::env;

async fn handle_request(req: Request<Body>) -> Result<Response<Body>, Infallible> {
    let path = req.uri().path().to_string();
    let method = req.method().clone();

    // CORS
    let mut resp = Response::builder()
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        .header("Access-Control-Allow-Headers", "Content-Type");

    if method == hyper::Method::OPTIONS {
        return Ok(resp.status(StatusCode::OK).body(Body::empty()).unwrap());
    }

    // API
    if path.starts_with("/api/") {
        if path == "/api/users" {
            match method {
                hyper::Method::GET => {
                    let body = r#"[{"id":1,"name":"Alice"},{"id":2,"name":"Bob"}]"#;
                    return Ok(resp.status(StatusCode::OK)
                        .header("Content-Type", "application/json")
                        .body(Body::from(body)).unwrap());
                }
                hyper::Method::POST => {
                    return Ok(resp.status(StatusCode::CREATED)
                        .header("Content-Type", "application/json")
                        .body(Body::from(r#"{"status":"created"}"#)).unwrap());
                }
                hyper::Method::PUT => {
                    return Ok(resp.status(StatusCode::OK)
                        .header("Content-Type", "application/json")
                        .body(Body::from(r#"{"status":"updated"}"#)).unwrap());
                }
                hyper::Method::DELETE => {
                    return Ok(resp.status(StatusCode::NO_CONTENT).body(Body::empty()).unwrap());
                }
                _ => {}
            }
        }
        return Ok(resp.status(StatusCode::NOT_FOUND)
            .body(Body::from("API endpoint not found")).unwrap());
    }

    // Статика
    let root = env::var("SERVER_ROOT").unwrap_or(".".to_string());
    let file_path = if path == "/" { "/index.html" } else { &path };
    let full_path = format!("{}{}", root, file_path);
    match tokio::fs::read(&full_path).await {
        Ok(content) => {
            let mime = mime_guess::from_path(&full_path).first_or_octet_stream();
            Ok(resp.status(StatusCode::OK)
                .header("Content-Type", mime.as_ref())
                .body(Body::from(content)).unwrap())
        }
        Err(_) => {
            Ok(resp.status(StatusCode::NOT_FOUND)
                .body(Body::from("404 Not Found")).unwrap())
        }
    }
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let args: Vec<String> = env::args().collect();
    let mut port = 8080;
    let mut root = ".".to_string();
    for i in 1..args.len() {
        if args[i] == "--port" && i+1 < args.len() {
            port = args[i+1].parse().unwrap_or(8080);
        } else if args[i] == "--root" && i+1 < args.len() {
            root = args[i+1].clone();
        }
    }
    env::set_var("SERVER_ROOT", root);

    let addr = SocketAddr::from(([0, 0, 0, 0], port));
    let make_svc = make_service_fn(|_conn| {
        async {
            Ok::<_, Infallible>(service_fn(handle_request))
        }
    });

    let server = Server::bind(&addr).serve(make_svc);
    println!("🌐 DevServer запущен на http://localhost:{}", port);
    println!("Корневая папка: {}", root);
    println!("Нажмите Ctrl+C для остановки");
    server.await?;
    Ok(())
}
