package br.edu.poo2.lembretezap.logging;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogConfigTest {

    @Test
    void debugLogFilterDeveFiltrarPorPacoteENivel() {
        DebugLogFilter filter = new DebugLogFilter();

        LogRecord appFine = new LogRecord(Level.FINE, "evento interno");
        appFine.setLoggerName("br.edu.poo2.lembretezap.view.TelaPrincipalFrame");
        assertTrue(filter.isLoggable(appFine));

        LogRecord externalFine = new LogRecord(Level.FINE, "ruido externo");
        externalFine.setLoggerName("java.awt.Component");
        assertFalse(filter.isLoggable(externalFine));

        LogRecord externalWarning = new LogRecord(Level.WARNING, "aviso externo");
        externalWarning.setLoggerName("java.sql.DriverManager");
        assertTrue(filter.isLoggable(externalWarning));

        assertFalse(filter.isLoggable(null));
    }

    @Test
    void initDeveSerIdempotenteEGerarArquivoDeLogFormatado() throws IOException {
        LogConfig.init();
        LogConfig.init();

        Logger rootLogger = Logger.getLogger(LogConfig.ROOT_LOGGER_NAME);
        assertEquals(Level.ALL, rootLogger.getLevel());
        assertFalse(rootLogger.getUseParentHandlers());
        assertEquals(2, rootLogger.getHandlers().length);

        Logger childLogger = Logger.getLogger(LogConfigTest.class.getName());
        String marker = "teste-logconfig-finest-" + System.nanoTime();
        childLogger.logp(Level.FINEST, LogConfigTest.class.getName(),
                "initDeveSerIdempotenteEGerarArquivoDeLogFormatado", marker);

        Path logPath = Path.of(LogConfig.LOG_FILE);
        assertTrue(Files.exists(logPath), "Arquivo logs/lembretezap.log deve existir");

        String content = Files.readString(logPath, StandardCharsets.UTF_8);
        assertTrue(content.contains("::FINEST::" + LogConfigTest.class.getName()
                + "::initDeveSerIdempotenteEGerarArquivoDeLogFormatado::"));
        assertTrue(content.contains("::" + marker));
    }
}
