package br.edu.poo2.lembretezap.view;

import br.edu.poo2.lembretezap.model.Compromisso;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Modelo de tabela tipado ({@link AbstractTableModel}) para exibição de {@link Compromisso}
 * na {@code JTable} principal.
 */
public class CompromissoTableModel extends AbstractTableModel {

    private static final String[] COLUNAS = {
            "Código",
            "Descrição",
            "Data",
            "Horário",
            "Contato",
            "WhatsApp",
            "Situação"
    };

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final List<Compromisso> compromissos = new ArrayList<>();

    /**
     * Atualiza a lista de compromissos exibidos na tabela e notifica os listeners.
     *
     * @param novosCompromissos nova coleção de compromissos (ou {@code null} para limpar)
     */
    public void setCompromissos(List<Compromisso> novosCompromissos) {
        compromissos.clear();
        if (novosCompromissos != null) {
            compromissos.addAll(novosCompromissos);
        }
        fireTableDataChanged();
    }

    /**
     * Retorna o {@link Compromisso} correspondente ao índice de linha no modelo.
     *
     * @param rowIndex índice da linha no modelo
     * @return compromisso encontrado ou {@code null} caso o índice seja inválido
     */
    public Compromisso getCompromissoAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= compromissos.size()) {
            return null;
        }
        return compromissos.get(rowIndex);
    }

    /**
     * Retorna uma visão somente leitura dos compromissos atualmente carregados.
     *
     * @return lista imutável de compromissos
     */
    public List<Compromisso> getCompromissos() {
        return Collections.unmodifiableList(compromissos);
    }

    @Override
    public int getRowCount() {
        return compromissos.size();
    }

    @Override
    public int getColumnCount() {
        return COLUNAS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUNAS[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? Integer.class : String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Compromisso c = compromissos.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> c.getCodigo();
            case 1 -> c.getDescricao();
            case 2 -> c.getData() != null ? c.getData().format(DATE_FORMATTER) : "";
            case 3 -> c.getHorario() != null ? c.getHorario().format(TIME_FORMATTER) : "";
            case 4 -> c.getNomeContato();
            case 5 -> c.getTelefone();
            case 6 -> c.getSituacao() != null ? c.getSituacao().getRotulo() : "";
            default -> "";
        };
    }
}
