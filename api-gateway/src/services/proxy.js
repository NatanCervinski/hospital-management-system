const axios = require('axios');
const config = require('../config');

/**
 * Serviço para proxy de requisições para microserviços
 */
class ProxyService {

  /**
   * Faz proxy de uma requisição para um microserviço específico
   */
  static async forwardRequest(serviceUrl, path, method, headers, data, timeout = 30000) {
    try {
      const url = `${serviceUrl}${path}`;

      // Remover headers problemáticos
      const cleanHeaders = { ...headers };
      delete cleanHeaders['host'];
      delete cleanHeaders['content-length'];
      delete cleanHeaders['connection'];

      const config = {
        method: method.toLowerCase(),
        url,
        headers: cleanHeaders,
        timeout,
        validateStatus: () => true // Aceitar qualquer status code
      };


      // Adicionar dados se for POST/PUT/PATCH
      if (['post', 'put', 'patch'].includes(method.toLowerCase()) && data) {
        config.data = data;
        console.log(`📦 Adicionando body para ${method.toUpperCase()}`);
      }

      console.log(`📋 Config final do axios:`, JSON.stringify(config, null, 2));

      console.log(`🔄 Proxy: ${method.toUpperCase()} ${url}`);

      const response = await axios(config);

      return {
        status: response.status,
        data: response.data,
        headers: response.headers
      };

    } catch (error) {
      console.error(`❌ Erro no proxy para ${serviceUrl}${path}:`, error.message);

      if (error.code === 'ECONNREFUSED') {
        throw new Error(`Serviço indisponível: ${serviceUrl}`);
      }

      if (error.code === 'ECONNABORTED') {
        throw new Error('Timeout na comunicação com o serviço');
      }

      throw error;
    }
  }

  /**
   * Determina qual microserviço deve receber a requisição baseado no path
   */
  static getServiceConfig(path) {
    // Rotas de autenticação
    process.stdout.write("\n 🔍 Verificando serviço para a rota: " + path + "\n");
    if (path.startsWith('/auth') || path.startsWith('/funcionarios')) {
      return {
        name: 'autenticacao',
        url: config.microservices.autenticacao.url,
        timeout: config.microservices.autenticacao.timeout
      };
    }

    // Rotas de paciente
    if (path.startsWith('/pacientes')) {
      return {
        name: 'paciente',
        url: config.microservices.paciente.url,
        timeout: config.microservices.paciente.timeout
      };
    }

    // Rotas de consulta/agendamento
    if (path.startsWith('/consultas') || path.startsWith('/agendamentos')) {
      return {
        name: 'consulta',
        url: config.microservices.consulta.url,
        timeout: config.microservices.consulta.timeout
      };
    }

    return null;
  }

  /**
   * Middleware para fazer proxy automático das requisições
   */
  static createProxyMiddleware() {
    return async (req, res, next) => {
      try {
        const targetPath = req.originalUrl.replace('/api', '');

        const serviceConfig = ProxyService.getServiceConfig(targetPath);

        if (!serviceConfig) {
          console.log(`❌ Serviço não encontrado para: ${targetPath}`);
          return res.status(404).json({
            error: 'Serviço não encontrado para esta rota',
            code: 'SERVICE_NOT_FOUND',
            path: targetPath
          });
        }

        console.log(`🔄 Encaminhando requisição para o serviço: ${serviceConfig.name} (${req.method})`);



        const result = await ProxyService.forwardRequest(
          serviceConfig.url,
          req.originalUrl,  // Mantém /api/auth/login
          req.method,
          req.headers,
          req.body,
          serviceConfig.timeout
        );

        if (result.headers['content-type']) {
          res.set('content-type', result.headers['content-type']);
        }

        res.status(result.status).json(result.data);

      } catch (error) {
        next(error);
      }
    };
  }
}

module.exports = ProxyService;
