const jwt = require('jsonwebtoken');
const axios = require('axios');
const config = require('../config');

/**
 * Middleware para validar JWT tokens
 * Verifica se o token é válido e não está na blacklist do Redis
 */
const authenticateToken = async (req, res, next) => {
  try {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1]; // Bearer TOKEN

    if (!token) {
      console.log('Token de acesso requerido');
      return res.status(401).json({
        error: 'Token de acesso requerido',
        code: 'TOKEN_REQUIRED'
      });
    }

    // Verificar se o token está válido localmente
    let decoded;
    try {
      decoded = jwt.verify(token, config.jwt.secret);
    } catch (err) {
      return res.status(403).json({
        error: 'Token inválido ou expirado',
        code: 'INVALID_TOKEN'
      });
    }

    // Verificar se o token não está na blacklist (via MS de Autenticação)
    try {
      const verifyResponse = await axios.get(
        `${config.microservices.autenticacao.url}/api/auth/verify`,
        {
          headers: { Authorization: `Bearer ${token}` },
          timeout: config.microservices.autenticacao.timeout
        }
      );

      if (verifyResponse.status !== 200) {
        return res.status(403).json({
          error: 'Token não autorizado',
          code: 'TOKEN_UNAUTHORIZED'
        });
      }
    } catch (error) {
      if (error.response?.status === 401 || error.response?.status === 403) {
        return res.status(403).json({
          error: 'Token não autorizado ou na blacklist',
          code: 'TOKEN_BLACKLISTED'
        });
      }

      // Se o MS de autenticação estiver indisponível, usar apenas validação local
      console.warn('MS Autenticação indisponível, usando apenas validação local');
    }

    // Adicionar informações do usuário ao request
    req.user = {
      id: decoded.id,
      email: decoded.email,
      tipo: decoded.tipo, // PACIENTE ou FUNCIONARIO
      exp: decoded.exp
    };

    next();
  } catch (error) {
    console.error('Erro na autenticação:', error);
    return res.status(500).json({
      error: 'Erro interno do servidor durante autenticação',
      code: 'AUTH_INTERNAL_ERROR'
    });
  }
};

/**
 * Middleware para verificar se o usuário é um funcionário
 */
const requireFuncionario = (req, res, next) => {
  if (!req.user) {
    return res.status(401).json({
      error: 'Token de acesso requerido',
      code: 'TOKEN_REQUIRED'
    });
  }

  if (req.user.tipo !== 'FUNCIONARIO') {
    console.error(`AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAcesso negado: usuário ${req.user.email} não é funcionário`);
    return res.status(403).json({
      error: 'Acesso restrito a funcionários',
      code: 'FUNCIONARIO_REQUIRED'
    });
  }

  next();
};

/**
 * Middleware para verificar se o usuário é um paciente
 */
const requirePaciente = (req, res, next) => {
  if (!req.user) {
    return res.status(401).json({
      error: 'Token de acesso requerido',
      code: 'TOKEN_REQUIRED'
    });
  }

  if (req.user.tipo !== 'PACIENTE') {
    return res.status(403).json({
      error: 'Acesso restrito a pacientes',
      code: 'PACIENTE_REQUIRED'
    });
  }

  next();
};

/**
 * Middleware opcional de autenticação (não falha se não houver token)
 */
const optionalAuth = async (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return next();
  }

  try {
    const decoded = jwt.verify(token, config.jwt.secret);
    req.user = {
      id: decoded.id,
      email: decoded.email,
      tipo: decoded.tipo,
      exp: decoded.exp
    };
  } catch (err) {
    // Token inválido, mas não bloqueamos a requisição
    console.warn('Token inválido em auth opcional:', err.message);
  }

  next();
};

module.exports = {
  authenticateToken,
  requireFuncionario,
  requirePaciente,
  optionalAuth
};
