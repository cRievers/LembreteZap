package br.edu.poo2.lembretezap.command;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Invocador centralizado do padrão GoF Command.
 * Registra início, conclusão e eventuais falhas de execução via {@code java.util.logging}.
 */
public class CommandInvoker {

    private static final String CLASS_NAME = CommandInvoker.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * Executa o comando informado delegando para {@link Command#execute()} e registrando
     * logs {@link Level#FINE} no início/fim e {@link Level#SEVERE} em caso de falha.
     *
     * @param command comando a ser executado
     */
    public void executar(Command command) {
        final String method = "executar";
        Objects.requireNonNull(command, "Comando não pode ser nulo.");
        String commandName = command.getClass().getSimpleName();

        LOGGER.logp(Level.FINE, CLASS_NAME, method,
                () -> "Iniciando execução do comando: " + commandName);
        try {
            command.execute();
            LOGGER.logp(Level.FINE, CLASS_NAME, method,
                    () -> "Comando concluído com sucesso: " + commandName);
        } catch (RuntimeException e) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, method,
                    "Falha ao executar comando: " + commandName, e);
            throw e;
        } catch (Exception e) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, method,
                    "Falha ao executar comando: " + commandName, e);
            throw new IllegalStateException("Falha ao executar comando: " + commandName, e);
        }
    }
}
