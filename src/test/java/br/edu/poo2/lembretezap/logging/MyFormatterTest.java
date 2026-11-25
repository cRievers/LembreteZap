package br.edu.poo2.lembretezap.logging;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MyFormatterTest {

    @Test
    @SuppressWarnings("deprecation")
    void deveFormatarLogRecordComDelimitadoresEQuebraDeLinha() {
        MyFormatter formatter = new MyFormatter();
        LogRecord record = new LogRecord(Level.FINE, "Compromisso cadastrado com sucesso");
        record.setSourceClassName("br.edu.poo2.lembretezap.command.CadastrarCompromissoCommand");
        record.setSourceMethodName("execute");
        long millis = 1_700_000_000_000L;
        record.setMillis(millis);
        record.setThreadID(42);

        String formatted = formatter.format(record);

        String expected = "42::FINE::br.edu.poo2.lembretezap.command.CadastrarCompromissoCommand::execute::"
                + new Date(millis) + "::Compromisso cadastrado com sucesso\n";
        assertEquals(expected, formatted);
        assertTrue(formatted.endsWith("\n"));
        assertEquals(6, formatted.substring(0, formatted.length() - 1).split("::", -1).length);
    }
}
