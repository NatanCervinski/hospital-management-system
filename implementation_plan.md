# Implementation Plan for Missing Features

## Overview

This document provides a practical, step-by-step implementation strategy for completing the Hospital Management System. Since **MS Autenticação** and **MS Paciente** are fully implemented, this plan focuses primarily on developing **MS Consulta** from its current minimal state to full functionality.

---

## Implementation Priority

### Phase 1: MS Consulta Core Foundation (Days 1-3)
**Priority: CRITICAL** - The consultation service requires complete redesign

### Phase 2: MS Consulta Business Logic (Days 4-7)  
**Priority: HIGH** - Implement core booking and management workflows

### Phase 3: MS Consulta Integration (Days 8-10)
**Priority: HIGH** - Connect with other microservices and API Gateway

### Phase 4: Testing & Refinement (Days 11-12)
**Priority: MEDIUM** - Comprehensive testing and bug fixes

### Phase 5: Minor Enhancements (Days 13-14)
**Priority: LOW** - Optional improvements to existing services

---

# Phase 1: MS Consulta Core Foundation (Days 1-3)

## Day 1: Database Design & Entity Modeling

### Step 1.1: Create New Entity Structure
**Location**: `backend/ms-consulta/src/main/java/br/edu/ufpr/hospital/consulta/model/`

#### Create `Consulta.java` (Consultation Slots)
```java
@Entity
@Table(name = "consultas")
public class Consulta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String codigo; // Auto-generated (CON001, CON002, etc.)
    private LocalDateTime dataHora;
    private String especialidade;
    private String medico;
    private BigDecimal valor;
    private Integer vagas; // Total available slots
    private Integer vagasOcupadas; // Booked slots
    
    @Enumerated(EnumType.STRING)
    private StatusConsulta status; // DISPONÍVEL, CANCELADA, REALIZADA
    
    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL)
    private List<Agendamento> agendamentos = new ArrayList<>();
    
    // Constructors, getters, setters
}
```

#### Create `Agendamento.java` (Patient Bookings)
```java
@Entity
@Table(name = "agendamentos")
public class Agendamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String codigoAgendamento; // Unique booking code
    private Integer pacienteId; // Reference to ms-paciente
    private BigDecimal pontosUsados;
    private BigDecimal valorPago;
    private LocalDateTime dataAgendamento;
    
    @Enumerated(EnumType.STRING)
    private StatusAgendamento status; // CRIADO, CHECK_IN, COMPARECEU, FALTOU, REALIZADO, CANCELADO
    
    @ManyToOne
    @JoinColumn(name = "consulta_id")
    private Consulta consulta;
    
    // Constructors, getters, setters
}
```

#### Create Status Enums
```java
public enum StatusConsulta {
    DISPONIVEL, CANCELADA, REALIZADA
}

public enum StatusAgendamento {
    CRIADO, CHECK_IN, COMPARECEU, FALTOU, REALIZADO, CANCELADO
}
```

### Step 1.2: Create Employee Entity for Local Management
```java
@Entity
@Table(name = "funcionarios")
public class Funcionario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    
    @Enumerated(EnumType.STRING)
    private StatusFuncionario status; // ATIVO, INATIVO
    
    // Constructors, getters, setters
}
```

### Step 1.3: Update Database Configuration
**Location**: `backend/ms-consulta/src/main/resources/application.properties`

Update database configuration to match other services:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5435/ms_consulta
spring.datasource.username=dac
spring.datasource.password=123
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

---

## Day 2: Repository Layer & Basic Configuration

### Step 2.1: Create Repository Interfaces
**Location**: `backend/ms-consulta/src/main/java/br/edu/ufpr/hospital/consulta/repository/`

#### ConsultaRepository.java
```java
@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    
    List<Consulta> findByStatusAndDataHoraAfter(StatusConsulta status, LocalDateTime dataHora);
    
    List<Consulta> findByEspecialidadeAndStatusAndDataHoraAfter(
        String especialidade, StatusConsulta status, LocalDateTime dataHora);
    
    List<Consulta> findByMedicoContainingIgnoreCaseAndStatusAndDataHoraAfter(
        String medico, StatusConsulta status, LocalDateTime dataHora);
    
    @Query("SELECT c FROM Consulta c WHERE c.dataHora BETWEEN :inicio AND :fim")
    List<Consulta> findConsultasNext48Hours(@Param("inicio") LocalDateTime inicio, 
                                           @Param("fim") LocalDateTime fim);
}
```

#### AgendamentoRepository.java
```java
@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    
    List<Agendamento> findByPacienteId(Integer pacienteId);
    
    List<Agendamento> findByPacienteIdOrderByDataAgendamentoDesc(Integer pacienteId);
    
    Optional<Agendamento> findByCodigoAgendamento(String codigoAgendamento);
    
    List<Agendamento> findByConsultaIdAndStatus(Long consultaId, StatusAgendamento status);
    
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.consulta.id = :consultaId AND a.status != 'CANCELADO'")
    Integer countActiveBookingsByConsulta(@Param("consultaId") Long consultaId);
}
```

#### FuncionarioRepository.java
```java
@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    Optional<Funcionario> findByCpf(String cpf);
    List<Funcionario> findByStatus(StatusFuncionario status);
}
```

### Step 2.2: Security Configuration
**Location**: `backend/ms-consulta/src/main/java/br/edu/ufpr/hospital/consulta/config/`

#### SecurityConfig.java
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/consultas/buscar/**").authenticated() // Both roles
                .requestMatchers("/agendamentos/**").hasRole("PACIENTE")
                .requestMatchers("/consultas/**").hasRole("FUNCIONARIO")
                .requestMatchers("/funcionarios/**").hasRole("FUNCIONARIO")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder()))
            );
        
        return http.build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(
            new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256")
        ).build();
    }
}
```

---

## Day 3: DTOs & Basic Service Structure

### Step 3.1: Create DTOs
**Location**: `backend/ms-consulta/src/main/java/br/edu/ufpr/hospital/consulta/dto/`

#### ConsultaDTO.java (for creation/updates)
```java
public class ConsultaDTO {
    @NotNull @Future
    private LocalDateTime dataHora;
    
    @NotBlank
    private String especialidade;
    
    @NotBlank
    private String medico;
    
    @NotNull @DecimalMin("0.01")
    private BigDecimal valor;
    
    @NotNull @Min(1)
    private Integer vagas;
    
    // Constructors, getters, setters
}
```

#### ConsultaResponseDTO.java
```java
public class ConsultaResponseDTO {
    private Long id;
    private String codigo;
    private LocalDateTime dataHora;
    private String especialidade;
    private String medico;
    private BigDecimal valor;
    private Integer vagas;
    private Integer vagasDisponiveis;
    private StatusConsulta status;
    
    // Static factory method
    public static ConsultaResponseDTO fromEntity(Consulta consulta) {
        // Implementation
    }
}
```

#### AgendamentoDTO.java
```java
public class AgendamentoDTO {
    @NotNull
    private Long consultaId;
    
    @DecimalMin("0")
    private BigDecimal pontosUsados = BigDecimal.ZERO;
    
    // Constructors, getters, setters
}
```

#### AgendamentoResponseDTO.java
```java
public class AgendamentoResponseDTO {
    private Long id;
    private String codigoAgendamento;
    private LocalDateTime dataAgendamento;
    private BigDecimal pontosUsados;
    private BigDecimal valorPago;
    private StatusAgendamento status;
    private ConsultaResponseDTO consulta;
    
    // Static factory method and constructors
}
```

### Step 3.2: Basic Service Layer Structure
**Location**: `backend/ms-consulta/src/main/java/br/edu/ufpr/hospital/consulta/service/`

#### ConsultaService.java (Basic structure)
```java
@Service
@Transactional
public class ConsultaService {
    
    private final ConsultaRepository consultaRepository;
    private final AgendamentoRepository agendamentoRepository;
    
    public ConsultaService(ConsultaRepository consultaRepository, 
                          AgendamentoRepository agendamentoRepository) {
        this.consultaRepository = consultaRepository;
        this.agendamentoRepository = agendamentoRepository;
    }
    
    // Methods to be implemented in Phase 2
    public ConsultaResponseDTO criarConsulta(ConsultaDTO dto) { }
    public List<ConsultaResponseDTO> buscarConsultasDisponiveis() { }
    public List<ConsultaResponseDTO> buscarPorEspecialidade(String especialidade) { }
    // ... other methods
}
```

---

# Phase 2: MS Consulta Business Logic (Days 4-7)

## Day 4: Core Consultation Management (R12)

### Step 4.1: Implement Consultation Creation
**In**: `ConsultaService.java`

```java
public ConsultaResponseDTO criarConsulta(ConsultaDTO dto) {
    Consulta consulta = new Consulta();
    consulta.setCodigo(generateConsultaCode());
    consulta.setDataHora(dto.getDataHora());
    consulta.setEspecialidade(dto.getEspecialidade());
    consulta.setMedico(dto.getMedico());
    consulta.setValor(dto.getValor());
    consulta.setVagas(dto.getVagas());
    consulta.setVagasOcupadas(0);
    consulta.setStatus(StatusConsulta.DISPONIVEL);
    
    consulta = consultaRepository.save(consulta);
    return ConsultaResponseDTO.fromEntity(consulta);
}

private String generateConsultaCode() {
    Long count = consultaRepository.count() + 1;
    return String.format("CON%03d", count);
}
```

### Step 4.2: Implement Search Functions (R05 - Part 1)
```java
public List<ConsultaResponseDTO> buscarConsultasDisponiveis() {
    LocalDateTime now = LocalDateTime.now();
    List<Consulta> consultas = consultaRepository
        .findByStatusAndDataHoraAfter(StatusConsulta.DISPONIVEL, now);
    
    return consultas.stream()
        .filter(c -> c.getVagasOcupadas() < c.getVagas())
        .map(ConsultaResponseDTO::fromEntity)
        .collect(Collectors.toList());
}

public List<ConsultaResponseDTO> buscarPorEspecialidade(String especialidade) {
    LocalDateTime now = LocalDateTime.now();
    List<Consulta> consultas = consultaRepository
        .findByEspecialidadeAndStatusAndDataHoraAfter(
            especialidade, StatusConsulta.DISPONIVEL, now);
    
    return consultas.stream()
        .filter(c -> c.getVagasOcupadas() < c.getVagas())
        .map(ConsultaResponseDTO::fromEntity)
        .collect(Collectors.toList());
}
```

### Step 4.3: Create Controller Endpoints
**Location**: `backend/ms-consulta/src/main/java/br/edu/ufpr/hospital/consulta/controller/`

#### ConsultaController.java
```java
@RestController
@RequestMapping("/consultas")
public class ConsultaController {
    
    private final ConsultaService consultaService;
    
    @PostMapping
    @PreAuthorize("hasRole('FUNCIONARIO')")
    public ResponseEntity<ConsultaResponseDTO> criarConsulta(@Valid @RequestBody ConsultaDTO dto) {
        ConsultaResponseDTO response = consultaService.criarConsulta(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/buscar")
    public ResponseEntity<List<ConsultaResponseDTO>> buscarConsultasDisponiveis() {
        List<ConsultaResponseDTO> consultas = consultaService.buscarConsultasDisponiveis();
        return ResponseEntity.ok(consultas);
    }
    
    @GetMapping("/buscar/especialidade/{especialidade}")
    public ResponseEntity<List<ConsultaResponseDTO>> buscarPorEspecialidade(
            @PathVariable String especialidade) {
        List<ConsultaResponseDTO> consultas = consultaService.buscarPorEspecialidade(especialidade);
        return ResponseEntity.ok(consultas);
    }
}
```

---

## Day 5: Patient Booking System (R05)

### Step 5.1: Create Integration Service for MS Paciente
**Location**: `backend/ms-consulta/src/main/java/br/edu/ufpr/hospital/consulta/service/`

#### PacienteIntegrationService.java
```java
@Service
public class PacienteIntegrationService {
    
    private final WebClient webClient;
    
    @Value("${ms.paciente.url:http://localhost:8083}")
    private String msPacienteUrl;
    
    public PacienteIntegrationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(msPacienteUrl).build();
    }
    
    public BigDecimal verificarSaldoPontos(Integer pacienteId, String token) {
        try {
            SaldoPontosDTO response = webClient.get()
                .uri("/pacientes/{pacienteId}/saldo-e-historico", pacienteId)
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(SaldoPontosDTO.class)
                .block();
                
            return response != null ? response.getSaldoAtual() : BigDecimal.ZERO;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao verificar saldo de pontos", e);
        }
    }
    
    public void deduzirPontos(Integer pacienteId, BigDecimal pontos, String token) {
        if (pontos.compareTo(BigDecimal.ZERO) > 0) {
            DeducaoPontosDTO request = new DeducaoPontosDTO(pontos, "USO EM CONSULTA");
            
            webClient.put()
                .uri("/pacientes/{pacienteId}/deduzir-pontos", pacienteId)
                .header("Authorization", token)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        }
    }
}
```

### Step 5.2: Implement Booking Logic
**In**: `ConsultaService.java`

```java
@Autowired
private PacienteIntegrationService pacienteService;

public AgendamentoResponseDTO agendarConsulta(Long consultaId, AgendamentoDTO dto, 
                                            Integer pacienteId, String authToken) {
    // Validate consultation exists and has available slots
    Consulta consulta = consultaRepository.findById(consultaId)
        .orElseThrow(() -> new ConsultaNaoEncontradaException("Consulta não encontrada"));
    
    if (consulta.getStatus() != StatusConsulta.DISPONIVEL) {
        throw new ConsultaIndisponivelException("Consulta não está disponível");
    }
    
    if (consulta.getVagasOcupadas() >= consulta.getVagas()) {
        throw new ConsultaIndisponivelException("Consulta sem vagas disponíveis");
    }
    
    // Validate and deduct points if requested
    BigDecimal pontosUsados = dto.getPontosUsados();
    if (pontosUsados.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal saldoAtual = pacienteService.verificarSaldoPontos(pacienteId, authToken);
        if (saldoAtual.compareTo(pontosUsados) < 0) {
            throw new SaldoInsuficienteException("Saldo de pontos insuficiente");
        }
    }
    
    // Calculate payment amount (1 point = R$ 5.00)
    BigDecimal descontoPontos = pontosUsados.multiply(new BigDecimal("5.00"));
    BigDecimal valorPago = consulta.getValor().subtract(descontoPontos);
    
    if (valorPago.compareTo(BigDecimal.ZERO) < 0) {
        valorPago = BigDecimal.ZERO;
    }
    
    // Create booking
    Agendamento agendamento = new Agendamento();
    agendamento.setCodigoAgendamento(generateBookingCode());
    agendamento.setPacienteId(pacienteId);
    agendamento.setConsulta(consulta);
    agendamento.setPontosUsados(pontosUsados);
    agendamento.setValorPago(valorPago);
    agendamento.setDataAgendamento(LocalDateTime.now());
    agendamento.setStatus(StatusAgendamento.CRIADO);
    
    // Deduct points from patient account
    if (pontosUsados.compareTo(BigDecimal.ZERO) > 0) {
        pacienteService.deduzirPontos(pacienteId, pontosUsados, authToken);
    }
    
    // Update consultation occupancy
    consulta.setVagasOcupadas(consulta.getVagasOcupadas() + 1);
    
    agendamento = agendamentoRepository.save(agendamento);
    consultaRepository.save(consulta);
    
    return AgendamentoResponseDTO.fromEntity(agendamento);
}

private String generateBookingCode() {
    return "AGD" + System.currentTimeMillis();
}
```

---

## Day 6: Patient Operations (R06, R07)

### Step 6.1: Implement Cancellation Logic (R06)
```java
public void cancelarAgendamento(Long agendamentoId, Integer pacienteId, String authToken) {
    Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
        .orElseThrow(() -> new AgendamentoNaoEncontradoException("Agendamento não encontrado"));
    
    // Verify ownership
    if (!agendamento.getPacienteId().equals(pacienteId)) {
        throw new AcessoNegadoException("Agendamento pertence a outro paciente");
    }
    
    // Verify status allows cancellation
    if (agendamento.getStatus() != StatusAgendamento.CRIADO && 
        agendamento.getStatus() != StatusAgendamento.CHECK_IN) {
        throw new CancelamentoInvalidoException("Agendamento não pode ser cancelado neste status");
    }
    
    // Refund points if any were used
    if (agendamento.getPontosUsados().compareTo(BigDecimal.ZERO) > 0) {
        pacienteService.adicionarPontos(pacienteId, agendamento.getPontosUsados(), 
                                       "CANCELAMENTO DE AGENDAMENTO", authToken);
    }
    
    // Update status and free up slot
    agendamento.setStatus(StatusAgendamento.CANCELADO);
    Consulta consulta = agendamento.getConsulta();
    consulta.setVagasOcupadas(consulta.getVagasOcupadas() - 1);
    
    agendamentoRepository.save(agendamento);
    consultaRepository.save(consulta);
}
```

### Step 6.2: Implement Check-in Logic (R07)
```java
public void realizarCheckin(Long agendamentoId, Integer pacienteId) {
    Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
        .orElseThrow(() -> new AgendamentoNaoEncontradoException("Agendamento não encontrado"));
    
    // Verify ownership
    if (!agendamento.getPacienteId().equals(pacienteId)) {
        throw new AcessoNegadoException("Agendamento pertence a outro paciente");
    }
    
    // Verify status
    if (agendamento.getStatus() != StatusAgendamento.CRIADO) {
        throw new CheckinInvalidoException("Check-in só é permitido para agendamentos com status CRIADO");
    }
    
    // Verify 48-hour window
    LocalDateTime consultaTime = agendamento.getConsulta().getDataHora();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime checkinLimit = consultaTime.minusHours(48);
    
    if (now.isBefore(checkinLimit)) {
        throw new CheckinInvalidoException("Check-in só é permitido nas 48 horas anteriores à consulta");
    }
    
    if (now.isAfter(consultaTime)) {
        throw new CheckinInvalidoException("Check-in não é mais permitido após o horário da consulta");
    }
    
    agendamento.setStatus(StatusAgendamento.CHECK_IN);
    agendamentoRepository.save(agendamento);
}
```

---

## Day 7: Employee Operations (R08-R11)

### Step 7.1: Employee Dashboard (R08)
```java
public List<ConsultaResponseDTO> buscarConsultasProximas48h() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime limit = now.plusHours(48);
    
    List<Consulta> consultas = consultaRepository.findConsultasNext48Hours(now, limit);
    
    return consultas.stream()
        .map(ConsultaResponseDTO::fromEntity)
        .collect(Collectors.toList());
}
```

### Step 7.2: Confirm Attendance (R09)
```java
public void confirmarComparecimento(String codigoAgendamento) {
    Agendamento agendamento = agendamentoRepository.findByCodigoAgendamento(codigoAgendamento)
        .orElseThrow(() -> new AgendamentoNaoEncontradoException("Código de agendamento inválido"));
    
    if (agendamento.getStatus() != StatusAgendamento.CHECK_IN) {
        throw new ConfirmacaoInvalidaException("Só é possível confirmar agendamentos com check-in realizado");
    }
    
    agendamento.setStatus(StatusAgendamento.COMPARECEU);
    agendamentoRepository.save(agendamento);
}
```

### Step 7.3: Cancel Consultation (R10)
```java
public void cancelarConsulta(Long consultaId, String authToken) {
    Consulta consulta = consultaRepository.findById(consultaId)
        .orElseThrow(() -> new ConsultaNaoEncontradaException("Consulta não encontrada"));
    
    // Check 50% occupancy rule
    double occupancyRate = (double) consulta.getVagasOcupadas() / consulta.getVagas();
    if (occupancyRate >= 0.5) {
        throw new CancelamentoInvalidoException("Consulta com 50% ou mais das vagas ocupadas não pode ser cancelada");
    }
    
    // Cancel all active bookings and refund points
    List<Agendamento> agendamentosAtivos = agendamentoRepository
        .findByConsultaIdAndStatus(consultaId, StatusAgendamento.CRIADO);
    agendamentosAtivos.addAll(agendamentoRepository
        .findByConsultaIdAndStatus(consultaId, StatusAgendamento.CHECK_IN));
    
    for (Agendamento agendamento : agendamentosAtivos) {
        agendamento.setStatus(StatusAgendamento.CANCELADO);
        
        // Refund points
        if (agendamento.getPontosUsados().compareTo(BigDecimal.ZERO) > 0) {
            pacienteService.adicionarPontos(agendamento.getPacienteId(), 
                                           agendamento.getPontosUsados(),
                                           "CANCELAMENTO DE CONSULTA", authToken);
        }
    }
    
    consulta.setStatus(StatusConsulta.CANCELADA);
    
    agendamentoRepository.saveAll(agendamentosAtivos);
    consultaRepository.save(consulta);
}
```

### Step 7.4: Finalize Consultation (R11)
```java
public void realizarConsulta(Long consultaId) {
    Consulta consulta = consultaRepository.findById(consultaId)
        .orElseThrow(() -> new ConsultaNaoEncontradoException("Consulta não encontrada"));
    
    // Update consultation status
    consulta.setStatus(StatusConsulta.REALIZADA);
    
    // Update all booking statuses
    List<Agendamento> agendamentos = agendamentoRepository.findByConsultaId(consultaId);
    
    for (Agendamento agendamento : agendamentos) {
        if (agendamento.getStatus() == StatusAgendamento.COMPARECEU) {
            agendamento.setStatus(StatusAgendamento.REALIZADO);
        } else if (agendamento.getStatus() == StatusAgendamento.CRIADO || 
                   agendamento.getStatus() == StatusAgendamento.CHECK_IN) {
            agendamento.setStatus(StatusAgendamento.FALTOU);
        }
        // CANCELADO remains unchanged
    }
    
    consultaRepository.save(consulta);
    agendamentoRepository.saveAll(agendamentos);
}
```

---

# Phase 3: MS Consulta Integration (Days 8-10)

## Day 8: Complete Controller Layer

### Step 8.1: Complete AgendamentoController
**Location**: `backend/ms-consulta/src/main/java/br/edu/ufpr/hospital/consulta/controller/`

```java
@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {
    
    private final ConsultaService consultaService;
    
    @PostMapping("/consulta/{consultaId}")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<AgendamentoResponseDTO> agendarConsulta(
            @PathVariable Long consultaId,
            @Valid @RequestBody AgendamentoDTO dto,
            Authentication authentication,
            HttpServletRequest request) {
        
        Integer pacienteId = extractPacienteIdFromToken(authentication);
        String authToken = request.getHeader("Authorization");
        
        AgendamentoResponseDTO response = consultaService.agendarConsulta(
            consultaId, dto, pacienteId, authToken);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{agendamentoId}/cancelar")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<Void> cancelarAgendamento(
            @PathVariable Long agendamentoId,
            Authentication authentication,
            HttpServletRequest request) {
        
        Integer pacienteId = extractPacienteIdFromToken(authentication);
        String authToken = request.getHeader("Authorization");
        
        consultaService.cancelarAgendamento(agendamentoId, pacienteId, authToken);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{agendamentoId}/checkin")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<Void> realizarCheckin(
            @PathVariable Long agendamentoId,
            Authentication authentication) {
        
        Integer pacienteId = extractPacienteIdFromToken(authentication);
        consultaService.realizarCheckin(agendamentoId, pacienteId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/paciente")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarAgendamentosPaciente(
            Authentication authentication) {
        
        Integer pacienteId = extractPacienteIdFromToken(authentication);
        List<AgendamentoResponseDTO> agendamentos = 
            consultaService.listarAgendamentosPaciente(pacienteId);
        
        return ResponseEntity.ok(agendamentos);
    }
}
```

### Step 8.2: Complete Employee Endpoints
Add to `ConsultaController.java`:

```java
@GetMapping("/dashboard")
@PreAuthorize("hasRole('FUNCIONARIO')")
public ResponseEntity<List<ConsultaResponseDTO>> buscarConsultasProximas48h() {
    List<ConsultaResponseDTO> consultas = consultaService.buscarConsultasProximas48h();
    return ResponseEntity.ok(consultas);
}

@PutMapping("/{consultaId}/cancelar")
@PreAuthorize("hasRole('FUNCIONARIO')")
public ResponseEntity<Void> cancelarConsulta(
        @PathVariable Long consultaId,
        HttpServletRequest request) {
    
    String authToken = request.getHeader("Authorization");
    consultaService.cancelarConsulta(consultaId, authToken);
    return ResponseEntity.noContent().build();
}

@PutMapping("/{consultaId}/realizar")
@PreAuthorize("hasRole('FUNCIONARIO')")
public ResponseEntity<Void> realizarConsulta(@PathVariable Long consultaId) {
    consultaService.realizarConsulta(consultaId);
    return ResponseEntity.noContent().build();
}

@PutMapping("/agendamento/confirmar")
@PreAuthorize("hasRole('FUNCIONARIO')")
public ResponseEntity<Void> confirmarComparecimento(@RequestParam String codigo) {
    consultaService.confirmarComparecimento(codigo);
    return ResponseEntity.noContent().build();
}
```

## Day 9: Employee CRUD (R13-R15)

### Step 9.1: FuncionarioService
```java
@Service
@Transactional
public class FuncionarioService {
    
    private final FuncionarioRepository funcionarioRepository;
    
    public FuncionarioResponseDTO criarFuncionario(FuncionarioDTO dto) {
        if (funcionarioRepository.findByCpf(dto.getCpf()).isPresent()) {
            throw new CpfJaExisteException("CPF já cadastrado");
        }
        
        Funcionario funcionario = new Funcionario();
        funcionario.setNome(dto.getNome());
        funcionario.setCpf(dto.getCpf());
        funcionario.setEmail(dto.getEmail());
        funcionario.setTelefone(dto.getTelefone());
        funcionario.setStatus(StatusFuncionario.ATIVO);
        
        funcionario = funcionarioRepository.save(funcionario);
        return FuncionarioResponseDTO.fromEntity(funcionario);
    }
    
    public FuncionarioResponseDTO atualizarFuncionario(Long id, FuncionarioUpdateDTO dto) {
        Funcionario funcionario = funcionarioRepository.findById(id)
            .orElseThrow(() -> new FuncionarioNaoEncontradoException("Funcionário não encontrado"));
        
        funcionario.setNome(dto.getNome());
        funcionario.setEmail(dto.getEmail());
        funcionario.setTelefone(dto.getTelefone());
        // CPF não pode ser alterado
        
        funcionario = funcionarioRepository.save(funcionario);
        return FuncionarioResponseDTO.fromEntity(funcionario);
    }
    
    public void inativarFuncionario(Long id) {
        Funcionario funcionario = funcionarioRepository.findById(id)
            .orElseThrow(() -> new FuncionarioNaoEncontradoException("Funcionário não encontrado"));
        
        funcionario.setStatus(StatusFuncionario.INATIVO);
        funcionarioRepository.save(funcionario);
    }
    
    public List<FuncionarioResponseDTO> listarFuncionarios() {
        return funcionarioRepository.findAll().stream()
            .map(FuncionarioResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
```

### Step 9.2: FuncionarioController
```java
@RestController
@RequestMapping("/funcionarios")
@PreAuthorize("hasRole('FUNCIONARIO')")
public class FuncionarioController {
    
    private final FuncionarioService funcionarioService;
    
    @PostMapping
    public ResponseEntity<FuncionarioResponseDTO> criarFuncionario(
            @Valid @RequestBody FuncionarioDTO dto) {
        FuncionarioResponseDTO response = funcionarioService.criarFuncionario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<FuncionarioResponseDTO> atualizarFuncionario(
            @PathVariable Long id,
            @Valid @RequestBody FuncionarioUpdateDTO dto) {
        FuncionarioResponseDTO response = funcionarioService.atualizarFuncionario(id, dto);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativarFuncionario(@PathVariable Long id) {
        funcionarioService.inativarFuncionario(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping
    public ResponseEntity<List<FuncionarioResponseDTO>> listarFuncionarios() {
        List<FuncionarioResponseDTO> funcionarios = funcionarioService.listarFuncionarios();
        return ResponseEntity.ok(funcionarios);
    }
}
```

## Day 10: API Gateway Integration

### Step 10.1: Update API Gateway Routes
**Location**: `api-gateway/src/routes/consulta.js`

```javascript
const express = require('express');
const { authenticateToken } = require('../middlewares/auth');
const { proxyRequest } = require('../services/proxy');

const router = express.Router();
const MS_CONSULTA_URL = process.env.MS_CONSULTA_URL || 'http://localhost:8085';

// Public search endpoints
router.get('/consultas/buscar*', authenticateToken, (req, res) => {
    proxyRequest(req, res, MS_CONSULTA_URL);
});

// Employee consultation management
router.post('/consultas', authenticateToken, (req, res) => {
    proxyRequest(req, res, MS_CONSULTA_URL);
});

router.get('/consultas/dashboard', authenticateToken, (req, res) => {
    proxyRequest(req, res, MS_CONSULTA_URL);
});

router.put('/consultas/:id/cancelar', authenticateToken, (req, res) => {
    proxyRequest(req, res, MS_CONSULTA_URL);
});

router.put('/consultas/:id/realizar', authenticateToken, (req, res) => {
    proxyRequest(req, res, MS_CONSULTA_URL);
});

router.put('/consultas/agendamento/confirmar', authenticateToken, (req, res) => {
    proxyRequest(req, res, MS_CONSULTA_URL);
});

// Patient booking management
router.post('/agendamentos/consulta/:consultaId', authenticateToken, (req, res) => {
    proxyRequest(req, res, MS_CONSULTA_URL);
});

router.put('/agendamentos/:id/cancelar', authenticateToken, (req, res) => {
    proxyRequest(req, res, MS_CONSULTA_URL);
});

router.put('/agendamentos/:id/checkin', authenticateToken, (req, res) => {
    proxyRequest(req, res, MS_CONSULTA_URL);
});

router.get('/agendamentos/paciente', authenticateToken, (req, res) => {
    proxyRequest(req, res, MS_CONSULTA_URL);
});

// Employee CRUD
router.use('/funcionarios', authenticateToken, (req, res) => {
    proxyRequest(req, res, MS_CONSULTA_URL);
});

module.exports = router;
```

### Step 10.2: Update main API Gateway routes
**Location**: `api-gateway/src/routes/index.js`

```javascript
const consultaRoutes = require('./consulta');

app.use('/api', consultaRoutes);
```

---

# Phase 4: Testing & Refinement (Days 11-12)

## Day 11: Create Test Data & Integration Tests

### Step 11.1: Create Data Initialization
**Location**: `backend/ms-consulta/src/main/resources/data.sql`

```sql
-- Specialties data
INSERT INTO especialidades (codigo, nome) VALUES 
('CARD', 'Cardiologia'),
('DERM', 'Dermatologia'),
('PED', 'Pediatria'),
('GINE', 'Ginecologia'),
('ORTO', 'Ortopedia');

-- Employees data
INSERT INTO funcionarios (nome, cpf, email, telefone, status) VALUES 
('Dr. Paulo Cardoso', '23456789012', 'dr.paulo@hospital.com', '(41) 99999-0001', 'ATIVO'),
('Dra. Lúcia Pediatra', '34567890123', 'dra.lucia@hospital.com', '(41) 99999-0002', 'ATIVO'),
('Dr. Carlos Dermatologista', '45678901234', 'dr.carlos@hospital.com', '(41) 99999-0003', 'ATIVO');

-- Sample consultations
INSERT INTO consultas (codigo, data_hora, especialidade, medico, valor, vagas, vagas_ocupadas, status) VALUES 
('CON001', '2025-08-10 10:30:00', 'CARD', 'Dr. Paulo', 300.00, 5, 0, 'DISPONIVEL'),
('CON002', '2025-09-11 09:30:00', 'PED', 'Dra. Lúcia', 250.00, 4, 0, 'DISPONIVEL'),
('CON003', '2025-10-12 08:30:00', 'DERM', 'Dr. Carlos', 200.00, 3, 0, 'DISPONIVEL');
```

### Step 11.2: Create Integration Test Scripts
**Location**: `backend/ms-consulta/test_consulta_integration.sh`

```bash
#!/bin/bash

# Test MS Consulta integration
BASE_URL="http://localhost:8085"

echo "Testing MS Consulta Integration..."

# Get auth token (requires ms-autenticacao running)
TOKEN=$(http POST localhost:8081/api/auth/login email=func_pre@hospital.com senha=TADS | jq -r '.token')

echo "1. Testing consultation creation..."
http POST $BASE_URL/consultas Authorization:"Bearer $TOKEN" \
  dataHora="2025-12-01T10:00:00" \
  especialidade="CARD" \
  medico="Dr. Test" \
  valor:=150.00 \
  vagas:=3

echo "2. Testing consultation search..."
http GET $BASE_URL/consultas/buscar Authorization:"Bearer $TOKEN"

echo "3. Testing patient booking flow..."
# This would require patient token and coordination with ms-paciente

echo "Integration tests completed."
```

## Day 12: Bug Fixes & Error Handling

### Step 12.1: Comprehensive Exception Handling
**Location**: `backend/ms-consulta/src/main/java/br/edu/ufpr/hospital/consulta/exception/`

#### GlobalExceptionHandler.java
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ConsultaNaoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleConsultaNaoEncontrada(ConsultaNaoEncontradaException e) {
        ErrorResponse error = new ErrorResponse("CONSULTA_NAO_ENCONTRADA", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleSaldoInsuficiente(SaldoInsuficienteException e) {
        ErrorResponse error = new ErrorResponse("SALDO_INSUFICIENTE", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException e) {
        ErrorResponse error = new ErrorResponse("DADOS_INVALIDOS", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    // Other exception handlers...
}
```

### Step 12.2: Validate Integration Points
- Test communication with ms-paciente for points operations
- Verify JWT token validation works correctly
- Test API Gateway routing to all new endpoints
- Validate error responses match expected format

---

# Phase 5: Minor Enhancements (Days 13-14)

## Day 13: MS Paciente Minor Fixes

### Step 13.1: Fix DTO Issue
**Location**: `backend/ms-paciente/src/main/java/br/edu/ufpr/hospital/paciente/dto/PacienteResponseDTO.java`

Remove duplicate `setSaldoPontos()` calls around lines 212-214.

### Step 13.2: Add Missing Integration Endpoint
**In**: `PacienteService.java`

```java
public void adicionarPontos(Integer pacienteId, BigDecimal pontos, String descricao) {
    // Implementation for adding points from external services
    Paciente paciente = findPacienteById(pacienteId);
    
    TransacaoPonto transacao = new TransacaoPonto();
    transacao.setPaciente(paciente);
    transacao.setTipo(TipoTransacaoPonto.ENTRADA);
    transacao.setOrigem(OrigemTransacaoPonto.CANCELAMENTO_AGENDAMENTO);
    transacao.setQuantidadePontos(pontos);
    transacao.setValorReais(pontos.multiply(VALOR_PONTO_REAIS));
    transacao.setDescricao(descricao);
    transacao.setDataTransacao(LocalDateTime.now());
    
    transacaoPontoRepository.save(transacao);
}
```

## Day 14: Documentation & Final Testing

### Step 14.1: Update CLAUDE.md
Add ms-consulta documentation to the main CLAUDE.md file with:
- New endpoints and their purposes
- Status flow diagrams
- Integration requirements
- Testing procedures

### Step 14.2: Create Comprehensive Test Suite
**Location**: `backend/ms-consulta/run_all_tests.sh`

```bash
#!/bin/bash

echo "Running comprehensive MS Consulta test suite..."

# Start with unit tests
./mvnw test

# Run integration tests
./test_consulta_integration.sh
./test_booking_flow.sh
./test_employee_workflow.sh

echo "All tests completed."
```

---

# Success Criteria

## Completion Checklist

### MS Consulta Implementation
- [ ] ✅ Entity separation: Consulta vs Agendamento
- [ ] ✅ All required endpoints implemented
- [ ] ✅ Proper status management and transitions
- [ ] ✅ Points integration with ms-paciente
- [ ] ✅ JWT security configuration
- [ ] ✅ Employee CRUD operations
- [ ] ✅ Comprehensive error handling
- [ ] ✅ API Gateway integration

### Integration & Testing
- [ ] ✅ All services communicate correctly
- [ ] ✅ End-to-end booking workflow functional
- [ ] ✅ Employee management workflow functional
- [ ] ✅ All test scripts pass
- [ ] ✅ Error scenarios handled gracefully

### Documentation
- [ ] ✅ CLAUDE.md updated with ms-consulta information
- [ ] ✅ API documentation complete
- [ ] ✅ Development commands documented

## Estimated Timeline: 14 days total
- **Critical Path (Days 1-10)**: MS Consulta core implementation
- **Integration (Days 8-10)**: Can run parallel with business logic
- **Testing (Days 11-12)**: Comprehensive validation
- **Polish (Days 13-14)**: Minor fixes and documentation

This implementation plan transforms the minimal MS Consulta into a fully functional consultation management service that meets all project requirements while maintaining the high quality standards established by the other microservices.