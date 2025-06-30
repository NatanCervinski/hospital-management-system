const express = require('express');
const { authenticateToken, requireFuncionario, requirePaciente } = require('../middlewares/auth');
const ProxyService = require('../services/proxy');

const router = express.Router();

// ========== EMPLOYEE-ONLY ROUTES (FUNCIONARIO) ==========
// Based on ConsultaController.java endpoints with @PreAuthorize("hasRole('FUNCIONARIO')")

// Consultation management endpoints

router.post('/', authenticateToken, requireFuncionario, ProxyService.createProxyMiddleware());
router.get('/dashboard', authenticateToken, requireFuncionario, ProxyService.createProxyMiddleware());
router.put('/:consultaId/cancelar', authenticateToken, requireFuncionario, ProxyService.createProxyMiddleware());
router.put('/:consultaId/realizar', authenticateToken, requireFuncionario, ProxyService.createProxyMiddleware());
router.put('/agendamento/confirmar', authenticateToken, requireFuncionario, ProxyService.createProxyMiddleware());

// ========== PATIENT-ONLY ROUTES (PACIENTE) ==========

router.post('/consulta/:consultaId', authenticateToken, requirePaciente, ProxyService.createProxyMiddleware());
router.put('/:agendamentoId/cancelar', authenticateToken, requirePaciente, ProxyService.createProxyMiddleware());
router.put('/:agendamentoId/checkin', authenticateToken, requirePaciente, ProxyService.createProxyMiddleware());
router.get('/paciente', authenticateToken, requirePaciente, ProxyService.createProxyMiddleware());
// ========== PUBLIC ROUTES (NO AUTH REQUIRED) ==========
// Based on ConsultaController.java search endpoints without @PreAuthorize

// Consultation search endpoints - accessible by both patients and employees
router.get('/buscar', authenticateToken, ProxyService.createProxyMiddleware()); // GET /consultas/buscar - Search available consultations (R05)
router.get('/buscar/especialidade/:especialidade', authenticateToken, ProxyService.createProxyMiddleware()); // GET /consultas/buscar/especialidade/{especialidade} - Search by specialty (R05)
router.get('/buscar/medico', authenticateToken, ProxyService.createProxyMiddleware()); // GET /consultas/buscar/medico?medico=name - Search by doctor (R05)

// Middleware de logging específico para consultas
router.use((req, res, next) => {
  if (req.user) {
    const userType = req.user.tipo === 'PACIENTE' ? '🏥' : '👨‍⚕️';
    console.log(`${userType} ${req.user.email} acessou consultas: ${req.method} ${req.originalUrl}`);
  }
  next();
});

module.exports = router;
