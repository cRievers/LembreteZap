package br.edu.poo2.lembretezap.logging;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Filtro de logs ({@link Filter}) que restringe o processamento aos registros originados
 * no pacote da aplicação ({@code br.edu.poo2.lembretezap}) ou com severidade igual ou
 * superior a {@link Level#WARNING}.
 */
public class DebugLogFilter implements Filter {

    public static final String APP_PACKAGE_PREFIX = "br.edu.poo2.lembretezap";

    @Override
    public boolean isLoggable(LogRecord record) {
        if (record == null) {
            return false;
        }
        Level level = record.getLevel();
        if (level != null && level.intValue() >= Level.WARNING.intValue()) {
            return true;
        }
        String loggerName = record.getLoggerName();
        return loggerName != null && loggerName.startsWith(APP_PACKAGE_PREFIX);
    }
}
