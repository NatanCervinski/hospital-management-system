const express = require('express');
const ProxyService = require('../services/proxy');
const { optionalAuth } = require('../middlewares/auth');

const router = express.Router();

// Rotas públicas de autenticação (não requerem token)
const publicRoutes = [
  '/login',
  '/register/paciente',
  '/check-email',
  '/check-cpf',
  '/health'
];

// Middleware para determinar se a rota é pública
const checkPublicRoute = (req, res, next) => {
  const isPublic = publicRoutes.some(route => req.path === route || req.path.startsWith(route));
  
  if (isPublic) {
    return next();
  }
  
  // Para rotas protegidas, usar autenticação opcional
  return optionalAuth(req, res, next);
};

// Aplicar middleware de verificação
router.use(checkPublicRoute);

// Proxy para todas as rotas de autenticação
router.use('/', ProxyService.createProxyMiddleware());

module.exports = router;