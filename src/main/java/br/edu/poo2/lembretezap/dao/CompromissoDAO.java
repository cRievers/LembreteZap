package br.edu.poo2.lembretezap.dao;

import br.edu.poo2.lembretezap.model.Compromisso;
import br.edu.poo2.lembretezap.model.FiltroCompromisso;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência (DAO) para operações CRUD e consultas filtradas de {@link Compromisso}.
 */
public interface CompromissoDAO {

    /**
     * Insere um novo compromisso no banco e preenche seu {@code codigo} gerado.
     *
     * @param compromisso entidade a persistir
     * @return compromisso persistido com código gerado
     */
    Compromisso inserir(Compromisso compromisso);

    /**
     * Atualiza os dados de um compromisso existente identificado pelo seu {@code codigo}.
     *
     * @param compromisso entidade com os novos valores
     */
    void atualizar(Compromisso compromisso);

    /**
     * Exclui permanentemente um compromisso pelo seu identificador.
     *
     * @param codigo identificador único do compromisso
     */
    void excluir(Integer codigo);

    /**
     * Busca um compromisso pelo seu código identificador.
     *
     * @param codigo identificador único do compromisso
     * @return {@link Optional} contendo o compromisso encontrado, ou vazio se inexistente
     */
    Optional<Compromisso> buscarPorCodigo(Integer codigo);

    /**
     * Lista compromissos aplicando os critérios de situação, data e busca textual definidos no filtro.
     *
     * @param filtro critérios de filtragem (ou {@code null} para listar todos)
     * @return lista ordenada de compromissos correspondentes
     */
    List<Compromisso> listar(FiltroCompromisso filtro);
}
