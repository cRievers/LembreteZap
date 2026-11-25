package br.edu.poo2.lembretezap.model;

import java.time.LocalDate;

/**
 * Critérios de filtragem para consulta de compromissos na camada de persistência e na tabela.
 */
public record FiltroCompromisso(
        TipoFiltro tipo,
        LocalDate data,
        String termoBusca) {

    /**
     * Filtro de exibição por situação ou atraso do compromisso.
     */
    public enum TipoFiltro {
        TODOS("Todos"),
        PENDENTES("Pendentes"),
        CONCLUIDOS("Concluídos"),
        ATRASADOS("Somente Atrasados");

        private final String rotulo;

        TipoFiltro(String rotulo) {
            this.rotulo = rotulo;
        }

        public String getRotulo() {
            return rotulo;
        }

        @Override
        public String toString() {
            return rotulo;
        }
    }

    public FiltroCompromisso {
        tipo = (tipo == null) ? TipoFiltro.TODOS : tipo;
        if (termoBusca != null) {
            termoBusca = termoBusca.trim();
            if (termoBusca.isEmpty()) {
                termoBusca = null;
            }
        }
    }

    public static FiltroCompromisso todos() {
        return new FiltroCompromisso(TipoFiltro.TODOS, null, null);
    }

    public boolean hasData() {
        return data != null;
    }

    public boolean hasTermoBusca() {
        return termoBusca != null && !termoBusca.isBlank();
    }
}
