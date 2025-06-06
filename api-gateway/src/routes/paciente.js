const express = require('express');
const { authenticateToken, requirePaciente, optionalAuth } = require('../middlewares/auth');
const ProxyService = require('../services/proxy');

const router = express.Router();

// Rotas que podem ser acessadas por funcionários (sem restrição de tipo de usuário)
const funcionarioAccessRoutes = [
  '/search',
  '/list'
];

// Rotas públicas (não requerem autenticação)
const publicRoutes = [
  '/search-public'
];

// Middleware para verificar tipo de acesso necessário
router.use((req, res, next) => {
  // Rotas públicas
  if (publicRoutes.some(route => req.path.startsWith(route))) {
    return next();
  }
  
  // Rotas que funcionários podem acessar
  if (funcionarioAccessRoutes.some(route => req.path.startsWith(route))) {
    return authenticateToken(req, res, next);
  }
  
  // Demais rotas requerem autenticação como paciente
  return authenticateToken(req, res, (err) => {
    if (err) return next(err);
    
    // Verificar se é paciente ou funcionário (funcionário tem acesso total)
    if (req.user.tipo !== 'PACIENTE' && req.user.tipo !== 'FUNCIONARIO') {
      return res.status(403).json({
        error: 'Acesso restrito a pacientes ou funcionários',
        code: 'ACCESS_DENIED'
      });
    }
    
    next();
  });
});

// Middleware de logging específico para pacientes
router.use((req, res, next) => {
  if (req.user) {
    const userType = req.user.tipo === 'PACIENTE' ? '🏥' : '👨‍⚕️';
    console.log(`${userType} ${req.user.email} acessou: ${req.method} ${req.originalUrl}`);
  }
  next();
});

// Proxy para todas as rotas de paciente
router.use('/', ProxyService.createProxyMiddleware());

module.exports = router;