module.exports = {
  port: process.env.PORT || 3000,
  nodeEnv: process.env.NODE_ENV || 'development',
  
  jwt: {
    secret: process.env.JWT_SECRET || 'minhaChaveSecretaSuperSeguraParaJWT2025HospitalSystem',
    expiration: process.env.JWT_EXPIRATION || 86400000
  },
  
  microservices: {
    autenticacao: {
      url: process.env.MS_AUTENTICACAO_URL || 'http://ms-autenticacao:8081',
      timeout: 30000
    },
    paciente: {
      url: process.env.MS_PACIENTE_URL || 'http://ms-paciente:8083',
      timeout: 30000
    },
    consulta: {
      url: process.env.MS_CONSULTA_URL || 'http://ms-consulta:8085',
      timeout: 30000
    }
  },
  
  rateLimit: {
    windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS) || 900000, // 15 minutes
    maxRequests: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS) || 100
  },
  
  cors: {
    origin: process.env.CORS_ORIGIN || 'http://localhost:4200'
  }
};