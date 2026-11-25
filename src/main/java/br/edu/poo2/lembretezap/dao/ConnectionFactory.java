package br.edu.poo2.lembretezap.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fábrica de conexões JDBC com o banco de dados MySQL a partir das propriedades
 * definidas em {@code db.properties} no classpath.
 */
public final class ConnectionFactory {

    private static final String CLASS_NAME = ConnectionFactory.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private static final String DEFAULT_PROPERTIES_RESOURCE = "/db.properties";

    private ConnectionFactory() {
    }

    /**
     * Abre uma nova conexão JDBC utilizando o arquivo padrão {@code /db.properties}.
     *
     * @return conexão ativa com o banco de dados
     * @throws SQLException caso ocorra falha ao estabelecer a conexão JDBC
     */
    public static Connection getConnection() throws SQLException {
        return getConnection(DEFAULT_PROPERTIES_RESOURCE);
    }

    static Connection getConnection(String propertiesResource) throws SQLException {
        LOGGER.logp(Level.FINER, CLASS_NAME, "getConnection", "ENTRY");

        Properties props = loadProperties(propertiesResource);
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password", "");

        if (url == null || url.isBlank()) {
            IllegalStateException ex = new IllegalStateException(
                    "Propriedade obrigatória 'db.url' ausente ou vazia em " + propertiesResource);
            LOGGER.logp(Level.SEVERE, CLASS_NAME, "getConnection", ex.getMessage(), ex);
            throw ex;
        }
        if (user == null) {
            IllegalStateException ex = new IllegalStateException(
                    "Propriedade obrigatória 'db.user' ausente em " + propertiesResource);
            LOGGER.logp(Level.SEVERE, CLASS_NAME, "getConnection", ex.getMessage(), ex);
            throw ex;
        }

        LOGGER.logp(Level.FINEST, CLASS_NAME, "getConnection",
                () -> "Abrindo conexão JDBC [resource=" + propertiesResource
                        + ", url=" + url + ", user=" + user + "]");

        try {
            Connection connection = DriverManager.getConnection(url.trim(), user.trim(), password);
            LOGGER.logp(Level.FINEST, CLASS_NAME, "getConnection", "Conexão JDBC estabelecida com sucesso.");
            LOGGER.logp(Level.FINER, CLASS_NAME, "getConnection", "RETURN");
            return connection;
        } catch (SQLException e) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, "getConnection",
                    "Erro ao conectar ao banco de dados em " + url, e);
            throw e;
        }
    }

    static Properties loadProperties(String propertiesResource) {
        if (propertiesResource == null || propertiesResource.isBlank()) {
            throw new IllegalArgumentException("Caminho do arquivo de propriedades não pode ser vazio.");
        }

        Properties properties = new Properties();
        try (InputStream input = ConnectionFactory.class.getResourceAsStream(propertiesResource)) {
            if (input == null) {
                IllegalStateException ex = new IllegalStateException(
                        "Arquivo de configuração não encontrado no classpath: " + propertiesResource);
                LOGGER.logp(Level.SEVERE, CLASS_NAME, "loadProperties", ex.getMessage(), ex);
                throw ex;
            }
            properties.load(input);
            LOGGER.logp(Level.FINEST, CLASS_NAME, "loadProperties",
                    () -> "Propriedades carregadas de " + propertiesResource);
            return properties;
        } catch (IOException e) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, "loadProperties",
                    "Falha de leitura em " + propertiesResource, e);
            throw new IllegalStateException("Não foi possível ler o arquivo de configuração " + propertiesResource, e);
        }
    }
}
