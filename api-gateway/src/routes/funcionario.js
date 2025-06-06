const express = require('express');
const { authenticateToken, requireFuncionario } = require('../middlewares/auth');
const ProxyService = require('../services/proxy');

const router = express.Router();

// Todas as rotas de funcionário requerem autenticação e privilégios de funcionário
router.use(authenticateToken);
router.use(requireFuncionario);

// Middleware de logging específico para funcionários
router.use((req, res, next) => {
  console.log(`👨‍⚕️ Funcionário ${req.user.email} acessou: ${req.method} ${req.originalUrl}`);
  next();
});

// Proxy para todas as rotas de funcionário
router.use('/', ProxyService.createProxyMiddleware());

module.exports = router;