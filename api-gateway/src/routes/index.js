const express = require('express');
const authRoutes = require('./auth');
const pacienteRoutes = require('./paciente');
const consultaRoutes = require('./consulta');
const funcionarioRoutes = require('./funcionario');

const router = express.Router();

// Rotas específicas com middlewares de autenticação
router.use('/auth', authRoutes);
router.use('/funcionarios', funcionarioRoutes);
router.use('/pacientes', pacienteRoutes);
router.use('/consultas', consultaRoutes);
router.use('/agendamentos', consultaRoutes); // Alias para consultas

// Rota de informações da API
router.get('/', (req, res) => {
  res.json({
    service: 'Hospital Management API Gateway',
    version: '1.0.0',
    timestamp: new Date().toISOString(),
    endpoints: {
      auth: '/api/auth',
      funcionarios: '/api/funcionarios',
      pacientes: '/api/pacientes',
      consultas: '/api/consultas',
      agendamentos: '/api/agendamentos'
    }
  });
});

module.exports = router;