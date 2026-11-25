package br.edu.poo2.lembretezap.logging;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Formatador customizado de registros de log ({@link LogRecord}) para saída em arquivo e console,
 * seguindo o formato de uma linha por registro delimitada por {@code ::}.
 */
public class MyFormatter extends Formatter {

    @Override
    @SuppressWarnings("deprecation")
    public String format(LogRecord record) {
        return record.getThreadID() + "::"
                + record.getLevel() + "::"
                + record.getSourceClassName() + "::"
                + record.getSourceMethodName() + "::"
                + new Date(record.getMillis()) + "::"
                + record.getMessage() + "\n";
    }
}
