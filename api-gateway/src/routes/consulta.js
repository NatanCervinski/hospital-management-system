const express = require('express');
const { authenticateToken, requireFuncionario, requirePaciente } = require('../middlewares/auth');
const ProxyService = require('../services/proxy');

const router = express.Router();

// Rotas que requerem privilégios de funcionário
const funcionarioOnlyRoutes = [
  '/create',
  '/admin',
  '/manage'
];

// Rotas que requerem ser paciente
const pacienteOnlyRoutes = [
  '/schedule',
  '/my-appointments',
  '/cancel-patient'
];

// Rotas que podem ser acessadas por ambos (com autenticação)
const authenticatedRoutes = [
  '/search',
  '/available',
  '/details'
];

// Middleware para verificar permissões baseado na rota
router.use((req, res, next) => {
  // Verificar se é rota restrita a funcionários
  if (funcionarioOnlyRoutes.some(route => req.path.startsWith(route))) {
    return authenticateToken(req, res, (err) => {
      if (err) return next(err);
      return requireFuncionario(req, res, next);
    });
  }
  
  // Verificar se é rota restrita a pacientes
  if (pacienteOnlyRoutes.some(route => req.path.startsWith(route))) {
    return authenticateToken(req, res, (err) => {
      if (err) return next(err);
      return requirePaciente(req, res, next);
    });
  }
  
  // Para outras rotas autenticadas
  if (authenticatedRoutes.some(route => req.path.startsWith(route))) {
    return authenticateToken(req, res, next);
  }
  
  // Rotas gerais requerem autenticação
  return authenticateToken(req, res, next);
});

// Middleware de logging específico para consultas
router.use((req, res, next) => {
  if (req.user) {
    const userType = req.user.tipo === 'PACIENTE' ? '🏥' : '👨‍⚕️';
    console.log(`${userType} ${req.user.email} acessou consultas: ${req.method} ${req.originalUrl}`);
  }
  next();
});

// Proxy para todas as rotas de consulta
router.use('/', ProxyService.createProxyMiddleware());

module.exports = router;