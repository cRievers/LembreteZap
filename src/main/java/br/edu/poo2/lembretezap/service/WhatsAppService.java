package br.edu.poo2.lembretezap.service;

import br.edu.poo2.lembretezap.model.Compromisso;

import java.awt.Desktop;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serviço responsável por montar a mensagem de lembrete, gerar a URL {@code wa.me}
 * codificada em UTF-8 e abrir a conversa do WhatsApp no navegador padrão do sistema.
 */
public class WhatsAppService {

    private static final String CLASS_NAME = WhatsAppService.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String WHATSAPP_BASE_URL = "https://wa.me/";

    @FunctionalInterface
    interface BrowserLauncher {
        void browse(URI uri) throws IOException;
    }

    private final BrowserLauncher browserLauncher;

    public WhatsAppService() {
        this(WhatsAppService::abrirNoDesktop);
    }

    WhatsAppService(BrowserLauncher browserLauncher) {
        this.browserLauncher = Objects.requireNonNull(browserLauncher, "BrowserLauncher não pode ser nulo");
    }

    /**
     * Monta o texto amigável do lembrete com contato, descrição, data e horário.
     *
     * @param compromisso compromisso a ser lembrado
     * @return mensagem de lembrete preenchida
     */
    public String montarMensagemLembrete(Compromisso compromisso) {
        LOGGER.logp(Level.FINER, CLASS_NAME, "montarMensagemLembrete", "ENTRY");
        validarCompromissoParaMensagem(compromisso);

        String dataFormatada = compromisso.getData().format(DATE_FORMATTER);
        String horarioFormatado = compromisso.getHorario().format(TIME_FORMATTER);
        String mensagem = String.format(
                "Olá, %s! Lembrete do compromisso: \"%s\" em %s às %s.",
                compromisso.getNomeContato().trim(),
                compromisso.getDescricao().trim(),
                dataFormatada,
                horarioFormatado);

        LOGGER.logp(Level.FINEST, CLASS_NAME, "montarMensagemLembrete",
                () -> "Mensagem de lembrete montada: " + mensagem);
        LOGGER.logp(Level.FINER, CLASS_NAME, "montarMensagemLembrete", "RETURN");
        return mensagem;
    }

    /**
     * Monta a URL {@code https://wa.me/<telefone>?text=<texto>} com telefone somente numérico
     * e mensagem codificada em UTF-8.
     *
     * @param compromisso compromisso de destino
     * @return URL completa pronta para abertura no navegador
     */
    public String montarUrl(Compromisso compromisso) {
        LOGGER.logp(Level.FINER, CLASS_NAME, "montarUrl", "ENTRY");
        if (compromisso == null) {
            throw new IllegalArgumentException("Compromisso não pode ser nulo.");
        }

        String telefoneLimpo = limparTelefone(compromisso.getTelefone());
        LOGGER.logp(Level.FINEST, CLASS_NAME, "montarUrl",
                () -> "Telefone normalizado para WhatsApp: " + telefoneLimpo);

        String mensagem = montarMensagemLembrete(compromisso);
        String textoCodificado = URLEncoder.encode(mensagem, StandardCharsets.UTF_8);
        String url = WHATSAPP_BASE_URL + telefoneLimpo + "?text=" + textoCodificado;

        LOGGER.logp(Level.FINEST, CLASS_NAME, "montarUrl", () -> "URL wa.me gerada: " + url);
        LOGGER.logp(Level.FINER, CLASS_NAME, "montarUrl", "RETURN");
        return url;
    }

    /**
     * Abre a conversa no WhatsApp via navegador padrão utilizando {@link Desktop#browse(URI)}.
     *
     * @param compromisso compromisso cujo lembrete será enviado
     */
    public void abrirConversa(Compromisso compromisso) {
        LOGGER.logp(Level.FINER, CLASS_NAME, "abrirConversa", "ENTRY");
        String url = montarUrl(compromisso);

        try {
            URI uri = new URI(url);
            LOGGER.logp(Level.FINEST, CLASS_NAME, "abrirConversa",
                    () -> "Abrindo navegador para URI: " + uri);
            browserLauncher.browse(uri);
            LOGGER.logp(Level.FINER, CLASS_NAME, "abrirConversa", "RETURN");
        } catch (URISyntaxException e) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, "abrirConversa",
                    "URI inválida gerada para o WhatsApp: " + url, e);
            throw new IllegalStateException("URI inválida gerada para o WhatsApp.", e);
        } catch (IOException e) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, "abrirConversa",
                    "Erro de E/S ao abrir o navegador para o WhatsApp.", e);
            throw new UncheckedIOException("Falha ao abrir o navegador para o WhatsApp.", e);
        }
    }

    private static void abrirNoDesktop(URI uri) throws IOException {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            throw new IllegalStateException("Abertura de navegador via Desktop não é suportada neste ambiente.");
        }
        Desktop.getDesktop().browse(uri);
    }

    private static void validarCompromissoParaMensagem(Compromisso compromisso) {
        if (compromisso == null) {
            throw new IllegalArgumentException("Compromisso não pode ser nulo.");
        }
        if (compromisso.getNomeContato() == null || compromisso.getNomeContato().isBlank()) {
            throw new IllegalArgumentException("Nome do contato é obrigatório para montar o lembrete.");
        }
        if (compromisso.getDescricao() == null || compromisso.getDescricao().isBlank()) {
            throw new IllegalArgumentException("Descrição é obrigatória para montar o lembrete.");
        }
        if (compromisso.getData() == null || compromisso.getHorario() == null) {
            throw new IllegalArgumentException("Data e horário são obrigatórios para montar o lembrete.");
        }
    }

    private static String limparTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            throw new IllegalArgumentException("Telefone do contato é obrigatório para o WhatsApp.");
        }
        String apenasDigitos = telefone.replaceAll("\\D", "");
        if (apenasDigitos.isEmpty()) {
            throw new IllegalArgumentException("Telefone do contato deve conter dígitos numéricos.");
        }
        return apenasDigitos;
    }
}
