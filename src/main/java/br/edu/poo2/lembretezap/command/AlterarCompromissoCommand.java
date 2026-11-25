package br.edu.poo2.lembretezap.command;

import br.edu.poo2.lembretezap.dao.CompromissoDAO;
import br.edu.poo2.lembretezap.model.Compromisso;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comando responsável por validar e atualizar um {@link Compromisso} existente (incluindo
 * mudança de situação para concluído).
 */
public class AlterarCompromissoCommand implements Command {

    private static final String CLASS_NAME = AlterarCompromissoCommand.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private final CompromissoDAO dao;
    private final Compromisso compromisso;

    public AlterarCompromissoCommand(CompromissoDAO dao, Compromisso compromisso) {
        this.dao = Objects.requireNonNull(dao, "CompromissoDAO não pode ser nulo.");
        this.compromisso = compromisso;
    }

    @Override
    public void execute() {
        final String method = "execute";
        LOGGER.logp(Level.FINER, CLASS_NAME, method, "ENTRY");
        CadastrarCompromissoCommand.validarCompromisso(compromisso, true);

        dao.atualizar(compromisso);
        LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                () -> "Compromisso alterado código=" + compromisso.getCodigo()
                        + ", situacao=" + compromisso.getSituacao());
        LOGGER.logp(Level.FINER, CLASS_NAME, method, "RETURN");
    }
}
