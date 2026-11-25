package br.edu.poo2.lembretezap.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Entidade de domínio que representa um compromisso pessoal com contato de WhatsApp.
 */
public class Compromisso {

    private Integer codigo;
    private String descricao;
    private LocalDate data;
    private LocalTime horario;
    private String nomeContato;
    private String telefone;
    private SituacaoCompromisso situacao;

    public Compromisso() {
        this.situacao = SituacaoCompromisso.PENDENTE;
    }

    public Compromisso(
            String descricao,
            LocalDate data,
            LocalTime horario,
            String nomeContato,
            String telefone,
            SituacaoCompromisso situacao) {
        this(null, descricao, data, horario, nomeContato, telefone, situacao);
    }

    public Compromisso(
            Integer codigo,
            String descricao,
            LocalDate data,
            LocalTime horario,
            String nomeContato,
            String telefone,
            SituacaoCompromisso situacao) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.data = data;
        this.horario = horario;
        this.nomeContato = nomeContato;
        this.telefone = telefone;
        this.situacao = (situacao != null) ? situacao : SituacaoCompromisso.PENDENTE;
    }

    /**
     * Indica se o compromisso está pendente e com data/horário anterior ao instante atual.
     *
     * @return {@code true} se estiver pendente e atrasado; {@code false} caso contrário
     */
    public boolean isAtrasado() {
        if (situacao != SituacaoCompromisso.PENDENTE || data == null || horario == null) {
            return false;
        }
        return LocalDateTime.of(data, horario).isBefore(LocalDateTime.now());
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public void setHorario(LocalTime horario) {
        this.horario = horario;
    }

    public String getNomeContato() {
        return nomeContato;
    }

    public void setNomeContato(String nomeContato) {
        this.nomeContato = nomeContato;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public SituacaoCompromisso getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoCompromisso situacao) {
        this.situacao = situacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Compromisso other)) {
            return false;
        }
        return Objects.equals(codigo, other.codigo)
                && Objects.equals(descricao, other.descricao)
                && Objects.equals(data, other.data)
                && Objects.equals(horario, other.horario)
                && Objects.equals(nomeContato, other.nomeContato)
                && Objects.equals(telefone, other.telefone)
                && situacao == other.situacao;
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo, descricao, data, horario, nomeContato, telefone, situacao);
    }

    @Override
    public String toString() {
        return "Compromisso{"
                + "codigo=" + codigo
                + ", descricao='" + descricao + '\''
                + ", data=" + data
                + ", horario=" + horario
                + ", nomeContato='" + nomeContato + '\''
                + ", telefone='" + telefone + '\''
                + ", situacao=" + situacao
                + '}';
    }
}
