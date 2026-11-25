package br.edu.poo2.lembretezap.logging;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuração centralizada do subsistema de logging ({@code java.util.logging}) da aplicação.
 * Anexa {@link FileHandler} e {@link ConsoleHandler} ao logger raiz do pacote com nível
 * {@link Level#ALL}, {@link MyFormatter} e {@link DebugLogFilter}.
 */
public final class LogConfig {

    public static final String ROOT_LOGGER_NAME = "br.edu.poo2.lembretezap";
    public static final String LOG_DIRECTORY = "logs";
    public static final String LOG_FILE = "logs/lembretezap.log";

    private static boolean initialized = false;

    private LogConfig() {
    }

    /**
     * Inicializa o logger raiz da aplicação de forma idempotente, garantindo a criação do
     * diretório de logs e o registro dos handlers de arquivo e console.
     */
    public static synchronized void init() {
        Logger rootLogger = Logger.getLogger(ROOT_LOGGER_NAME);
        if (initialized && rootLogger.getHandlers().length > 0) {
            return;
        }

        try {
            Files.createDirectories(Path.of(LOG_DIRECTORY));

            rootLogger.setLevel(Level.ALL);
            rootLogger.setUseParentHandlers(false);

            MyFormatter formatter = new MyFormatter();
            DebugLogFilter filter = new DebugLogFilter();

            FileHandler fileHandler = new FileHandler(LOG_FILE, true);
            configureHandler(fileHandler, formatter, filter);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            configureHandler(consoleHandler, formatter, filter);

            rootLogger.addHandler(fileHandler);
            rootLogger.addHandler(consoleHandler);

            initialized = true;
            rootLogger.logp(Level.FINE, LogConfig.class.getName(), "init",
                    "Subsistema de logging inicializado com sucesso.");
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao inicializar subsistema de logging", e);
        }
    }

    private static void configureHandler(Handler handler, MyFormatter formatter, DebugLogFilter filter)
            throws IOException {
        handler.setLevel(Level.ALL);
        handler.setFormatter(formatter);
        handler.setFilter(filter);
        handler.setEncoding(StandardCharsets.UTF_8.name());
    }
}
