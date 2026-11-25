package br.edu.poo2.lembretezap.command;

import br.edu.poo2.lembretezap.dao.CompromissoDAO;
import br.edu.poo2.lembretezap.model.Compromisso;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comando responsável por validar os dados obrigatórios e cadastrar um novo {@link Compromisso}.
 */
public class CadastrarCompromissoCommand implements Command {

    private static final String CLASS_NAME = CadastrarCompromissoCommand.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private final CompromissoDAO dao;
    private final Compromisso compromisso;

    public CadastrarCompromissoCommand(CompromissoDAO dao, Compromisso compromisso) {
        this.dao = Objects.requireNonNull(dao, "CompromissoDAO não pode ser nulo.");
        this.compromisso = compromisso;
    }

    @Override
    public void execute() {
        final String method = "execute";
        LOGGER.logp(Level.FINER, CLASS_NAME, method, "ENTRY");
        validarCompromisso(compromisso, false);

        Compromisso salvo = dao.inserir(compromisso);
        LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                () -> "Compromisso cadastrado com código: " + salvo.getCodigo());
        LOGGER.logp(Level.FINER, CLASS_NAME, method, "RETURN");
    }

    static void validarCompromisso(Compromisso compromisso, boolean exigirCodigo) {
        if (compromisso == null) {
            throw new IllegalArgumentException("Compromisso não pode ser nulo.");
        }
        if (exigirCodigo) {
            validarCodigo(compromisso.getCodigo());
        }
        if (isBlank(compromisso.getDescricao())) {
            throw new IllegalArgumentException("Descrição do compromisso é obrigatória.");
        }
        if (compromisso.getData() == null) {
            throw new IllegalArgumentException("Data do compromisso é obrigatória.");
        }
        if (compromisso.getHorario() == null) {
            throw new IllegalArgumentException("Horário do compromisso é obrigatório.");
        }
        if (isBlank(compromisso.getNomeContato())) {
            throw new IllegalArgumentException("Nome do contato é obrigatório.");
        }
        if (isBlank(compromisso.getTelefone())) {
            throw new IllegalArgumentException("Telefone do contato é obrigatório.");
        }
    }

    static void validarCodigo(Integer codigo) {
        if (codigo == null || codigo <= 0) {
            throw new IllegalArgumentException("Código do compromisso deve ser um inteiro positivo.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
