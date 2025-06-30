const express = require('express');
const authRoutes = require('./auth');
const pacienteRoutes = require('./paciente');
const consultaRoutes = require('./consulta');
const funcionarioRoutes = require('./funcionario');
const ProxyService = require('../services/proxy');

const router = express.Router();

// Rotas específicas com middlewares de autenticação
router.use('/auth', authRoutes);
router.use('/funcionarios', funcionarioRoutes);
router.use('/pacientes', pacienteRoutes);
router.use('/consultas', consultaRoutes);
router.use('/agendamentos', consultaRoutes); // Alias para consultas

// Health check aggregation - checks MS Autenticacao, MS Paciente, and MS Consulta
router.get('/health', async (req, res) => {
  const healthResults = {
    gateway: {
      status: 'UP',
      timestamp: new Date().toISOString()
    },
    services: {}
  };

  // Check MS Autenticacao
  try {
    const authServiceConfig = ProxyService.getServiceConfig('/auth');
    const authResult = await ProxyService.forwardRequest(
      authServiceConfig.url,
      '/actuator/health',
      'GET',
      { 'Accept': 'application/json' },
      null,
      5000 // Shorter timeout for health checks
    );
    
    healthResults.services.autenticacao = {
      status: authResult.status === 200 ? 'UP' : 'DOWN',
      responseTime: Date.now(),
      details: authResult.data
    };
  } catch (error) {
    healthResults.services.autenticacao = {
      status: 'DOWN',
      error: error.message
    };
  }

  // Check MS Paciente
  try {
    const pacienteServiceConfig = ProxyService.getServiceConfig('/pacientes');
    const pacienteResult = await ProxyService.forwardRequest(
      pacienteServiceConfig.url,
      '/actuator/health',
      'GET',
      { 'Accept': 'application/json' },
      null,
      5000 // Shorter timeout for health checks
    );
    
    healthResults.services.paciente = {
      status: pacienteResult.status === 200 ? 'UP' : 'DOWN',
      responseTime: Date.now(),
      details: pacienteResult.data
    };
  } catch (error) {
    healthResults.services.paciente = {
      status: 'DOWN',
      error: error.message
    };
  }

  // Check MS Consulta
  try {
    const consultaServiceConfig = ProxyService.getServiceConfig('/consultas');
    const consultaResult = await ProxyService.forwardRequest(
      consultaServiceConfig.url,
      '/actuator/health',
      'GET',
      { 'Accept': 'application/json' },
      null,
      5000 // Shorter timeout for health checks
    );
    
    healthResults.services.consulta = {
      status: consultaResult.status === 200 ? 'UP' : 'DOWN',
      responseTime: Date.now(),
      details: consultaResult.data
    };
  } catch (error) {
    healthResults.services.consulta = {
      status: 'DOWN',
      error: error.message
    };
  }

  // Determine overall status
  const allServicesUp = Object.values(healthResults.services).every(service => service.status === 'UP');
  const overallStatus = allServicesUp ? 200 : 503;

  healthResults.overall = allServicesUp ? 'UP' : 'DEGRADED';

  res.status(overallStatus).json(healthResults);
});

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
      agendamentos: '/api/agendamentos',
      health: '/api/health'
    },
    public_endpoints: [
      '/api/auth/login',
      '/api/auth/register',
      '/api/pacientes/cadastro',
      '/api/health'
    ]
  });
});

module.exports = router;