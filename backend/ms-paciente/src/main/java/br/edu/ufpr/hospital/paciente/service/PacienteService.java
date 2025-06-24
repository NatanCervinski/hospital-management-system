package br.edu.ufpr.hospital.paciente.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
import br.edu.ufpr.hospital.paciente.util.ViaCepClient;
import jakarta.transaction.Transactional;
import reactor.core.publisher.Mono;

@Service
public class PacienteService {
    private final PacienteRepository pacienteRepository;
    private final TransacaoPontoRepository transacaoPontoRepository;
    private final ViaCepClient viaCepClient;

    private final BigDecimal VALOR_PONTO_REAIS = new BigDecimal("5.00");

    public PacienteService(PacienteRepository pacienteRepository,
                           TransacaoPontoRepository transacaoPontoRepository,
                           ViaCepClient viaCepClient) {
        this.pacienteRepository = pacienteRepository;
        this.transacaoPontoRepository = transacaoPontoRepository;
        this.viaCepClient = viaCepClient;
    }

    @Transactional
    public Mono<PacienteResponseDTO> cadastrarPaciente(PacienteCadastroDTO dto, Integer usuarioId) {
        if (pacienteRepository.existsByCpf(dto.getCpf())) {
            throw new NegocioException("CPF já cadastrado.");
        }
        if (pacienteRepository.existsByEmail(dto.getEmail())) {
            throw new NegocioException("Email já cadastrado.");
        }
        return viaCepClient.buscarEnderecoPorCep(dto.getCep())
                .flatMap(enderecoDTO -> {
                    if (enderecoDTO.getCep() == null) { // ViaCEP retorna CEP nulo se não encontrar
                        return Mono.error(new NegocioException("CEP não encontrado ou inválido."));
                    }

                    Paciente paciente = new Paciente();
                    paciente.setCpf(dto.getCpf());
                    paciente.setNome(dto.getNome());
                    paciente.setEmail(dto.getEmail());
                    paciente.setCep(dto.getCep());
                    paciente.setLogradouro(enderecoDTO.getLogradouro());
                    paciente.setBairro(enderecoDTO.getBairro());
                    paciente.setLocalidade(enderecoDTO.getLocalidade());
                    paciente.setUf(enderecoDTO.getUf());
                    // Número e complemento não vêm do ViaCEP automaticamente, o usuário adicionaria depois ou em outra etapa
                    // paciente.setNumero(dto.getNumero());
                    // paciente.setComplemento(dto.getComplemento());
                    paciente.setSaldoPontos(BigDecimal.ZERO); // Inicia com 0 pontos

                    paciente.setUsuarioId(usuarioId);
                    Paciente savedPaciente = pacienteRepository.save(paciente);

                    // *****************************************************************
                    // ATENÇÃO: Envio de senha por e-mail (R01)
                    // Este microsserviço NÃO deve enviar e-mail diretamente.
                    // Ele deve ENVIAR UM EVENTO ou chamar um endpoint de um
                    // MS de Notificação/Autenticação para que a senha seja gerada e enviada.
                    // Por exemplo:
                    // msNotificacaoClient.enviarSenha(paciente.getEmail(), senhaGerada);
                    // *****************************************************************
                    System.out.println("DEBUG: Paciente cadastrado. Lógica de envio de senha por e-mail deve ser implementada via MS de Notificação/Autenticação.");

                    return Mono.just(convertToResponseDTO(savedPaciente));
                })
                .switchIfEmpty(Mono.error(new NegocioException("Não foi possível buscar o endereço para o CEP informado.")));
    }

    @Transactional
    public PacienteResponseDTO comprarPontos(UUID pacienteId, CompraPontosDTO dto) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new PacienteNaoEncontradoException("Paciente não encontrado."));

        if (dto.getValorReais().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("O valor para compra de pontos deve ser positivo.");
        }

        // 1 ponto = R$ 5,00
        BigDecimal quantidadePontos = dto.getValorReais().divide(VALOR_PONTO_REAIS, 2, BigDecimal.ROUND_DOWN);
        if (quantidadePontos.compareTo(BigDecimal.ZERO) <= 0) {
             throw new NegocioException("Valor insuficiente para comprar pontos. O valor mínimo é R$ " + VALOR_PONTO_REAIS);
        }

        paciente.setSaldoPontos(paciente.getSaldoPontos().add(quantidadePontos));
        Paciente updatedPaciente = pacienteRepository.save(paciente);

        TransacaoPonto transacao = new TransacaoPonto();
        transacao.setPaciente(updatedPaciente);
        transacao.setTipo(TipoTransacaoPonto.ENTRADA);
        transacao.setOrigem(OrigemTransacaoPonto.COMPRA);
        transacao.setValorReais(dto.getValorReais());
        transacao.setQuantidadePontos(quantidadePontos);
        transacao.setDescricao("COMPRA DE PONTOS - Valor: R$ " + dto.getValorReais() + " | Pontos: " + quantidadePontos);
        transacaoPontoRepository.save(transacao);

        return convertToResponseDTO(updatedPaciente);
    }

    public SaldoPontosDTO consultarSaldoEHistorico(UUID pacienteId) {
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
    public PacienteResponseDTO deduzirPontos(UUID pacienteId, BigDecimal pontosADeduzir, String descricao) {
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
    public PacienteResponseDTO adicionarPontos(UUID pacienteId, BigDecimal pontosAAdicionar, String descricao, OrigemTransacaoPonto origem) {
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

    public boolean pacientePertenceAoUsuario(UUID pacienteId, Integer usuarioId) {
    return pacienteRepository.findById(pacienteId)
            .map(p -> p.getUsuarioId().equals(usuarioId))
            .orElse(false);
    }


    // Métodos utilitários para conversão

    
    private PacienteResponseDTO convertToResponseDTO(Paciente paciente) {
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
        dto.setLocalidade(paciente.getLocalidade());
        dto.setUf(paciente.getUf());
        dto.setSaldoPontos(paciente.getSaldoPontos());
        dto.setDataCadastro(paciente.getDataCadastro());
        dto.setAtivo(paciente.isAtivo());
        return dto;
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
