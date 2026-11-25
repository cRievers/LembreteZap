package br.edu.poo2.lembretezap.view;

import br.edu.poo2.lembretezap.model.Compromisso;
import br.edu.poo2.lembretezap.model.SituacaoCompromisso;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;

/**
 * Renderizador customizado para a {@link JTable} de compromissos.
 * Destaca visualmente compromissos atrasados em vermelho suave e concluídos em verde suave.
 */
public class CompromissoCellRenderer extends DefaultTableCellRenderer {

    static final Color COR_ATRASADO_FUNDO = new Color(255, 224, 224);
    static final Color COR_ATRASADO_TEXTO = new Color(145, 20, 20);
    static final Color COR_CONCLUIDO_FUNDO = new Color(224, 247, 228);
    static final Color COR_CONCLUIDO_TEXTO = new Color(20, 100, 40);
    static final Color COR_PADRAO_PAR = Color.WHITE;
    static final Color COR_PADRAO_IMPAR = new Color(248, 250, 252);

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

        Component component = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        setHorizontalAlignment(resolverAlinhamento(column));

        if (isSelected) {
            component.setBackground(table.getSelectionBackground());
            component.setForeground(table.getSelectionForeground());
            return component;
        }

        Compromisso compromisso = obterCompromissoDaLinha(table, row);
        if (compromisso != null && compromisso.isAtrasado()) {
            component.setBackground(COR_ATRASADO_FUNDO);
            component.setForeground(COR_ATRASADO_TEXTO);
        } else if (compromisso != null && compromisso.getSituacao() == SituacaoCompromisso.CONCLUIDO) {
            component.setBackground(COR_CONCLUIDO_FUNDO);
            component.setForeground(COR_CONCLUIDO_TEXTO);
        } else {
            component.setBackground(row % 2 == 0 ? COR_PADRAO_PAR : COR_PADRAO_IMPAR);
            component.setForeground(table.getForeground());
        }

        return component;
    }

    private static int resolverAlinhamento(int column) {
        return switch (column) {
            case 0, 2, 3, 6 -> SwingConstants.CENTER;
            default -> SwingConstants.LEFT;
        };
    }

    private static Compromisso obterCompromissoDaLinha(JTable table, int viewRow) {
        if (!(table.getModel() instanceof CompromissoTableModel tableModel)) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return tableModel.getCompromissoAt(modelRow);
    }
}
