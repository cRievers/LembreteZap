package br.edu.poo2.lembretezap.command;

/**
 * Contrato do padrão comportamental GoF Command para encapsular operações da aplicação.
 */
@FunctionalInterface
public interface Command {

    /**
     * Executa a operação encapsulada pelo comando.
     *
     * @throws Exception caso ocorra erro de validação, persistência ou integração
     */
    void execute() throws Exception;
}
