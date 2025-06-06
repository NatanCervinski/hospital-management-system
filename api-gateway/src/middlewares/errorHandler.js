/**
 * Middleware global para tratamento de erros
 */
const errorHandler = (error, req, res, next) => {
  console.error('Erro capturado pelo errorHandler:', {
    message: error.message,
    stack: error.stack,
    url: req.originalUrl,
    method: req.method,
    timestamp: new Date().toISOString()
  });

  // Erro de timeout
  if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
    return res.status(504).json({
      error: 'Timeout na comunicação com o serviço',
      code: 'SERVICE_TIMEOUT'
    });
  }

  // Erro de conexão com microserviço
  if (error.code === 'ECONNREFUSED' || error.code === 'ENOTFOUND') {
    return res.status(503).json({
      error: 'Serviço temporariamente indisponível',
      code: 'SERVICE_UNAVAILABLE'
    });
  }

  // Erro de parsing JSON
  if (error instanceof SyntaxError && error.status === 400 && 'body' in error) {
    return res.status(400).json({
      error: 'Formato JSON inválido',
      code: 'INVALID_JSON'
    });
  }

  // Erro de JWT
  if (error.name === 'JsonWebTokenError') {
    return res.status(403).json({
      error: 'Token JWT inválido',
      code: 'INVALID_JWT'
    });
  }

  if (error.name === 'TokenExpiredError') {
    return res.status(403).json({
      error: 'Token JWT expirado',
      code: 'EXPIRED_JWT'
    });
  }

  // Erro de validação
  if (error.name === 'ValidationError') {
    return res.status(400).json({
      error: 'Dados de entrada inválidos',
      code: 'VALIDATION_ERROR',
      details: error.details || error.message
    });
  }

  // Erro HTTP do Axios (resposta de microserviço)
  if (error.response) {
    const status = error.response.status;
    const data = error.response.data;
    
    return res.status(status).json({
      error: data?.error || data?.message || 'Erro no serviço',
      code: data?.code || 'SERVICE_ERROR',
      details: data?.details
    });
  }

  // Erro genérico
  const status = error.status || error.statusCode || 500;
  
  res.status(status).json({
    error: process.env.NODE_ENV === 'production' 
      ? 'Erro interno do servidor' 
      : error.message,
    code: 'INTERNAL_SERVER_ERROR',
    ...(process.env.NODE_ENV !== 'production' && { stack: error.stack })
  });
};

module.exports = errorHandler;