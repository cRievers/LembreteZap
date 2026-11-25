package br.edu.poo2.lembretezap.view;

import br.edu.poo2.lembretezap.command.CommandInvoker;
import br.edu.poo2.lembretezap.dao.CompromissoDAO;
import br.edu.poo2.lembretezap.model.Compromisso;
import br.edu.poo2.lembretezap.model.FiltroCompromisso;
import br.edu.poo2.lembretezap.model.SituacaoCompromisso;
import br.edu.poo2.lembretezap.service.WhatsAppService;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompromissoViewTest {

    @Test
    void deveExibirColunasEValoresFormatadosNoTableModel() {
        CompromissoTableModel model = new CompromissoTableModel();
        Compromisso c = new Compromisso(
                10,
                "Reunião POO II",
                LocalDate.of(2026, 11, 25),
                LocalTime.of(9, 5),
                "Profa. Daniela",
                "5531999990000",
                SituacaoCompromisso.PENDENTE);

        model.setCompromissos(List.of(c));

        assertEquals(7, model.getColumnCount());
        assertEquals("Código", model.getColumnName(0));
        assertEquals("Descrição", model.getColumnName(1));
        assertEquals("Data", model.getColumnName(2));
        assertEquals("Horário", model.getColumnName(3));
        assertEquals("Contato", model.getColumnName(4));
        assertEquals("WhatsApp", model.getColumnName(5));
        assertEquals("Situação", model.getColumnName(6));

        assertEquals(1, model.getRowCount());
        assertEquals(10, model.getValueAt(0, 0));
        assertEquals("Reunião POO II", model.getValueAt(0, 1));
        assertEquals("25/11/2026", model.getValueAt(0, 2));
        assertEquals("09:05", model.getValueAt(0, 3));
        assertEquals("Profa. Daniela", model.getValueAt(0, 4));
        assertEquals("5531999990000", model.getValueAt(0, 5));
        assertEquals("Pendente", model.getValueAt(0, 6));
        assertFalse(model.isCellEditable(0, 1));
        assertNull(model.getCompromissoAt(99));
    }

    @Test
    void deveDestacarLinhasAtrasadasEConcluidasNoCellRenderer() {
        Compromisso atrasado = new Compromisso(
                1,
                "Atrasado",
                LocalDate.now().minusDays(2),
                LocalTime.of(8, 0),
                "Contato A",
                "5531999991111",
                SituacaoCompromisso.PENDENTE);

        Compromisso concluido = new Compromisso(
                2,
                "Concluído",
                LocalDate.now().minusDays(1),
                LocalTime.of(10, 0),
                "Contato B",
                "5531999992222",
                SituacaoCompromisso.CONCLUIDO);

        Compromisso pendenteFuturo = new Compromisso(
                3,
                "Futuro",
                LocalDate.now().plusDays(3),
                LocalTime.of(16, 30),
                "Contato C",
                "5531999993333",
                SituacaoCompromisso.PENDENTE);

        CompromissoTableModel model = new CompromissoTableModel();
        model.setCompromissos(List.of(atrasado, concluido, pendenteFuturo));

        JTable table = new JTable(model);
        CompromissoCellRenderer renderer = new CompromissoCellRenderer();

        Component compAtrasado = renderer.getTableCellRendererComponent(
                table, "Atrasado", false, false, 0, 1);
        assertEquals(CompromissoCellRenderer.COR_ATRASADO_FUNDO, compAtrasado.getBackground());
        assertEquals(CompromissoCellRenderer.COR_ATRASADO_TEXTO, compAtrasado.getForeground());

        Component compConcluido = renderer.getTableCellRendererComponent(
                table, "Concluído", false, false, 1, 1);
        assertEquals(CompromissoCellRenderer.COR_CONCLUIDO_FUNDO, compConcluido.getBackground());
        assertEquals(CompromissoCellRenderer.COR_CONCLUIDO_TEXTO, compConcluido.getForeground());

        Component compFuturo = renderer.getTableCellRendererComponent(
                table, "Futuro", false, false, 2, 1);
        assertEquals(CompromissoCellRenderer.COR_PADRAO_PAR, compFuturo.getBackground());

        Component compSelecionado = renderer.getTableCellRendererComponent(
                table, "Atrasado", true, false, 0, 1);
        assertEquals(table.getSelectionBackground(), compSelecionado.getBackground());
    }

    @Test
    void deveValidarParsingEstritoDeDataHorarioEMascara() {
        assertEquals(LocalDate.of(2026, 11, 24), TelaPrincipalFrame.parseData("24/11/2026"));
        assertEquals(LocalTime.of(14, 45), TelaPrincipalFrame.parseHorario("14:45"));

        assertThrows(IllegalArgumentException.class, () -> TelaPrincipalFrame.parseData("31/02/2026"));
        assertThrows(IllegalArgumentException.class, () -> TelaPrincipalFrame.parseData("__/__/____"));
        assertThrows(IllegalArgumentException.class, () -> TelaPrincipalFrame.parseHorario("25:00"));
        assertThrows(IllegalArgumentException.class, () -> TelaPrincipalFrame.parseHorario("__:__"));

        assertTrue(TelaPrincipalFrame.isMascaraVazia("__/__/____"));
        assertTrue(TelaPrincipalFrame.isMascaraVazia("  /  /    "));
        assertFalse(TelaPrincipalFrame.isMascaraVazia("24/11/____"));
    }

    @Test
    void deveCarregarTabelaAoConstruirTelaPrincipalQuandoAmbienteGraficoDisponivel() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());

        Compromisso item = new Compromisso(
                7,
                "Teste GUI",
                LocalDate.of(2026, 12, 1),
                LocalTime.of(11, 0),
                "Maria",
                "5531988887777",
                SituacaoCompromisso.PENDENTE);

        CompromissoDAO daoStub = new CompromissoDAO() {
            @Override
            public Compromisso inserir(Compromisso compromisso) {
                return compromisso;
            }

            @Override
            public void atualizar(Compromisso compromisso) {
            }

            @Override
            public void excluir(Integer codigo) {
            }

            @Override
            public Optional<Compromisso> buscarPorCodigo(Integer codigo) {
                return Optional.empty();
            }

            @Override
            public List<Compromisso> listar(FiltroCompromisso filtro) {
                return List.of(item);
            }
        };

        SwingUtilities.invokeAndWait(() -> {
            TelaPrincipalFrame frame = new TelaPrincipalFrame(
                    daoStub, new WhatsAppService(), new CommandInvoker());
            try {
                assertEquals(1, frame.getTableModel().getRowCount());
                assertEquals("Teste GUI", frame.getTableModel().getValueAt(0, 1));
            } finally {
                frame.dispose();
            }
        });
    }
}
