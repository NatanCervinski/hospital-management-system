// api-gateway/src/routes/funcionario-ops.js (VERSÃO CORRIGIDA E SIMPLIFICADA)

const express = require('express');
const { authenticateToken, requireFuncionario } = require('../middlewares/auth');
const ProxyService = require('../services/proxy');

const router = express.Router();

// 1. Crie uma única instância do proxy genérico, sem passar nenhuma configuração.
const proxy = ProxyService.createProxyMiddleware();

// 2. Defina a rota que você precisa. Para o cadastro, é a rota POST na raiz ('/').
//    Aplique os middlewares de segurança e, por último, o proxy.
router.post('/', authenticateToken, requireFuncionario, proxy);
router.get('/medicos', authenticateToken, requireFuncionario, proxy);
// Se você precisar de outras rotas como GET, PUT, etc., para '/funcionarios-ops',
// adicione-as aqui seguindo o mesmo padrão.

module.exports = router;
