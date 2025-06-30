const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const rateLimit = require('express-rate-limit');
require('dotenv').config();

const authMiddleware = require('./middlewares/auth');
const errorHandler = require('./middlewares/errorHandler');
const router = require('./routes');
const config = require('./config');

const app = express();

// Security middleware
app.use(helmet());

// CORS configuration
const corsOptions = {
  origin: config.cors.origin,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'],
  allowedHeaders: ['Content-Type', 'Authorization'],
  credentials: true
};
app.use(cors(corsOptions));

// Rate limiting
const limiter = rateLimit({
  windowMs: config.rateLimit.windowMs,
  max: config.rateLimit.maxRequests,
  message: {
    error: 'Muitas requisições realizadas. Tente novamente em alguns minutos.',
    code: 'RATE_LIMIT_EXCEEDED'
  }
});
app.use(limiter);

// Logging
app.use(morgan('combined'));

// Body parsing
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// Health check endpoint
app.get('/health', (req, res) => {
  res.status(200).json({
    status: 'UP',
    service: 'API Gateway',
    timestamp: new Date().toISOString(),
    version: process.env.npm_package_version || '1.0.0'
  });
});

// API routes
app.use('/api', router);

// 404 handler
app.use('*', (req, res) => {
  res.status(404).json({
    error: 'Endpoint não encontrado',
    code: 'ENDPOINT_NOT_FOUND',
    path: req.originalUrl
  });
});

// Error handling middleware
app.use(errorHandler);

const PORT = config.port || 3000;

app.listen(PORT, () => {
  console.log(`🚀 API Gateway rodando na porta ${PORT}`);
  console.log(`📊 Health check disponível em: http://localhost:${PORT}/health`);
  console.log(`🔒 JWT Secret configurado: ${config.jwt.secret ? 'Sim' : 'Não'}`);
  console.log(`🌐 CORS habilitado para: ${config.cors.origin}`);
});
