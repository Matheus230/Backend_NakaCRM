package com.nakacorp.backend.service;

import com.nakacorp.backend.model.Cliente;
import com.nakacorp.backend.model.Produto;
import com.nakacorp.backend.model.enums.OrigemLead;
import com.nakacorp.backend.model.enums.StatusLead;
import com.nakacorp.backend.model.enums.TipoCobranca;
import com.nakacorp.backend.model.enums.TipoPagamento;
import com.nakacorp.backend.repository.ClienteRepository;
import com.nakacorp.backend.repository.ProdutoRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável pela importação de dados a partir de arquivos CSV.
 *
 * Permite importar Clientes e Produtos em lote, validando os dados
 * e tratando erros adequadamente.
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@Service
@Transactional
public class CsvImportService {

    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public CsvImportService(ClienteRepository clienteRepository,
                           ProdutoRepository produtoRepository) {
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
    }

    /**
     * Importa clientes a partir de um arquivo CSV.
     *
     * Formato esperado:
     * Nome, Email, Telefone, Empresa, Cargo, Cidade, Estado, CEP, Endereco, Origem Lead, Status Lead, Observacoes
     *
     * @param file arquivo CSV
     * @return resultado da importação
     * @throws IOException em caso de erro na leitura
     */
    public ImportResult importarClientes(MultipartFile file) throws IOException {
        List<String> erros = new ArrayList<>();
        int sucessos = 0;
        int linhaAtual = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build())) {

            for (CSVRecord record : parser) {
                linhaAtual++;
                try {
                    Cliente cliente = new Cliente();

                    cliente.setNome(record.get("Nome"));
                    cliente.setEmail(record.get("Email"));

                    cliente.setTelefone(getOrNull(record, "Telefone"));
                    cliente.setEmpresa(getOrNull(record, "Empresa"));
                    cliente.setCargo(getOrNull(record, "Cargo"));
                    cliente.setCidade(getOrNull(record, "Cidade"));
                    cliente.setEstado(getOrNull(record, "Estado"));
                    cliente.setCep(getOrNull(record, "CEP"));
                    cliente.setEndereco(getOrNull(record, "Endereco"));
                    cliente.setObservacoes(getOrNull(record, "Observacoes"));

                    String origemLead = getOrNull(record, "Origem Lead");
                    cliente.setOrigemLead(origemLead != null ?
                        OrigemLead.valueOf(origemLead.toUpperCase()) : OrigemLead.MANUAL);

                    String statusLead = getOrNull(record, "Status Lead");
                    cliente.setStatusLead(statusLead != null ?
                        StatusLead.valueOf(statusLead.toUpperCase()) : StatusLead.NOVO);

                    if (cliente.getNome() == null || cliente.getNome().isBlank()) {
                        erros.add("Linha " + linhaAtual + ": Nome é obrigatório");
                        continue;
                    }

                    if (cliente.getEmail() == null || cliente.getEmail().isBlank()) {
                        erros.add("Linha " + linhaAtual + ": Email é obrigatório");
                        continue;
                    }

                    if (clienteRepository.existsByEmail(cliente.getEmail())) {
                        erros.add("Linha " + linhaAtual + ": Email já cadastrado - " + cliente.getEmail());
                        continue;
                    }

                    clienteRepository.save(cliente);
                    sucessos++;

                } catch (IllegalArgumentException e) {
                    erros.add("Linha " + linhaAtual + ": Valor inválido - " + e.getMessage());
                } catch (Exception e) {
                    erros.add("Linha " + linhaAtual + ": Erro ao processar - " + e.getMessage());
                }
            }
        }

        return new ImportResult(sucessos, erros.size(), erros);
    }

    /**
     * Importa produtos a partir de um arquivo CSV.
     *
     * Formato esperado:
     * Nome, Descricao, Categoria, Preco, Tipo Cobranca, Tipo Pagamento, Ativo
     *
     * @param file arquivo CSV
     * @return resultado da importação
     * @throws IOException em caso de erro na leitura
     */
    public ImportResult importarProdutos(MultipartFile file) throws IOException {
        List<String> erros = new ArrayList<>();
        int sucessos = 0;
        int linhaAtual = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build())) {

            for (CSVRecord record : parser) {
                linhaAtual++;
                try {
                    Produto produto = new Produto();

                    produto.setNome(record.get("Nome"));
                    produto.setDescricao(getOrNull(record, "Descricao"));
                    produto.setCategoria(getOrNull(record, "Categoria"));

                    String precoStr = getOrNull(record, "Preco");
                    if (precoStr != null && !precoStr.isBlank()) {
                        produto.setPreco(new BigDecimal(precoStr.replace(",", ".")));
                    }

                    String tipoCobranca = getOrNull(record, "Tipo Cobranca");
                    produto.setTipoCobranca(tipoCobranca != null ?
                        TipoCobranca.valueOf(tipoCobranca.toUpperCase()) : TipoCobranca.UNICO);

                    String tipoPagamento = getOrNull(record, "Tipo Pagamento");
                    produto.setTipoPagamento(tipoPagamento != null ?
                        TipoPagamento.valueOf(tipoPagamento.toUpperCase()) : TipoPagamento.CARTAO);

                    String ativoStr = getOrNull(record, "Ativo");
                    produto.setAtivo(ativoStr == null || ativoStr.equalsIgnoreCase("true") || ativoStr.equals("1"));

                    if (produto.getNome() == null || produto.getNome().isBlank()) {
                        erros.add("Linha " + linhaAtual + ": Nome é obrigatório");
                        continue;
                    }

                    if (produtoRepository.existsByNome(produto.getNome())) {
                        erros.add("Linha " + linhaAtual + ": Produto já cadastrado - " + produto.getNome());
                        continue;
                    }

                    produtoRepository.save(produto);
                    sucessos++;

                } catch (IllegalArgumentException e) {
                    erros.add("Linha " + linhaAtual + ": Valor inválido - " + e.getMessage());
                } catch (Exception e) {
                    erros.add("Linha " + linhaAtual + ": Erro ao processar - " + e.getMessage());
                }
            }
        }

        return new ImportResult(sucessos, erros.size(), erros);
    }

    /**
     * Obtém valor de uma coluna ou retorna null se vazia.
     */
    private String getOrNull(CSVRecord record, String column) {
        try {
            String value = record.get(column);
            return (value != null && !value.isBlank()) ? value : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Classe para representar o resultado de uma importação.
     */
    public static class ImportResult {
        private final int sucessos;
        private final int erros;
        private final List<String> mensagensErro;

        public ImportResult(int sucessos, int erros, List<String> mensagensErro) {
            this.sucessos = sucessos;
            this.erros = erros;
            this.mensagensErro = mensagensErro;
        }

        public int getSucessos() {
            return sucessos;
        }

        public int getErros() {
            return erros;
        }

        public List<String> getMensagensErro() {
            return mensagensErro;
        }

        public int getTotal() {
            return sucessos + erros;
        }
    }
}
