package br.edu.ufpr.hospital.autenticacao.config;

import br.edu.ufpr.hospital.autenticacao.model.FuncionarioModel;
import br.edu.ufpr.hospital.autenticacao.repository.UsuarioRepository;
import br.edu.ufpr.hospital.autenticacao.security.SecureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;

/**
 * Data initializer that conditionally loads initial data only if the database is empty.
 * This prevents duplicate key violations when containers restart.
 */
@Configuration
public class DataInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Value("${app.data.load-initial:true}")
    private boolean loadInitialData;
    
    @Bean
    @Profile("!test") // Don't run during tests
    public ApplicationRunner initializeData() {
        return args -> {
            try {
                if (!loadInitialData) {
                    logger.info("Data loading is disabled (app.data.load-initial=false). Skipping initialization.");
                    return;
                }
                
                // Only initialize data if no users exist
                if (usuarioRepository.count() == 0) {
                    logger.info("Database is empty. Initializing default data...");
                    createDefaultFuncionario();
                    logger.info("Default data initialization completed successfully.");
                } else {
                    logger.info("Database already contains {} users. Skipping data initialization.", 
                              usuarioRepository.count());
                }
            } catch (Exception e) {
                logger.error("Error during data initialization: {}", e.getMessage(), e);
                // Don't rethrow - let the application start even if data initialization fails
            }
        };
    }
    
    private void createDefaultFuncionario() {
        // Check specifically for the default funcionario by CPF
        if (usuarioRepository.existsByCpf("90769281001")) {
            logger.info("Default funcionario already exists. Skipping creation.");
            return;
        }
        
        FuncionarioModel funcionario = new FuncionarioModel();
        funcionario.setNome("Funcionário Padrão");
        funcionario.setCpf("90769281001");
        funcionario.setEmail("func_pre@hospital.com");
        
        // Generate salt and hash password using SecureUtils
        String salt = SecureUtils.generateSalt();
        String hashedPassword = SecureUtils.getSecurePassword("TADS", salt);
        funcionario.setSenha(hashedPassword);
        funcionario.setSalt(salt);
        
        funcionario.setAtivo(true);
        funcionario.setDataCadastro(LocalDateTime.now());
        funcionario.setSenhaTemporaria(false);
        // Note: perfil is set automatically via @DiscriminatorValue("FUNCIONARIO")
        
        usuarioRepository.save(funcionario);
        logger.info("Created default funcionario: {}", funcionario.getEmail());
    }
}