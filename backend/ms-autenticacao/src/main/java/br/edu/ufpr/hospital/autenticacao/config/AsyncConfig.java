package br.edu.ufpr.hospital.autenticacao.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuração para processamento assíncrono
 * Principalmente usado para envio de e-mails
 */
@Configuration
@EnableAsync
@EnableRetry
@Slf4j
public class AsyncConfig {

  /**
   * Configuração do executor para tarefas assíncronas de e-mail
   */
  @Bean(name = "emailTaskExecutor")
  public Executor emailTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    
    // Configurações do pool de threads
    executor.setCorePoolSize(2);           // Número mínimo de threads
    executor.setMaxPoolSize(5);            // Número máximo de threads
    executor.setQueueCapacity(100);        // Capacidade da fila
    executor.setThreadNamePrefix("EmailTask-"); // Prefixo do nome das threads
    
    // Configurações de comportamento
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);
    
    // Política de rejeição: descartar tarefas mais antigas quando a fila estiver cheia
    executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy());
    
    executor.initialize();
    
    log.info("Email Task Executor configurado: Core={}, Max={}, Queue={}", 
        executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
    
    return executor;
  }

  /**
   * Configuração do executor padrão para outras tarefas assíncronas
   */
  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    
    executor.setCorePoolSize(3);
    executor.setMaxPoolSize(6);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("AsyncTask-");
    
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);
    
    executor.initialize();
    
    log.info("Default Task Executor configurado: Core={}, Max={}, Queue={}", 
        executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
    
    return executor;
  }
}