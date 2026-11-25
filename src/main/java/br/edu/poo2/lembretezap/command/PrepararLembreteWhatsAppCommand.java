package br.edu.poo2.lembretezap.command;

import br.edu.poo2.lembretezap.model.Compromisso;
import br.edu.poo2.lembretezap.service.WhatsAppService;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Comando responsável por preparar a prévia do lembrete de WhatsApp, solicitar a confirmação
 * do usuário e abrir a conversa via {@link WhatsAppService} somente quando confirmada.
 */
public class PrepararLembreteWhatsAppCommand implements Command {

    private static final String CLASS_NAME = PrepararLembreteWhatsAppCommand.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private final WhatsAppService whatsAppService;
    private final Compromisso compromisso;
    private final Predicate<String> confirmacaoUsuario;

    public PrepararLembreteWhatsAppCommand(
            WhatsAppService whatsAppService,
            Compromisso compromisso,
            Predicate<String> confirmacaoUsuario) {
        this.whatsAppService = Objects.requireNonNull(
                whatsAppService, "WhatsAppService não pode ser nulo.");
        this.compromisso = compromisso;
        this.confirmacaoUsuario = Objects.requireNonNull(
                confirmacaoUsuario, "Predicado de confirmação não pode ser nulo.");
    }

    @Override
    public void execute() {
        final String method = "execute";
        LOGGER.logp(Level.FINER, CLASS_NAME, method, "ENTRY");

        String mensagemPrevia = whatsAppService.montarMensagemLembrete(compromisso);
        LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                () -> "Prévia da mensagem preparada para confirmação: " + mensagemPrevia);

        boolean confirmado = confirmacaoUsuario.test(mensagemPrevia);
        if (confirmado) {
            LOGGER.logp(Level.FINE, CLASS_NAME, method,
                    () -> "Envio de lembrete WhatsApp confirmado pelo usuário para: "
                            + compromisso.getNomeContato());
            whatsAppService.abrirConversa(compromisso);
        } else {
            LOGGER.logp(Level.FINE, CLASS_NAME, method,
                    () -> "Envio de lembrete WhatsApp cancelado pelo usuário para: "
                            + compromisso.getNomeContato());
        }

        LOGGER.logp(Level.FINER, CLASS_NAME, method, "RETURN");
    }
}
