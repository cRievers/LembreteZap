package br.edu.poo2.lembretezap.model;

/**
 * Representa os possíveis estados de um compromisso no sistema.
 */
public enum SituacaoCompromisso {
    PENDENTE("Pendente"),
    CONCLUIDO("Concluído");

    private final String rotulo;

    SituacaoCompromisso(String rotulo) {
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
