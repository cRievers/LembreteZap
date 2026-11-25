package br.edu.poo2.lembretezap.dao;

import br.edu.poo2.lembretezap.model.Compromisso;
import br.edu.poo2.lembretezap.model.FiltroCompromisso;
import br.edu.poo2.lembretezap.model.SituacaoCompromisso;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementação JDBC (MySQL) de {@link CompromissoDAO} com {@link PreparedStatement},
 * {@code try-with-resources} e instrumentação completa via {@code java.util.logging}.
 */
public class CompromissoJdbcDAO implements CompromissoDAO {

    private static final String CLASS_NAME = CompromissoJdbcDAO.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private static final String SELECT_COLUMNS =
            "codigo, descricao, data_compromisso, horario, nome_contato, telefone, situacao";

    private static final String SQL_INSERT =
            "INSERT INTO compromisso (descricao, data_compromisso, horario, nome_contato, telefone, situacao) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE compromisso SET descricao = ?, data_compromisso = ?, horario = ?, "
                    + "nome_contato = ?, telefone = ?, situacao = ? WHERE codigo = ?";

    private static final String SQL_DELETE =
            "DELETE FROM compromisso WHERE codigo = ?";

    private static final String SQL_SELECT_BY_CODIGO =
            "SELECT " + SELECT_COLUMNS + " FROM compromisso WHERE codigo = ?";

    private static final String SQL_SELECT_BASE =
            "SELECT " + SELECT_COLUMNS + " FROM compromisso";

    @FunctionalInterface
    interface ConnectionSupplier {
        Connection getConnection() throws SQLException;
    }

    private record SqlQuery(String sql, List<Object> parameters) {
    }

    private final ConnectionSupplier connectionSupplier;

    public CompromissoJdbcDAO() {
        this(ConnectionFactory::getConnection);
    }

    CompromissoJdbcDAO(ConnectionSupplier connectionSupplier) {
        this.connectionSupplier = Objects.requireNonNull(
                connectionSupplier, "ConnectionSupplier não pode ser nulo.");
    }

    @Override
    public Compromisso inserir(Compromisso compromisso) {
        final String method = "inserir";
        LOGGER.logp(Level.FINER, CLASS_NAME, method, "ENTRY");
        validarCompromisso(compromisso, false);

        SituacaoCompromisso situacao = (compromisso.getSituacao() != null)
                ? compromisso.getSituacao()
                : SituacaoCompromisso.PENDENTE;
        compromisso.setSituacao(situacao);

        LOGGER.logp(Level.FINEST, CLASS_NAME, method, () -> String.format(
                "SQL: %s | params=[descricao=%s, data=%s, horario=%s, nomeContato=%s, telefone=%s, situacao=%s]",
                SQL_INSERT,
                compromisso.getDescricao(),
                compromisso.getData(),
                compromisso.getHorario(),
                compromisso.getNomeContato(),
                compromisso.getTelefone(),
                situacao.name()));

        try (Connection conn = connectionSupplier.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            preencherParametrosBase(ps, compromisso, situacao);
            int affectedRows = ps.executeUpdate();
            LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                    () -> "Linhas inseridas: " + affectedRows);

            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir compromisso: nenhuma linha afetada.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedCodigo = generatedKeys.getInt(1);
                    compromisso.setCodigo(generatedCodigo);
                    LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                            () -> "Código gerado pelo banco: " + generatedCodigo);
                } else {
                    throw new SQLException("Falha ao inserir compromisso: nenhum código gerado retornado.");
                }
            }

            LOGGER.logp(Level.FINER, CLASS_NAME, method, "RETURN");
            return compromisso;
        } catch (SQLException e) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, method, "Erro SQL ao inserir compromisso.", e);
            throw new IllegalStateException("Erro ao inserir compromisso no banco de dados.", e);
        }
    }

    @Override
    public void atualizar(Compromisso compromisso) {
        final String method = "atualizar";
        LOGGER.logp(Level.FINER, CLASS_NAME, method, "ENTRY");
        validarCompromisso(compromisso, true);

        SituacaoCompromisso situacao = (compromisso.getSituacao() != null)
                ? compromisso.getSituacao()
                : SituacaoCompromisso.PENDENTE;
        compromisso.setSituacao(situacao);

        LOGGER.logp(Level.FINEST, CLASS_NAME, method, () -> String.format(
                "SQL: %s | params=[descricao=%s, data=%s, horario=%s, nomeContato=%s, telefone=%s, situacao=%s, codigo=%d]",
                SQL_UPDATE,
                compromisso.getDescricao(),
                compromisso.getData(),
                compromisso.getHorario(),
                compromisso.getNomeContato(),
                compromisso.getTelefone(),
                situacao.name(),
                compromisso.getCodigo()));

        try (Connection conn = connectionSupplier.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            preencherParametrosBase(ps, compromisso, situacao);
            ps.setInt(7, compromisso.getCodigo());

            int affectedRows = ps.executeUpdate();
            LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                    () -> "Linhas atualizadas: " + affectedRows);

            if (affectedRows == 0) {
                LOGGER.logp(Level.WARNING, CLASS_NAME, method,
                        () -> "Nenhum compromisso encontrado para atualização com código: "
                                + compromisso.getCodigo());
                throw new IllegalStateException(
                        "Nenhum compromisso encontrado com código " + compromisso.getCodigo() + ".");
            }

            LOGGER.logp(Level.FINER, CLASS_NAME, method, "RETURN");
        } catch (SQLException e) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, method,
                    "Erro SQL ao atualizar compromisso código " + compromisso.getCodigo(), e);
            throw new IllegalStateException("Erro ao atualizar compromisso no banco de dados.", e);
        }
    }

    @Override
    public void excluir(Integer codigo) {
        final String method = "excluir";
        LOGGER.logp(Level.FINER, CLASS_NAME, method, "ENTRY");
        validarCodigo(codigo);

        LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                () -> "SQL: " + SQL_DELETE + " | params=[codigo=" + codigo + "]");

        try (Connection conn = connectionSupplier.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, codigo);
            int affectedRows = ps.executeUpdate();
            LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                    () -> "Linhas excluídas: " + affectedRows);

            if (affectedRows == 0) {
                LOGGER.logp(Level.WARNING, CLASS_NAME, method,
                        () -> "Nenhum compromisso encontrado para exclusão com código: " + codigo);
                throw new IllegalStateException(
                        "Nenhum compromisso encontrado com código " + codigo + ".");
            }

            LOGGER.logp(Level.FINER, CLASS_NAME, method, "RETURN");
        } catch (SQLException e) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, method,
                    "Erro SQL ao excluir compromisso código " + codigo, e);
            throw new IllegalStateException("Erro ao excluir compromisso no banco de dados.", e);
        }
    }

    @Override
    public Optional<Compromisso> buscarPorCodigo(Integer codigo) {
        final String method = "buscarPorCodigo";
        LOGGER.logp(Level.FINER, CLASS_NAME, method, "ENTRY");
        validarCodigo(codigo);

        LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                () -> "SQL: " + SQL_SELECT_BY_CODIGO + " | params=[codigo=" + codigo + "]");

        try (Connection conn = connectionSupplier.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_CODIGO)) {

            ps.setInt(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                Optional<Compromisso> resultado = rs.next()
                        ? Optional.of(mapRow(rs))
                        : Optional.empty();
                LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                        () -> "Registro encontrado: " + resultado.isPresent());
                LOGGER.logp(Level.FINER, CLASS_NAME, method, "RETURN");
                return resultado;
            }
        } catch (SQLException e) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, method,
                    "Erro SQL ao buscar compromisso código " + codigo, e);
            throw new IllegalStateException("Erro ao buscar compromisso no banco de dados.", e);
        }
    }

    @Override
    public List<Compromisso> listar(FiltroCompromisso filtro) {
        final String method = "listar";
        LOGGER.logp(Level.FINER, CLASS_NAME, method, "ENTRY");

        FiltroCompromisso filtroEfetivo = (filtro != null) ? filtro : FiltroCompromisso.todos();
        SqlQuery query = buildListarQuery(filtroEfetivo);

        LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                () -> "SQL: " + query.sql() + " | params=" + query.parameters());

        try (Connection conn = connectionSupplier.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.sql())) {

            for (int i = 0; i < query.parameters().size(); i++) {
                Object param = query.parameters().get(i);
                if (param instanceof Date dateParam) {
                    ps.setDate(i + 1, dateParam);
                } else {
                    ps.setString(i + 1, String.valueOf(param));
                }
            }

            List<Compromisso> compromissos = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    compromissos.add(mapRow(rs));
                }
            }

            LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                    () -> "Quantidade de registros retornados: " + compromissos.size());
            LOGGER.logp(Level.FINER, CLASS_NAME, method, "RETURN");
            return compromissos;
        } catch (SQLException e) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, method, "Erro SQL ao listar compromissos.", e);
            throw new IllegalStateException("Erro ao listar compromissos no banco de dados.", e);
        }
    }

    private static SqlQuery buildListarQuery(FiltroCompromisso filtro) {
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        switch (filtro.tipo()) {
            case PENDENTES -> {
                conditions.add("situacao = ?");
                params.add(SituacaoCompromisso.PENDENTE.name());
            }
            case CONCLUIDOS -> {
                conditions.add("situacao = ?");
                params.add(SituacaoCompromisso.CONCLUIDO.name());
            }
            case ATRASADOS -> {
                conditions.add("situacao = ?");
                params.add(SituacaoCompromisso.PENDENTE.name());
                conditions.add("CONCAT(data_compromisso, ' ', horario) < NOW()");
            }
            case TODOS -> {
                // Sem filtro por situação
            }
        }

        if (filtro.hasData()) {
            conditions.add("data_compromisso = ?");
            params.add(Date.valueOf(filtro.data()));
        }

        if (filtro.hasTermoBusca()) {
            conditions.add("(descricao LIKE ? OR nome_contato LIKE ?)");
            String likePattern = "%" + filtro.termoBusca() + "%";
            params.add(likePattern);
            params.add(likePattern);
        }

        StringBuilder sql = new StringBuilder(SQL_SELECT_BASE);
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        sql.append(" ORDER BY data_compromisso ASC, horario ASC, codigo ASC");

        return new SqlQuery(sql.toString(), List.copyOf(params));
    }

    private static void preencherParametrosBase(
            PreparedStatement ps,
            Compromisso compromisso,
            SituacaoCompromisso situacao) throws SQLException {
        ps.setString(1, compromisso.getDescricao());
        ps.setDate(2, Date.valueOf(compromisso.getData()));
        ps.setTime(3, Time.valueOf(compromisso.getHorario()));
        ps.setString(4, compromisso.getNomeContato());
        ps.setString(5, compromisso.getTelefone());
        ps.setString(6, situacao.name());
    }

    private static Compromisso mapRow(ResultSet rs) throws SQLException {
        Date dataSql = rs.getDate("data_compromisso");
        Time horarioSql = rs.getTime("horario");
        String situacaoStr = rs.getString("situacao");

        return new Compromisso(
                rs.getInt("codigo"),
                rs.getString("descricao"),
                dataSql != null ? dataSql.toLocalDate() : null,
                horarioSql != null ? horarioSql.toLocalTime() : null,
                rs.getString("nome_contato"),
                rs.getString("telefone"),
                situacaoStr != null ? SituacaoCompromisso.valueOf(situacaoStr) : SituacaoCompromisso.PENDENTE);
    }

    private static void validarCompromisso(Compromisso compromisso, boolean exigirCodigo) {
        if (compromisso == null) {
            throw new IllegalArgumentException("Compromisso não pode ser nulo.");
        }
        if (exigirCodigo) {
            validarCodigo(compromisso.getCodigo());
        }
        if (compromisso.getDescricao() == null || compromisso.getDescricao().isBlank()) {
            throw new IllegalArgumentException("Descrição do compromisso é obrigatória.");
        }
        if (compromisso.getData() == null) {
            throw new IllegalArgumentException("Data do compromisso é obrigatória.");
        }
        if (compromisso.getHorario() == null) {
            throw new IllegalArgumentException("Horário do compromisso é obrigatório.");
        }
        if (compromisso.getNomeContato() == null || compromisso.getNomeContato().isBlank()) {
            throw new IllegalArgumentException("Nome do contato é obrigatório.");
        }
        if (compromisso.getTelefone() == null || compromisso.getTelefone().isBlank()) {
            throw new IllegalArgumentException("Telefone do contato é obrigatório.");
        }
    }

    private static void validarCodigo(Integer codigo) {
        if (codigo == null || codigo <= 0) {
            throw new IllegalArgumentException("Código do compromisso deve ser um inteiro positivo.");
        }
    }
}
