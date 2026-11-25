package br.edu.poo2.lembretezap.command;

import br.edu.poo2.lembretezap.dao.CompromissoDAO;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comando responsável por excluir um compromisso identificado pelo seu código.
 */
public class ExcluirCompromissoCommand implements Command {

    private static final String CLASS_NAME = ExcluirCompromissoCommand.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private final CompromissoDAO dao;
    private final Integer codigo;

    public ExcluirCompromissoCommand(CompromissoDAO dao, Integer codigo) {
        this.dao = Objects.requireNonNull(dao, "CompromissoDAO não pode ser nulo.");
        this.codigo = codigo;
    }

    @Override
    public void execute() {
        final String method = "execute";
        LOGGER.logp(Level.FINER, CLASS_NAME, method, "ENTRY");
        CadastrarCompromissoCommand.validarCodigo(codigo);

        dao.excluir(codigo);
        LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                () -> "Compromisso excluído código=" + codigo);
        LOGGER.logp(Level.FINER, CLASS_NAME, method, "RETURN");
    }
}
