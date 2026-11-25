package br.edu.poo2.lembretezap.command;

import br.edu.poo2.lembretezap.dao.CompromissoDAO;
import br.edu.poo2.lembretezap.logging.LogConfig;
import br.edu.poo2.lembretezap.model.Compromisso;
import br.edu.poo2.lembretezap.model.FiltroCompromisso;
import br.edu.poo2.lembretezap.model.SituacaoCompromisso;
import br.edu.poo2.lembretezap.service.WhatsAppService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandTest {

    private InMemoryCompromissoDAO fakeDao;
    private FakeWhatsAppService fakeWhatsAppService;
    private CommandInvoker invoker;
    private CapturingHandler logHandler;

    @BeforeAll
    static void initLogging() {
        LogConfig.init();
    }

    @BeforeEach
    void setUp() {
        fakeDao = new InMemoryCompromissoDAO();
        fakeWhatsAppService = new FakeWhatsAppService();
        invoker = new CommandInvoker();
        logHandler = new CapturingHandler();
        Logger.getLogger(LogConfig.ROOT_LOGGER_NAME).addHandler(logHandler);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        Logger.getLogger(LogConfig.ROOT_LOGGER_NAME).removeHandler(logHandler);
    }

    @Test
    void deveExecutarCadastrarCompromissoCommandViaInvoker() {
        Compromisso novo = novoCompromisso(null, "Reunião de projeto", SituacaoCompromisso.PENDENTE);

        invoker.executar(new CadastrarCompromissoCommand(fakeDao, novo));

        assertNotNull(novo.getCodigo());
        assertEquals(1, fakeDao.store.size());
        assertEquals("Reunião de projeto", fakeDao.buscarPorCodigo(novo.getCodigo()).orElseThrow().getDescricao());
        assertTrue(hasLog(Level.FINE, "Iniciando execução do comando: CadastrarCompromissoCommand"));
        assertTrue(hasLog(Level.FINE, "Comando concluído com sucesso: CadastrarCompromissoCommand"));
    }

    @Test
    void deveExecutarAlterarCompromissoCommandViaInvoker() {
        Compromisso existente = fakeDao.inserir(
                novoCompromisso(null, "Dentista", SituacaoCompromisso.PENDENTE));

        Compromisso alterado = new Compromisso(
                existente.getCodigo(),
                "Dentista - retorno",
                LocalDate.of(2026, 12, 2),
                LocalTime.of(14, 30),
                "Dr. João",
                "5531999993333",
                SituacaoCompromisso.CONCLUIDO);

        invoker.executar(new AlterarCompromissoCommand(fakeDao, alterado));

        Compromisso salvo = fakeDao.buscarPorCodigo(existente.getCodigo()).orElseThrow();
        assertEquals("Dentista - retorno", salvo.getDescricao());
        assertEquals(SituacaoCompromisso.CONCLUIDO, salvo.getSituacao());
        assertTrue(hasLog(Level.FINE, "Comando concluído com sucesso: AlterarCompromissoCommand"));
    }

    @Test
    void deveExecutarExcluirCompromissoCommandViaInvoker() {
        Compromisso existente = fakeDao.inserir(
                novoCompromisso(null, "Pagar fatura", SituacaoCompromisso.PENDENTE));

        invoker.executar(new ExcluirCompromissoCommand(fakeDao, existente.getCodigo()));

        assertTrue(fakeDao.buscarPorCodigo(existente.getCodigo()).isEmpty());
        assertTrue(hasLog(Level.FINE, "Comando concluído com sucesso: ExcluirCompromissoCommand"));
    }

    @Test
    void deveAbrirConversaWhatsAppQuandoUsuarioConfirmar() {
        Compromisso compromisso = novoCompromisso(5, "Entrega POO II", SituacaoCompromisso.PENDENTE);
        AtomicReference<String> previaRecebida = new AtomicReference<>();

        PrepararLembreteWhatsAppCommand cmd = new PrepararLembreteWhatsAppCommand(
                fakeWhatsAppService,
                compromisso,
                previa -> {
                    previaRecebida.set(previa);
                    return true;
                });

        invoker.executar(cmd);

        assertNotNull(previaRecebida.get());
        assertTrue(previaRecebida.get().contains("Entrega POO II"));
        assertTrue(previaRecebida.get().contains("Profa. Daniela"));
        assertEquals(compromisso, fakeWhatsAppService.ultimoCompromissoAberto);
        assertEquals(1, fakeWhatsAppService.aberturas);
        assertTrue(hasLog(Level.FINE, "Envio de lembrete WhatsApp confirmado pelo usuário"));
    }

    @Test
    void naoDeveAbrirConversaWhatsAppQuandoUsuarioCancelar() {
        Compromisso compromisso = novoCompromisso(6, "Apresentação final", SituacaoCompromisso.PENDENTE);
        AtomicReference<String> previaRecebida = new AtomicReference<>();

        PrepararLembreteWhatsAppCommand cmd = new PrepararLembreteWhatsAppCommand(
                fakeWhatsAppService,
                compromisso,
                previa -> {
                    previaRecebida.set(previa);
                    return false;
                });

        invoker.executar(cmd);

        assertNotNull(previaRecebida.get());
        assertNull(fakeWhatsAppService.ultimoCompromissoAberto);
        assertEquals(0, fakeWhatsAppService.aberturas);
        assertTrue(hasLog(Level.FINE, "Envio de lembrete WhatsApp cancelado pelo usuário"));
    }

    @Test
    void deveValidarDadosERegistrarSevereEmFalhaDeComando() {
        Compromisso invalidoSemDescricao = new Compromisso(
                null,
                "   ",
                LocalDate.of(2026, 11, 30),
                LocalTime.of(10, 0),
                "Contato",
                "5531999990000",
                SituacaoCompromisso.PENDENTE);

        assertThrows(IllegalArgumentException.class,
                () -> invoker.executar(new CadastrarCompromissoCommand(fakeDao, invalidoSemDescricao)));
        assertTrue(hasLog(Level.SEVERE, "Falha ao executar comando: CadastrarCompromissoCommand"));

        Compromisso semCodigo = novoCompromisso(null, "Sem código", SituacaoCompromisso.PENDENTE);
        assertThrows(IllegalArgumentException.class,
                () -> invoker.executar(new AlterarCompromissoCommand(fakeDao, semCodigo)));
        assertTrue(hasLog(Level.SEVERE, "Falha ao executar comando: AlterarCompromissoCommand"));

        assertThrows(IllegalArgumentException.class,
                () -> invoker.executar(new ExcluirCompromissoCommand(fakeDao, 0)));
        assertTrue(hasLog(Level.SEVERE, "Falha ao executar comando: ExcluirCompromissoCommand"));

        Command checkedFailingCommand = () -> {
            throw new Exception("Erro checado simulado");
        };
        IllegalStateException wrapped = assertThrows(IllegalStateException.class,
                () -> invoker.executar(checkedFailingCommand));
        assertNotNull(wrapped.getCause());
        assertFalse(logHandler.records.isEmpty());
    }

    private boolean hasLog(Level level, String messageSnippet) {
        return logHandler.records.stream()
                .anyMatch(r -> level.equals(r.getLevel())
                        && r.getMessage() != null
                        && r.getMessage().contains(messageSnippet));
    }

    private static Compromisso novoCompromisso(
            Integer codigo,
            String descricao,
            SituacaoCompromisso situacao) {
        return new Compromisso(
                codigo,
                descricao,
                LocalDate.of(2026, 11, 28),
                LocalTime.of(18, 0),
                "Profa. Daniela",
                "5531999992222",
                situacao);
    }

    private static final class InMemoryCompromissoDAO implements CompromissoDAO {

        private final Map<Integer, Compromisso> store = new LinkedHashMap<>();
        private final AtomicInteger sequence = new AtomicInteger(1);

        @Override
        public Compromisso inserir(Compromisso compromisso) {
            int codigo = sequence.getAndIncrement();
            compromisso.setCodigo(codigo);
            store.put(codigo, copiar(compromisso));
            return compromisso;
        }

        @Override
        public void atualizar(Compromisso compromisso) {
            if (!store.containsKey(compromisso.getCodigo())) {
                throw new IllegalStateException("Código inexistente: " + compromisso.getCodigo());
            }
            store.put(compromisso.getCodigo(), copiar(compromisso));
        }

        @Override
        public void excluir(Integer codigo) {
            if (store.remove(codigo) == null) {
                throw new IllegalStateException("Código inexistente: " + codigo);
            }
        }

        @Override
        public Optional<Compromisso> buscarPorCodigo(Integer codigo) {
            Compromisso encontrado = store.get(codigo);
            return Optional.ofNullable(encontrado).map(InMemoryCompromissoDAO::copiar);
        }

        @Override
        public List<Compromisso> listar(FiltroCompromisso filtro) {
            return store.values().stream().map(InMemoryCompromissoDAO::copiar).toList();
        }

        private static Compromisso copiar(Compromisso c) {
            return new Compromisso(
                    c.getCodigo(),
                    c.getDescricao(),
                    c.getData(),
                    c.getHorario(),
                    c.getNomeContato(),
                    c.getTelefone(),
                    c.getSituacao());
        }
    }

    private static final class FakeWhatsAppService extends WhatsAppService {

        private Compromisso ultimoCompromissoAberto;
        private int aberturas;

        @Override
        public void abrirConversa(Compromisso compromisso) {
            montarUrl(compromisso);
            this.ultimoCompromissoAberto = compromisso;
            this.aberturas++;
        }
    }

    private static final class CapturingHandler extends Handler {

        private final List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            if (record != null) {
                records.add(record);
            }
        }

        @Override
        public void flush() {
            // Sem buffer em memória
        }

        @Override
        public void close() {
            records.clear();
        }
    }
}
