package br.edu.ufpr.hospital.paciente.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import br.edu.ufpr.hospital.paciente.dto.CompraPontosDTO;
import br.edu.ufpr.hospital.paciente.dto.PacienteCadastroDTO;
import br.edu.ufpr.hospital.paciente.dto.PacienteResponseDTO;
import br.edu.ufpr.hospital.paciente.dto.SaldoPontosDTO;
import br.edu.ufpr.hospital.paciente.dto.TransacaoPontoDTO;
import br.edu.ufpr.hospital.paciente.exception.NegocioException;
import br.edu.ufpr.hospital.paciente.exception.PacienteNaoEncontradoException;
import br.edu.ufpr.hospital.paciente.model.OrigemTransacaoPonto;
import br.edu.ufpr.hospital.paciente.model.Paciente;
import br.edu.ufpr.hospital.paciente.model.TipoTransacaoPonto;
import br.edu.ufpr.hospital.paciente.model.TransacaoPonto;
import br.edu.ufpr.hospital.paciente.repository.PacienteRepository;
import br.edu.ufpr.hospital.paciente.repository.TransacaoPontoRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PacienteService {
    private final PacienteRepository pacienteRepository;
    private final TransacaoPontoRepository transacaoPontoRepository;
    private final BigDecimal VALOR_PONTO_REAIS = new BigDecimal("5.00");

    public PacienteService(PacienteRepository pacienteRepository,
            TransacaoPontoRepository transacaoPontoRepository) {
        this.pacienteRepository = pacienteRepository;
        this.transacaoPontoRepository = transacaoPontoRepository;
    }

    @Transactional
    public PacienteResponseDTO cadastrarPaciente(PacienteCadastroDTO dto) {
        if (pacienteRepository.existsByCpf(dto.getCpf())) {
            throw new NegocioException("CPF já cadastrado.");
        }
        if (pacienteRepository.existsByEmail(dto.getEmail())) {
            throw new NegocioException("Email já cadastrado.");
        }

        Paciente paciente = new Paciente();
        String cpfNormalizado = dto.getCpf().replaceAll("\\D", "");
        paciente.setCpf(cpfNormalizado);
        paciente.setNome(dto.getNome());
        paciente.setEmail(dto.getEmail());
        if (dto.getTelefone() != null) {
            String telefoneNormalizado = dto.getTelefone().replaceAll("\\D", "");
            paciente.setTelefone(telefoneNormalizado);
        }
        String cepNormalizado = dto.getCep().replaceAll("\\D", "");
        paciente.setCep(cepNormalizado);
        paciente.setLogradouro(dto.getLogradouro());
        paciente.setNumero(dto.getNumero());
        paciente.setComplemento(dto.getComplemento());
        paciente.setBairro(dto.getBairro());
        paciente.setCidade(dto.getCidade());
        paciente.setUf(dto.getEstado());
        paciente.setSaldoPontos(BigDecimal.ZERO); // Inicia com 0 pontos
        paciente.setUsuarioId(dto.getUsuarioId());

        Paciente savedPaciente = pacienteRepository.save(paciente);
        return convertToResponseDTO(savedPaciente);
    }

    @Transactional
    public PacienteResponseDTO comprarPontos(Integer pacienteId, CompraPontosDTO dto) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new PacienteNaoEncontradoException("Paciente não encontrado."));

        if (dto.getValorReais().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("O valor para compra de pontos deve ser positivo.");
        }

        // 1 ponto = R$ 5,00
        BigDecimal quantidadePontos = dto.getValorReais().divide(VALOR_PONTO_REAIS, 2, RoundingMode.DOWN);
        if (quantidadePontos.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException(
                    "Valor insuficiente para comprar pontos. O valor mínimo é R$ " + VALOR_PONTO_REAIS);
        }

        paciente.setSaldoPontos(paciente.getSaldoPontos().add(quantidadePontos));
        Paciente updatedPaciente = pacienteRepository.save(paciente);

        TransacaoPonto transacao = new TransacaoPonto();
        transacao.setPaciente(updatedPaciente);
        transacao.setTipo(TipoTransacaoPonto.ENTRADA);
        transacao.setOrigem(OrigemTransacaoPonto.COMPRA);
        transacao.setValorReais(dto.getValorReais());
        transacao.setQuantidadePontos(quantidadePontos);
        transacao
                .setDescricao("COMPRA DE PONTOS - Valor: R$ " + dto.getValorReais() + " | Pontos: " + quantidadePontos);
        transacaoPontoRepository.save(transacao);

        return convertToResponseDTO(updatedPaciente);
    }

    public SaldoPontosDTO consultarSaldoEHistorico(Integer pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new PacienteNaoEncontradoException("Paciente não encontrado."));

        List<TransacaoPonto> transacoes = transacaoPontoRepository.findByPacienteIdOrderByDataHoraDesc(pacienteId);

        List<TransacaoPontoDTO> historicoDTO = transacoes.stream()
                .map(this::convertToTransacaoDTO)
                .collect(Collectors.toList());

        SaldoPontosDTO saldoDTO = new SaldoPontosDTO();
        saldoDTO.setSaldoAtual(paciente.getSaldoPontos());
        saldoDTO.setHistoricoTransacoes(historicoDTO);
        return saldoDTO;
    }

    @Transactional
    public PacienteResponseDTO deduzirPontos(Integer pacienteId, BigDecimal pontosADeduzir, String descricao) {
        if (pontosADeduzir.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("A quantidade de pontos a deduzir deve ser positiva.");
        }

        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new PacienteNaoEncontradoException("Paciente não encontrado."));

        if (paciente.getSaldoPontos().compareTo(pontosADeduzir) < 0) {
            throw new NegocioException("Saldo de pontos insuficiente.");
        }

        paciente.setSaldoPontos(paciente.getSaldoPontos().subtract(pontosADeduzir));
        Paciente updatedPaciente = pacienteRepository.save(paciente);

        TransacaoPonto transacao = new TransacaoPonto();
        transacao.setPaciente(updatedPaciente);
        transacao.setTipo(TipoTransacaoPonto.SAIDA);
        transacao.setOrigem(OrigemTransacaoPonto.USO_CONSULTA); // Ou outra origem específica se for o caso
        transacao.setQuantidadePontos(pontosADeduzir);
        transacao.setDescricao(descricao);
        transacaoPontoRepository.save(transacao);

        return convertToResponseDTO(updatedPaciente);
    }

    @Transactional
    public PacienteResponseDTO adicionarPontos(Integer pacienteId, BigDecimal pontosAAdicionar, String descricao,
            OrigemTransacaoPonto origem) {
        if (pontosAAdicionar.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("A quantidade de pontos a adicionar deve ser positiva.");
        }

        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new PacienteNaoEncontradoException("Paciente não encontrado."));

        paciente.setSaldoPontos(paciente.getSaldoPontos().add(pontosAAdicionar));

        Paciente updatedPaciente = pacienteRepository.save(paciente);

        TransacaoPonto transacao = new TransacaoPonto();
        transacao.setPaciente(updatedPaciente);
        transacao.setTipo(TipoTransacaoPonto.ENTRADA);
        transacao.setOrigem(origem);
        transacao.setQuantidadePontos(pontosAAdicionar);
        transacao.setDescricao(descricao);
        transacaoPontoRepository.save(transacao);

        return convertToResponseDTO(updatedPaciente);
    }

    public boolean pacientePertenceAoUsuario(Integer pacienteId, Jwt jwt) {
        Integer usuarioId = ((Number) jwt.getClaim("id")).intValue();
        log.info("Verificando se o paciente com ID {} pertence ao usuário com ID {}", pacienteId, usuarioId);

        // 2. Buscar o paciente pelo ID da URL e comparar os CPFs.
        return pacienteRepository.findById(pacienteId)
                .map(paciente -> paciente.getUsuarioId().equals(usuarioId))
                .orElse(false);
    }

    public PacienteResponseDTO buscarPacientePorCpf(String cpf) {
        Paciente paciente = pacienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new PacienteNaoEncontradoException("Paciente não encontrado com CPF: " + cpf));
        return convertToResponseDTO(paciente);
    }

    public PacienteResponseDTO buscarPorId(Integer pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new PacienteNaoEncontradoException("Paciente não encontrado com ID: " + pacienteId));

        BigDecimal saldoDePontos = transacaoPontoRepository.calcularSaldoDePontos(pacienteId);
        log.info("Saldo de pontos para o paciente com ID {}: {}", pacienteId, saldoDePontos);

        return convertToResponseDTO(paciente, saldoDePontos);
    }

    private PacienteResponseDTO convertToResponseDTO(Paciente paciente, BigDecimal saldoDePontos) {
        PacienteResponseDTO dto = new PacienteResponseDTO();
        dto.setId(paciente.getId());
        dto.setCpf(paciente.getCpf());
        dto.setNome(paciente.getNome());
        dto.setEmail(paciente.getEmail());
        dto.setCep(paciente.getCep());
        dto.setLogradouro(paciente.getLogradouro());
        dto.setNumero(paciente.getNumero());
        dto.setComplemento(paciente.getComplemento());
        dto.setBairro(paciente.getBairro());
        dto.setCidade(paciente.getCidade());
        dto.setSaldoPontos(saldoDePontos);
        dto.setUf(paciente.getUf());
        dto.setSaldoPontos(paciente.getSaldoPontos());
        dto.setDataCadastro(paciente.getDataCadastro());
        dto.setAtivo(paciente.isAtivo());
        return dto;
    }

    private PacienteResponseDTO convertToResponseDTO(Paciente paciente) {
        return convertToResponseDTO(paciente, BigDecimal.ZERO); // Ou buscar o saldo aqui também
    }

    private TransacaoPontoDTO convertToTransacaoDTO(TransacaoPonto transacao) {
        TransacaoPontoDTO dto = new TransacaoPontoDTO();
        dto.setId(transacao.getId());
        dto.setDataHora(transacao.getDataHora());
        dto.setTipo(transacao.getTipo());
        dto.setOrigem(transacao.getOrigem());
        dto.setValorReais(transacao.getValorReais());
        dto.setQuantidadePontos(transacao.getQuantidadePontos());
        dto.setDescricao(transacao.getDescricao());
        return dto;
    }

}
