package com.nakacorp.backend.service;

import com.nakacorp.backend.dto.req.InteracaoClienteRequestDto;
import com.nakacorp.backend.dto.req.InteracaoClienteUpdateDto;
import com.nakacorp.backend.dto.res.InteracaoClienteResponseDto;
import com.nakacorp.backend.dto.res.InteracaoStatsDto;
import com.nakacorp.backend.dto.res.TimelineClienteDto;
import com.nakacorp.backend.model.Cliente;
import com.nakacorp.backend.model.InteracaoCliente;
import com.nakacorp.backend.model.Usuario;
import com.nakacorp.backend.model.enums.NivelInteresse;
import com.nakacorp.backend.model.enums.StatusLead;
import com.nakacorp.backend.model.enums.TipoInteracao;
import com.nakacorp.backend.repository.ClienteRepository;
import com.nakacorp.backend.repository.InteracaoClienteRepository;
import com.nakacorp.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class InteracaoClienteService {

    private final InteracaoClienteRepository interacaoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public InteracaoClienteService(
            InteracaoClienteRepository interacaoRepository,
            ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository) {
        this.interacaoRepository = interacaoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Page<InteracaoClienteResponseDto> findAll(Pageable pageable) {
        return interacaoRepository.findAll(pageable)
                .map(InteracaoClienteResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<InteracaoClienteResponseDto> findById(Long id) {
        return interacaoRepository.findById(id)
                .map(InteracaoClienteResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<TimelineClienteDto> getTimelineCliente(Long clienteId) {
        return interacaoRepository.findByClienteIdOrderByCreatedAtDesc(clienteId)
                .stream()
                .map(TimelineClienteDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InteracaoClienteResponseDto> findByUsuario(Long usuarioId) {
        return interacaoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(InteracaoClienteResponseDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InteracaoClienteResponseDto> findByTipo(TipoInteracao tipo) {
        return interacaoRepository.findByTipoInteracao(tipo)
                .stream()
                .map(InteracaoClienteResponseDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InteracaoClienteResponseDto> findByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return interacaoRepository.findByCreatedAtBetween(inicio, fim)
                .stream()
                .map(InteracaoClienteResponseDto::fromEntity)
                .toList();
    }

    public InteracaoClienteResponseDto create(InteracaoClienteRequestDto request) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + request.clienteId()));

        Usuario usuario = null;
        if (request.usuarioId() != null) {
            usuario = usuarioRepository.findById(request.usuarioId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + request.usuarioId()));
        }

        InteracaoCliente interacao = new InteracaoCliente(cliente, request.tipoInteracao(), request.descricao());
        interacao.setUsuario(usuario);
        interacao.setDadosExtras(request.dadosExtras());

        InteracaoCliente saved = interacaoRepository.save(interacao);

        cliente.setDataUltimaInteracao(LocalDateTime.now());
        clienteRepository.save(cliente);

        return InteracaoClienteResponseDto.fromEntity(saved);
    }

    public InteracaoClienteResponseDto update(Long id, InteracaoClienteUpdateDto request) {
        InteracaoCliente interacao = interacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Interação não encontrada: " + id));

        if (request.tipoInteracao() != null) interacao.setTipoInteracao(request.tipoInteracao());
        if (request.descricao() != null) interacao.setDescricao(request.descricao());
        if (request.dadosExtras() != null) interacao.setDadosExtras(request.dadosExtras());

        InteracaoCliente updated = interacaoRepository.save(interacao);
        return InteracaoClienteResponseDto.fromEntity(updated);
    }

    public void delete(Long id) {
        if (!interacaoRepository.existsById(id)) {
            throw new IllegalArgumentException("Interação não encontrada: " + id);
        }
        interacaoRepository.deleteById(id);
    }

    public void registrarPrimeiroContato(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + clienteId));

        Map<String, Object> dadosExtras = new HashMap<>();
        dadosExtras.put("automatico", true);
        dadosExtras.put("origem", cliente.getOrigemLead().toString());

        InteracaoCliente interacao = new InteracaoCliente(
                cliente,
                TipoInteracao.NOTA_INTERNA,
                "Cliente cadastrado no sistema - Primeiro contato registrado"
        );
        interacao.setDadosExtras(dadosExtras);

        interacaoRepository.save(interacao);

        if (cliente.getDataPrimeiroContato() == null) {
            cliente.setDataPrimeiroContato(LocalDateTime.now());
            clienteRepository.save(cliente);
        }
    }

    public void registrarMudancaStatus(Long clienteId, StatusLead statusAnterior, StatusLead novoStatus) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + clienteId));

        Map<String, Object> dadosExtras = new HashMap<>();
        dadosExtras.put("automatico", true);
        dadosExtras.put("statusAnterior", statusAnterior.toString());
        dadosExtras.put("novoStatus", novoStatus.toString());

        InteracaoCliente interacao = new InteracaoCliente(
                cliente,
                TipoInteracao.NOTA_INTERNA,
                String.format("Status alterado de %s para %s", statusAnterior, novoStatus)
        );
        interacao.setDadosExtras(dadosExtras);

        interacaoRepository.save(interacao);
    }

    public void registrarEmail(Long clienteId, String assunto, String remetente, Long usuarioId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + clienteId));

        Usuario usuario = usuarioId != null ?
                usuarioRepository.findById(usuarioId).orElse(null) : null;

        Map<String, Object> dadosExtras = new HashMap<>();
        dadosExtras.put("assunto", assunto);
        dadosExtras.put("remetente", remetente);
        dadosExtras.put("automatico", false);

        InteracaoCliente interacao = new InteracaoCliente(
                cliente,
                TipoInteracao.EMAIL,
                String.format("Email enviado: %s", assunto)
        );
        interacao.setUsuario(usuario);
        interacao.setDadosExtras(dadosExtras);

        interacaoRepository.save(interacao);

        cliente.setDataUltimaInteracao(LocalDateTime.now());
        clienteRepository.save(cliente);
    }

    public void registrarTelefone(Long clienteId, String numeroTelefone, String duracao, Long usuarioId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + clienteId));

        Usuario usuario = usuarioId != null ?
                usuarioRepository.findById(usuarioId).orElse(null) : null;

        Map<String, Object> dadosExtras = new HashMap<>();
        dadosExtras.put("telefone", numeroTelefone);
        dadosExtras.put("duracao", duracao);
        dadosExtras.put("automatico", false);

        InteracaoCliente interacao = new InteracaoCliente(
                cliente,
                TipoInteracao.TELEFONE,
                String.format("Ligação telefônica realizada - Duração: %s", duracao)
        );
        interacao.setUsuario(usuario);
        interacao.setDadosExtras(dadosExtras);

        interacaoRepository.save(interacao);

        cliente.setDataUltimaInteracao(LocalDateTime.now());
        clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public InteracaoStatsDto getStats() {
        List<InteracaoCliente> todasInteracoes = interacaoRepository.findAll();

        long total = todasInteracoes.size();
        long emails = todasInteracoes.stream()
                .filter(i -> i.getTipoInteracao() == TipoInteracao.EMAIL)
                .count();
        long telefones = todasInteracoes.stream()
                .filter(i -> i.getTipoInteracao() == TipoInteracao.TELEFONE)
                .count();
        long whatsapp = todasInteracoes.stream()
                .filter(i -> i.getTipoInteracao() == TipoInteracao.WHATSAPP)
                .count();
        long forms = todasInteracoes.stream()
                .filter(i -> i.getTipoInteracao() == TipoInteracao.FORM_SUBMIT)
                .count();
        long visits = todasInteracoes.stream()
                .filter(i -> i.getTipoInteracao() == TipoInteracao.SITE_VISIT)
                .count();
        long notas = todasInteracoes.stream()
                .filter(i -> i.getTipoInteracao() == TipoInteracao.NOTA_INTERNA)
                .count();

        return new InteracaoStatsDto(total, emails, telefones, whatsapp, forms, visits, notas);
    }

    public void registrarInteresseProduto(Long clienteId, String nomeProduto, NivelInteresse nivel) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + clienteId));

        Map<String, Object> dadosExtras = new HashMap<>();
        dadosExtras.put("automatico", true);
        dadosExtras.put("produto", nomeProduto);
        dadosExtras.put("nivelInteresse", nivel.toString());

        InteracaoCliente interacao = new InteracaoCliente(
                cliente,
                TipoInteracao.NOTA_INTERNA,
                String.format("Cliente demonstrou interesse %s no produto: %s", nivel.toString().toLowerCase(), nomeProduto)
        );
        interacao.setDadosExtras(dadosExtras);

        interacaoRepository.save(interacao);

        cliente.setDataUltimaInteracao(LocalDateTime.now());
        clienteRepository.save(cliente);
    }

    public void registrarMudancaNivelInteresse(Long clienteId, String nomeProduto,
                                               NivelInteresse nivelAnterior,
                                               NivelInteresse novoNivel) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + clienteId));

        Map<String, Object> dadosExtras = new HashMap<>();
        dadosExtras.put("automatico", true);
        dadosExtras.put("produto", nomeProduto);
        dadosExtras.put("nivelAnterior", nivelAnterior.toString());
        dadosExtras.put("novoNivel", novoNivel.toString());

        InteracaoCliente interacao = new InteracaoCliente(
                cliente,
                TipoInteracao.NOTA_INTERNA,
                String.format("Nível de interesse alterado de %s para %s no produto: %s",
                        nivelAnterior.toString().toLowerCase(),
                        novoNivel.toString().toLowerCase(),
                        nomeProduto)
        );
        interacao.setDadosExtras(dadosExtras);

        interacaoRepository.save(interacao);

        cliente.setDataUltimaInteracao(LocalDateTime.now());
        clienteRepository.save(cliente);
    }

    public void registrarRemocaoInteresse(Long clienteId, String nomeProduto) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + clienteId));

        Map<String, Object> dadosExtras = new HashMap<>();
        dadosExtras.put("automatico", true);
        dadosExtras.put("produto", nomeProduto);

        InteracaoCliente interacao = new InteracaoCliente(
                cliente,
                TipoInteracao.NOTA_INTERNA,
                String.format("Interesse removido do produto: %s", nomeProduto)
        );
        interacao.setDadosExtras(dadosExtras);

        interacaoRepository.save(interacao);

        cliente.setDataUltimaInteracao(LocalDateTime.now());
        clienteRepository.save(cliente);
    }
}