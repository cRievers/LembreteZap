package br.edu.poo2.lembretezap.service;

import br.edu.poo2.lembretezap.logging.LogConfig;
import br.edu.poo2.lembretezap.model.Compromisso;
import br.edu.poo2.lembretezap.model.FiltroCompromisso;
import br.edu.poo2.lembretezap.model.SituacaoCompromisso;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WhatsAppServiceTest {

    @Test
    void deveMontarUrlWaMeComTelefoneLimpoEMensagemCodificadaEmUtf8() {
        WhatsAppService service = new WhatsAppService();
        Compromisso compromisso = new Compromisso(
                1,
                "Entrega do trabalho de POO II (acentuação: ç, ã, é)",
                LocalDate.of(2026, 11, 25),
                LocalTime.of(18, 30),
                "Profa. Daniela Terra",
                "+55 (31) 99999-2222",
                SituacaoCompromisso.PENDENTE);

        String mensagem = service.montarMensagemLembrete(compromisso);
        String url = service.montarUrl(compromisso);

        assertTrue(mensagem.contains("Profa. Daniela Terra"));
        assertTrue(mensagem.contains("Entrega do trabalho de POO II (acentuação: ç, ã, é)"));
        assertTrue(mensagem.contains("25/11/2026"));
        assertTrue(mensagem.contains("18:30"));

        String expectedPrefix = "https://wa.me/5531999992222?text=";
        assertTrue(url.startsWith(expectedPrefix));

        String encodedQuery = url.substring(expectedPrefix.length());
        assertEquals(URLEncoder.encode(mensagem, StandardCharsets.UTF_8), encodedQuery);
        assertEquals(mensagem, URLDecoder.decode(encodedQuery, StandardCharsets.UTF_8));
    }

    @Test
    void deveRejeitarTelefoneInvalidoOuAusenteAoMontarUrl() {
        WhatsAppService service = new WhatsAppService();
        Compromisso semDigitos = new Compromisso(
                "Reunião",
                LocalDate.of(2026, 12, 1),
                LocalTime.of(10, 0),
                "Carlos",
                "---",
                SituacaoCompromisso.PENDENTE);

        assertThrows(IllegalArgumentException.class, () -> service.montarUrl(semDigitos));
        assertThrows(IllegalArgumentException.class, () -> service.montarUrl(null));
    }

    @Test
    void deveAbrirConversaComUriValidaERegistrarExcecaoDeES() {
        LogConfig.init();
        AtomicReference<URI> openedUri = new AtomicReference<>();
        WhatsAppService service = new WhatsAppService(openedUri::set);

        Compromisso compromisso = new Compromisso(
                "Consulta médica",
                LocalDate.of(2026, 11, 30),
                LocalTime.of(9, 15),
                "Dra. Mariana",
                "5531999991111",
                SituacaoCompromisso.PENDENTE);

        service.abrirConversa(compromisso);
        assertEquals(service.montarUrl(compromisso), openedUri.get().toString());

        WhatsAppService failingService = new WhatsAppService(uri -> {
            throw new IOException("Falha simulada");
        });
        assertThrows(UncheckedIOException.class, () -> failingService.abrirConversa(compromisso));
    }

    @Test
    void deveIdentificarCompromissoAtrasadoCorretamente() {
        Compromisso pendenteAtrasado = new Compromisso(
                "Pagar boleto",
                LocalDate.now().minusDays(1),
                LocalTime.of(10, 0),
                "Banco",
                "5531999990000",
                SituacaoCompromisso.PENDENTE);
        assertTrue(pendenteAtrasado.isAtrasado());

        Compromisso pendenteFuturo = new Compromisso(
                "Prova final",
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                "Universidade",
                "5531999990001",
                SituacaoCompromisso.PENDENTE);
        assertFalse(pendenteFuturo.isAtrasado());

        Compromisso concluidoPassado = new Compromisso(
                "Enviar relatório",
                LocalDate.now().minusDays(2),
                LocalTime.of(8, 0),
                "Gestor",
                "5531999990002",
                SituacaoCompromisso.CONCLUIDO);
        assertFalse(concluidoPassado.isAtrasado());

        Compromisso incompleto = new Compromisso();
        assertFalse(incompleto.isAtrasado());
    }

    @Test
    void deveNormalizarFiltroESituacaoCompromisso() {
        assertEquals("Pendente", SituacaoCompromisso.PENDENTE.toString());
        assertEquals("Concluído", SituacaoCompromisso.CONCLUIDO.getRotulo());

        FiltroCompromisso padrao = new FiltroCompromisso(null, null, "   ");
        assertEquals(FiltroCompromisso.TipoFiltro.TODOS, padrao.tipo());
        assertNull(padrao.termoBusca());
        assertFalse(padrao.hasData());
        assertFalse(padrao.hasTermoBusca());

        FiltroCompromisso filtrado = new FiltroCompromisso(
                FiltroCompromisso.TipoFiltro.ATRASADOS,
                LocalDate.of(2026, 11, 24),
                "  Daniela  ");
        assertEquals(FiltroCompromisso.TipoFiltro.ATRASADOS, filtrado.tipo());
        assertEquals("Daniela", filtrado.termoBusca());
        assertTrue(filtrado.hasData());
        assertTrue(filtrado.hasTermoBusca());
    }
}
