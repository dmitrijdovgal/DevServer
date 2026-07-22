// server_js.js — локальный HTTP-сервер (разработка) на JavaScript (Node.js + Express)

const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs');

const args = process.argv.slice(2);
let port = 8080;
let root = '.';
for (let i = 0; i < args.length; i++) {
    if (args[i] === '--port' && i+1 < args.length) port = parseInt(args[++i]);
    else if (args[i] === '--root' && i+1 < args.length) root = args[++i];
}

const app = express();

// CORS
app.use(cors());
app.use(express.json());

// Логирование
app.use((req, res, next) => {
    const start = Date.now();
    res.on('finish', () => {
        const duration = Date.now() - start;
        console.log(`[${new Date().toISOString()}] ${req.method} ${req.url} -> ${res.statusCode} (${duration}ms)`);
    });
    next();
});

// Статика
app.use(express.static(root));

// API
app.get('/api/users', (req, res) => {
    res.json([{id:1, name:'Alice'}, {id:2, name:'Bob'}]);
});
app.post('/api/users', (req, res) => {
    res.status(201).json({status:'created'});
});
app.put('/api/users', (req, res) => {
    res.json({status:'updated'});
});
app.delete('/api/users', (req, res) => {
    res.status(204).send();
});

// Обработка 404
app.use((req, res) => {
    res.status(404).send('404 Not Found');
});

app.listen(port, () => {
    console.log(`🌐 DevServer запущен на http://localhost:${port}`);
    console.log(`Корневая папка: ${root}`);
    console.log('Нажмите Ctrl+C для остановки');
});
