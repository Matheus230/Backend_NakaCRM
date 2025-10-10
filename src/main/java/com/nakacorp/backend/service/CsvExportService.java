package com.nakacorp.backend.service;

import com.nakacorp.backend.model.Cliente;
import com.nakacorp.backend.model.Produto;
import com.nakacorp.backend.model.InteracaoCliente;
import com.nakacorp.backend.repository.ClienteRepository;
import com.nakacorp.backend.repository.ProdutoRepository;
import com.nakacorp.backend.repository.InteracaoClienteRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço responsável pela exportação de dados em formato CSV.
 *
 * Permite exportar dados de Clientes, Produtos e Interações para arquivos CSV
 * com encoding UTF-8 e formato compatível com Excel.
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
public class CsvExportService {

    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final InteracaoClienteRepository interacaoRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public CsvExportService(ClienteRepository clienteRepository,
                           ProdutoRepository produtoRepository,
                           InteracaoClienteRepository interacaoRepository) {
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.interacaoRepository = interacaoRepository;
    }

    /**
     * Exporta todos os clientes para CSV.
     *
     * @return bytes do arquivo CSV
     * @throws IOException em caso de erro na geração
     */
    public byte[] exportarClientes() throws IOException {
        List<Cliente> clientes = clienteRepository.findAll();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL
                     .builder()
                     .setHeader("ID", "Nome", "Email", "Telefone", "Empresa", "Cargo",
                               "Cidade", "Estado", "CEP", "Endereco", "Origem Lead",
                               "Status Lead", "Observacoes", "Data Primeiro Contato",
                               "Data Ultima Interacao", "Criado Em", "Atualizado Em")
                     .build())) {

            for (Cliente cliente : clientes) {
                printer.printRecord(
                    cliente.getId(),
                    cliente.getNome(),
                    cliente.getEmail(),
                    cliente.getTelefone(),
                    cliente.getEmpresa(),
                    cliente.getCargo(),
                    cliente.getCidade(),
                    cliente.getEstado(),
                    cliente.getCep(),
                    cliente.getEndereco(),
                    cliente.getOrigemLead(),
                    cliente.getStatusLead(),
                    cliente.getObservacoes(),
                    cliente.getDataPrimeiroContato() != null ?
                        cliente.getDataPrimeiroContato().format(DATE_FORMATTER) : "",
                    cliente.getDataUltimaInteracao() != null ?
                        cliente.getDataUltimaInteracao().format(DATE_FORMATTER) : "",
                    cliente.getCreatedAt() != null ?
                        cliente.getCreatedAt().format(DATE_FORMATTER) : "",
                    cliente.getUpdatedAt() != null ?
                        cliente.getUpdatedAt().format(DATE_FORMATTER) : ""
                );
            }

            printer.flush();
            return out.toByteArray();
        }
    }

    /**
     * Exporta todos os produtos para CSV.
     *
     * @return bytes do arquivo CSV
     * @throws IOException em caso de erro na geração
     */
    public byte[] exportarProdutos() throws IOException {
        List<Produto> produtos = produtoRepository.findAll();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL
                     .builder()
                     .setHeader("ID", "Nome", "Descricao", "Categoria", "Preco",
                               "Tipo Cobranca", "Tipo Pagamento", "Ativo",
                               "Criado Em", "Atualizado Em")
                     .build())) {

            for (Produto produto : produtos) {
                printer.printRecord(
                    produto.getId(),
                    produto.getNome(),
                    produto.getDescricao(),
                    produto.getCategoria(),
                    produto.getPreco(),
                    produto.getTipoCobranca(),
                    produto.getTipoPagamento(),
                    produto.getAtivo(),
                    produto.getCreatedAt() != null ?
                        produto.getCreatedAt().format(DATE_FORMATTER) : "",
                    produto.getUpdatedAt() != null ?
                        produto.getUpdatedAt().format(DATE_FORMATTER) : ""
                );
            }

            printer.flush();
            return out.toByteArray();
        }
    }

    /**
     * Exporta todas as interações para CSV.
     *
     * @return bytes do arquivo CSV
     * @throws IOException em caso de erro na geração
     */
    public byte[] exportarInteracoes() throws IOException {
        List<InteracaoCliente> interacoes = interacaoRepository.findAll();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL
                     .builder()
                     .setHeader("ID", "Cliente ID", "Cliente Nome", "Usuario ID",
                               "Usuario Nome", "Tipo Interacao", "Descricao",
                               "Criado Em")
                     .build())) {

            for (InteracaoCliente interacao : interacoes) {
                printer.printRecord(
                    interacao.getId(),
                    interacao.getCliente().getId(),
                    interacao.getCliente().getNome(),
                    interacao.getUsuario() != null ? interacao.getUsuario().getId() : "",
                    interacao.getUsuario() != null ? interacao.getUsuario().getNome() : "",
                    interacao.getTipoInteracao(),
                    interacao.getDescricao(),
                    interacao.getCreatedAt() != null ?
                        interacao.getCreatedAt().format(DATE_FORMATTER) : ""
                );
            }

            printer.flush();
            return out.toByteArray();
        }
    }

    /**
     * Exporta dados completos do sistema (clientes + produtos + interações).
     *
     * @return bytes do arquivo CSV combinado
     * @throws IOException em caso de erro na geração
     */
    public byte[] exportarTodosDados() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {

            writer.write("CLIENTES\n");
            writer.flush();
            out.write(exportarClientes());

            writer.write("\n\nPRODUTOS\n");
            writer.flush();
            out.write(exportarProdutos());

            writer.write("\n\nINTERAÇÕES\n");
            writer.flush();
            out.write(exportarInteracoes());

            return out.toByteArray();
        }
    }
}
