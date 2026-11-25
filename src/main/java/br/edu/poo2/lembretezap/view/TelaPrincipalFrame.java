package br.edu.poo2.lembretezap.view;

import br.edu.poo2.lembretezap.command.AlterarCompromissoCommand;
import br.edu.poo2.lembretezap.command.CadastrarCompromissoCommand;
import br.edu.poo2.lembretezap.command.CommandInvoker;
import br.edu.poo2.lembretezap.command.ExcluirCompromissoCommand;
import br.edu.poo2.lembretezap.command.PrepararLembreteWhatsAppCommand;
import br.edu.poo2.lembretezap.dao.CompromissoDAO;
import br.edu.poo2.lembretezap.dao.CompromissoJdbcDAO;
import br.edu.poo2.lembretezap.model.Compromisso;
import br.edu.poo2.lembretezap.model.FiltroCompromisso;
import br.edu.poo2.lembretezap.model.SituacaoCompromisso;
import br.edu.poo2.lembretezap.service.WhatsAppService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumnModel;
import javax.swing.text.MaskFormatter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Janela principal da aplicação LembreteZap em Java Swing.
 * Integra o formulário de cadastro/edição, filtros de consulta, {@link JTable}
 * com destaque visual de compromissos atrasados/concluídos e execução das operações
 * via padrão GoF Command ({@link CommandInvoker}).
 */
public class TelaPrincipalFrame extends JFrame {

    private static final String CLASS_NAME = TelaPrincipalFrame.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private final CompromissoDAO dao;
    private final WhatsAppService whatsAppService;
    private final CommandInvoker commandInvoker;

    private final CompromissoTableModel tableModel = new CompromissoTableModel();
    private final JTable tabelaCompromissos = new JTable(tableModel);

    private final JTextField txtCodigo = new JTextField(6);
    private final JTextField txtDescricao = new JTextField(30);
    private final JFormattedTextField txtData = criarCampoComMascara("##/##/####", 9);
    private final JFormattedTextField txtHorario = criarCampoComMascara("##:##", 6);
    private final JTextField txtNomeContato = new JTextField(22);
    private final JTextField txtTelefone = new JTextField(16);
    private final JComboBox<SituacaoCompromisso> cmbSituacao =
            new JComboBox<>(SituacaoCompromisso.values());

    private final JComboBox<FiltroCompromisso.TipoFiltro> cmbFiltroTipo =
            new JComboBox<>(FiltroCompromisso.TipoFiltro.values());
    private final JFormattedTextField txtFiltroData = criarCampoComMascara("##/##/####", 9);
    private final JTextField txtFiltroBusca = new JTextField(18);

    private final JButton btnCadastrar = new JButton("Cadastrar");
    private final JButton btnAlterar = new JButton("Alterar");
    private final JButton btnExcluir = new JButton("Excluir");
    private final JButton btnMarcarConcluido = new JButton("Marcar Concluído");
    private final JButton btnLembreteWhatsApp = new JButton("Lembrete WhatsApp");
    private final JButton btnLimpar = new JButton("Limpar");
    private final JButton btnFiltrar = new JButton("Filtrar");
    private final JButton btnLimparFiltro = new JButton("Limpar Filtro");

    private final JLabel lblStatus = new JLabel("Pronto.");
    private String erroCargaInicial;

    public TelaPrincipalFrame() {
        this(new CompromissoJdbcDAO(), new WhatsAppService(), new CommandInvoker());
    }

    public TelaPrincipalFrame(
            CompromissoDAO dao,
            WhatsAppService whatsAppService,
            CommandInvoker commandInvoker) {
        super("LembreteZap — Gerenciador de Compromissos e Lembretes");
        this.dao = Objects.requireNonNull(dao, "CompromissoDAO não pode ser nulo.");
        this.whatsAppService = Objects.requireNonNull(whatsAppService, "WhatsAppService não pode ser nulo.");
        this.commandInvoker = Objects.requireNonNull(commandInvoker, "CommandInvoker não pode ser nulo.");

        inicializarComponentes();
        registrarEventos();
        carregarCompromissos(false);
    }

    private void inicializarComponentes() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(980, 640));
        setPreferredSize(new Dimension(1080, 700));

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(12, 12, 10, 12));

        root.add(criarPainelFormulario(), BorderLayout.NORTH);
        root.add(criarPainelCentral(), BorderLayout.CENTER);
        root.add(criarPainelStatus(), BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel criarPainelFormulario() {
        JPanel container = new JPanel(new BorderLayout(8, 8));
        container.setBorder(BorderFactory.createTitledBorder("Dados do Compromisso"));

        JPanel grid = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtCodigo.setEditable(false);
        txtCodigo.setFocusable(false);
        txtCodigo.setBackground(new Color(240, 242, 245));
        txtTelefone.setToolTipText("Informe DDI + DDD + número (ex.: 5531999998888)");

        // Linha 0: Código | Descrição | Situação
        adicionarCampoGrid(grid, gbc, 0, 0, "Código:", txtCodigo, 0.0);
        adicionarCampoGrid(grid, gbc, 2, 0, "Descrição:", txtDescricao, 1.0);
        adicionarCampoGrid(grid, gbc, 4, 0, "Situação:", cmbSituacao, 0.0);

        // Linha 1: Data | Horário | Contato | WhatsApp
        JPanel painelDataHora = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        painelDataHora.add(txtData);
        painelDataHora.add(new JLabel("Horário:"));
        painelDataHora.add(txtHorario);

        adicionarCampoGrid(grid, gbc, 0, 1, "Data:", painelDataHora, 0.0);
        adicionarCampoGrid(grid, gbc, 2, 1, "Contato:", txtNomeContato, 0.6);
        adicionarCampoGrid(grid, gbc, 4, 1, "WhatsApp:", txtTelefone, 0.4);

        container.add(grid, BorderLayout.CENTER);
        container.add(criarPainelBotoesFormulario(), BorderLayout.SOUTH);
        return container;
    }

    private JPanel criarPainelBotoesFormulario() {
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        botoes.add(btnLimpar);
        botoes.add(btnCadastrar);
        botoes.add(btnAlterar);
        botoes.add(btnExcluir);
        botoes.add(btnMarcarConcluido);
        botoes.add(btnLembreteWhatsApp);
        return botoes;
    }

    private JPanel criarPainelCentral() {
        JPanel central = new JPanel(new BorderLayout(8, 8));
        central.add(criarPainelFiltros(), BorderLayout.NORTH);
        central.add(criarPainelTabela(), BorderLayout.CENTER);
        return central;
    }

    private JPanel criarPainelFiltros() {
        JPanel filtros = new JPanel();
        filtros.setLayout(new BoxLayout(filtros, BoxLayout.X_AXIS));
        filtros.setBorder(BorderFactory.createTitledBorder("Filtros de Exibição"));

        filtros.add(Box.createHorizontalStrut(6));
        filtros.add(new JLabel("Situação:"));
        filtros.add(Box.createHorizontalStrut(6));
        cmbFiltroTipo.setMaximumSize(new Dimension(170, 28));
        filtros.add(cmbFiltroTipo);

        filtros.add(Box.createHorizontalStrut(12));
        filtros.add(new JLabel("Data:"));
        filtros.add(Box.createHorizontalStrut(6));
        txtFiltroData.setMaximumSize(new Dimension(100, 28));
        filtros.add(txtFiltroData);

        filtros.add(Box.createHorizontalStrut(12));
        filtros.add(new JLabel("Buscar (Descrição/Contato):"));
        filtros.add(Box.createHorizontalStrut(6));
        txtFiltroBusca.setMaximumSize(new Dimension(260, 28));
        filtros.add(txtFiltroBusca);

        filtros.add(Box.createHorizontalStrut(10));
        filtros.add(btnFiltrar);
        filtros.add(Box.createHorizontalStrut(6));
        filtros.add(btnLimparFiltro);
        filtros.add(Box.createHorizontalGlue());

        return filtros;
    }

    private JScrollPane criarPainelTabela() {
        tabelaCompromissos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaCompromissos.setRowHeight(26);
        tabelaCompromissos.getTableHeader().setReorderingAllowed(false);

        CompromissoCellRenderer renderer = new CompromissoCellRenderer();
        tabelaCompromissos.setDefaultRenderer(Object.class, renderer);
        tabelaCompromissos.setDefaultRenderer(Integer.class, renderer);

        configurarLarguraColunas();

        JScrollPane scrollPane = new JScrollPane(tabelaCompromissos);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Compromissos Agendados"));
        return scrollPane;
    }

    private void configurarLarguraColunas() {
        TableColumnModel columnModel = tabelaCompromissos.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(65);
        columnModel.getColumn(0).setMaxWidth(85);
        columnModel.getColumn(1).setPreferredWidth(270);
        columnModel.getColumn(2).setPreferredWidth(95);
        columnModel.getColumn(3).setPreferredWidth(75);
        columnModel.getColumn(4).setPreferredWidth(180);
        columnModel.getColumn(5).setPreferredWidth(130);
        columnModel.getColumn(6).setPreferredWidth(100);
    }

    private JPanel criarPainelStatus() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(215, 220, 226)),
                new EmptyBorder(6, 4, 2, 4)));

        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.PLAIN, 12f));
        JLabel legenda = new JLabel("Legenda: vermelho = atrasado | verde = concluído");
        legenda.setFont(legenda.getFont().deriveFont(Font.ITALIC, 11f));
        legenda.setForeground(new Color(90, 95, 105));

        statusPanel.add(lblStatus, BorderLayout.WEST);
        statusPanel.add(legenda, BorderLayout.EAST);
        return statusPanel;
    }

    private void registrarEventos() {
        btnCadastrar.addActionListener(e -> onCadastrar());
        btnAlterar.addActionListener(e -> onAlterar());
        btnExcluir.addActionListener(e -> onExcluir());
        btnMarcarConcluido.addActionListener(e -> onMarcarConcluido());
        btnLembreteWhatsApp.addActionListener(e -> onEnviarLembreteWhatsApp());
        btnLimpar.addActionListener(e -> onLimparFormulario());

        btnFiltrar.addActionListener(e -> onAplicarFiltro("botão Filtrar"));
        btnLimparFiltro.addActionListener(e -> onLimparFiltros());
        cmbFiltroTipo.addActionListener(e -> onAplicarFiltro("combo Situação"));
        txtFiltroBusca.addActionListener(e -> onAplicarFiltro("Enter em Busca"));
        txtFiltroData.addActionListener(e -> onAplicarFiltro("Enter em Data"));

        tabelaCompromissos.getSelectionModel().addListSelectionListener(this::onLinhaSelecionada);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                LOGGER.logp(Level.FINE, CLASS_NAME, "windowOpened",
                        "Janela principal aberta pelo usuário.");
                if (erroCargaInicial != null) {
                    mostrarErro(
                            "Não foi possível conectar ao banco de dados na inicialização:\n"
                                    + erroCargaInicial
                                    + "\n\nVerifique o MySQL e o arquivo src/main/resources/db.properties.",
                            "Erro de Conexão");
                    erroCargaInicial = null;
                }
            }

            @Override
            public void windowClosing(WindowEvent e) {
                LOGGER.logp(Level.FINE, CLASS_NAME, "windowClosing",
                        "Usuário solicitou o fechamento da janela principal.");
            }

            @Override
            public void windowClosed(WindowEvent e) {
                LOGGER.logp(Level.FINE, CLASS_NAME, "windowClosed",
                        "Janela principal encerrada.");
            }

            @Override
            public void windowActivated(WindowEvent e) {
                LOGGER.logp(Level.FINEST, CLASS_NAME, "windowActivated",
                        "Janela principal recebeu foco.");
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                LOGGER.logp(Level.FINEST, CLASS_NAME, "windowDeactivated",
                        "Janela principal perdeu foco.");
            }
        });
    }

    private void onCadastrar() {
        final String method = "onCadastrar";
        LOGGER.logp(Level.FINE, CLASS_NAME, method, "Clique no botão Cadastrar.");

        Compromisso compromisso = lerCompromissoDoFormulario(false);
        if (compromisso == null) {
            return;
        }

        try {
            commandInvoker.executar(new CadastrarCompromissoCommand(dao, compromisso));
            LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                    () -> "Cadastro finalizado para código=" + compromisso.getCodigo());
            carregarCompromissos(true);
            limparCamposFormulario();
            atualizarStatus("Compromisso cadastrado com sucesso (código " + compromisso.getCodigo() + ").");
        } catch (RuntimeException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, method, "Erro ao cadastrar compromisso.", ex);
            mostrarErro("Erro ao cadastrar compromisso: " + ex.getMessage(), "Erro ao Cadastrar");
        }
    }

    private void onAlterar() {
        final String method = "onAlterar";
        LOGGER.logp(Level.FINE, CLASS_NAME, method, "Clique no botão Alterar.");

        Compromisso compromisso = lerCompromissoDoFormulario(true);
        if (compromisso == null) {
            return;
        }

        try {
            commandInvoker.executar(new AlterarCompromissoCommand(dao, compromisso));
            LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                    () -> "Alteração finalizada para código=" + compromisso.getCodigo());
            carregarCompromissos(true);
            selecionarLinhaPorCodigo(compromisso.getCodigo());
            atualizarStatus("Compromisso código " + compromisso.getCodigo() + " alterado com sucesso.");
        } catch (RuntimeException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, method, "Erro ao alterar compromisso.", ex);
            mostrarErro("Erro ao alterar compromisso: " + ex.getMessage(), "Erro ao Alterar");
        }
    }

    private void onExcluir() {
        final String method = "onExcluir";
        LOGGER.logp(Level.FINE, CLASS_NAME, method, "Clique no botão Excluir.");

        Integer codigo = parseCodigoFormulario();
        if (codigo == null) {
            LOGGER.logp(Level.FINE, CLASS_NAME, method,
                    "Exclusão bloqueada: nenhum compromisso selecionado no formulário.");
            mostrarAviso("Selecione um compromisso na tabela para excluir.", "Nenhum Compromisso Selecionado");
            return;
        }

        int resposta = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente excluir o compromisso código " + codigo + "?",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (resposta != JOptionPane.YES_OPTION) {
            LOGGER.logp(Level.FINE, CLASS_NAME, method,
                    () -> "Exclusão cancelada pelo usuário para código=" + codigo);
            atualizarStatus("Exclusão cancelada.");
            return;
        }

        LOGGER.logp(Level.FINE, CLASS_NAME, method,
                () -> "Exclusão confirmada pelo usuário para código=" + codigo);
        try {
            commandInvoker.executar(new ExcluirCompromissoCommand(dao, codigo));
            carregarCompromissos(true);
            limparCamposFormulario();
            atualizarStatus("Compromisso código " + codigo + " excluído com sucesso.");
        } catch (RuntimeException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, method, "Erro ao excluir compromisso.", ex);
            mostrarErro("Erro ao excluir compromisso: " + ex.getMessage(), "Erro ao Excluir");
        }
    }

    private void onMarcarConcluido() {
        final String method = "onMarcarConcluido";
        LOGGER.logp(Level.FINE, CLASS_NAME, method, "Clique no botão Marcar Concluído.");

        Compromisso compromisso = lerCompromissoDoFormulario(true);
        if (compromisso == null) {
            return;
        }
        if (compromisso.getSituacao() == SituacaoCompromisso.CONCLUIDO) {
            LOGGER.logp(Level.FINE, CLASS_NAME, method,
                    () -> "Compromisso código=" + compromisso.getCodigo() + " já está concluído.");
            mostrarAviso("Este compromisso já está marcado como Concluído.", "Situação Atual");
            return;
        }

        compromisso.setSituacao(SituacaoCompromisso.CONCLUIDO);
        cmbSituacao.setSelectedItem(SituacaoCompromisso.CONCLUIDO);

        try {
            commandInvoker.executar(new AlterarCompromissoCommand(dao, compromisso));
            LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                    () -> "Compromisso marcado como CONCLUIDO código=" + compromisso.getCodigo());
            carregarCompromissos(true);
            selecionarLinhaPorCodigo(compromisso.getCodigo());
            atualizarStatus("Compromisso código " + compromisso.getCodigo() + " marcado como Concluído.");
        } catch (RuntimeException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, method,
                    "Erro ao marcar compromisso como concluído.", ex);
            mostrarErro("Erro ao concluir compromisso: " + ex.getMessage(), "Erro ao Concluir");
        }
    }

    private void onEnviarLembreteWhatsApp() {
        final String method = "onEnviarLembreteWhatsApp";
        LOGGER.logp(Level.FINE, CLASS_NAME, method, "Clique no botão Lembrete WhatsApp.");

        Compromisso compromisso = lerCompromissoDoFormulario(false);
        if (compromisso == null) {
            return;
        }

        try {
            PrepararLembreteWhatsAppCommand command = new PrepararLembreteWhatsAppCommand(
                    whatsAppService,
                    compromisso,
                    previa -> confirmarEnvioWhatsApp(compromisso, previa));

            commandInvoker.executar(command);
        } catch (RuntimeException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, method,
                    "Erro ao preparar ou abrir lembrete no WhatsApp.", ex);
            mostrarErro("Erro ao enviar lembrete no WhatsApp: " + ex.getMessage(), "Erro WhatsApp");
        }
    }

    private boolean confirmarEnvioWhatsApp(Compromisso compromisso, String previaMensagem) {
        final String method = "confirmarEnvioWhatsApp";
        String textoDialogo = String.format(
                "Contato: %s%nWhatsApp: %s%n%nMensagem a enviar:%n\"%s\"%n%nDeseja abrir a conversa no WhatsApp?",
                compromisso.getNomeContato(),
                compromisso.getTelefone(),
                previaMensagem);

        int opcao = JOptionPane.showConfirmDialog(
                this,
                textoDialogo,
                "Confirmar Lembrete WhatsApp",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        boolean confirmado = (opcao == JOptionPane.YES_OPTION);
        LOGGER.logp(Level.FINE, CLASS_NAME, method,
                () -> "Diálogo de confirmação WhatsApp respondeu confirmado=" + confirmado);
        LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                () -> "Prévia apresentada: " + previaMensagem);

        atualizarStatus(confirmado
                ? "Conversa WhatsApp aberta para " + compromisso.getNomeContato() + "."
                : "Envio de lembrete WhatsApp cancelado pelo usuário.");
        return confirmado;
    }

    private void onLimparFormulario() {
        final String method = "onLimparFormulario";
        LOGGER.logp(Level.FINE, CLASS_NAME, method, "Clique no botão Limpar formulário.");
        limparCamposFormulario();
        atualizarStatus("Formulário limpo.");
    }

    private void onAplicarFiltro(String origem) {
        final String method = "onAplicarFiltro";
        LOGGER.logp(Level.FINE, CLASS_NAME, method,
                () -> "Solicitada aplicação de filtro via " + origem + ".");

        FiltroCompromisso filtro = construirFiltroDaTela();
        if (filtro == null) {
            return;
        }

        LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                () -> "Filtro aplicado: tipo=" + filtro.tipo()
                        + ", data=" + filtro.data()
                        + ", busca=" + filtro.termoBusca());
        carregarCompromissos(filtro, true);
    }

    private void onLimparFiltros() {
        final String method = "onLimparFiltros";
        LOGGER.logp(Level.FINE, CLASS_NAME, method, "Clique no botão Limpar Filtro.");

        cmbFiltroTipo.setSelectedItem(FiltroCompromisso.TipoFiltro.TODOS);
        limparCampoMascarado(txtFiltroData);
        txtFiltroBusca.setText("");

        carregarCompromissos(FiltroCompromisso.todos(), true);
        atualizarStatus("Filtros limpos.");
    }

    private void onLinhaSelecionada(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
        }
        final String method = "onLinhaSelecionada";
        int viewRow = tabelaCompromissos.getSelectedRow();
        if (viewRow < 0) {
            LOGGER.logp(Level.FINEST, CLASS_NAME, method, "Seleção da tabela limpa.");
            return;
        }

        int modelRow = tabelaCompromissos.convertRowIndexToModel(viewRow);
        Compromisso selecionado = tableModel.getCompromissoAt(modelRow);
        if (selecionado == null) {
            return;
        }

        LOGGER.logp(Level.FINE, CLASS_NAME, method,
                () -> "Linha selecionada na tabela: codigo=" + selecionado.getCodigo());
        LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                () -> "Compromisso carregado no formulário: " + selecionado);

        preencherFormulario(selecionado);
        atualizarStatus("Compromisso código " + selecionado.getCodigo() + " selecionado.");
    }

    /**
     * Recarrega a tabela utilizando os filtros atualmente preenchidos na tela.
     *
     * @param exibirErroDialog {@code true} para abrir {@link JOptionPane} em falha de consulta
     */
    void carregarCompromissos(boolean exibirErroDialog) {
        FiltroCompromisso filtro = construirFiltroSilent();
        carregarCompromissos(filtro, exibirErroDialog);
    }

    private void carregarCompromissos(FiltroCompromisso filtro, boolean exibirErroDialog) {
        final String method = "carregarCompromissos";
        LOGGER.logp(Level.FINER, CLASS_NAME, method, "ENTRY");

        try {
            List<Compromisso> lista = dao.listar(filtro);
            tableModel.setCompromissos(lista);
            LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                    () -> "Tabela atualizada com " + lista.size() + " compromisso(s).");
            atualizarStatus("Exibindo " + lista.size() + " compromisso(s).");
        } catch (RuntimeException ex) {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, method,
                    "Erro ao carregar compromissos na tabela.", ex);
            atualizarStatus("Erro ao consultar banco de dados.");
            if (exibirErroDialog) {
                mostrarErro("Erro ao consultar compromissos: " + ex.getMessage(), "Erro de Consulta");
            } else {
                this.erroCargaInicial = ex.getMessage();
            }
        }
        LOGGER.logp(Level.FINER, CLASS_NAME, method, "RETURN");
    }

    private FiltroCompromisso construirFiltroDaTela() {
        FiltroCompromisso.TipoFiltro tipo =
                (FiltroCompromisso.TipoFiltro) cmbFiltroTipo.getSelectedItem();
        String dataTexto = txtFiltroData.getText();
        LocalDate dataFiltro = null;

        if (!isMascaraVazia(dataTexto)) {
            try {
                dataFiltro = parseData(dataTexto);
            } catch (IllegalArgumentException ex) {
                LOGGER.logp(Level.FINE, CLASS_NAME, "construirFiltroDaTela",
                        () -> "Data inválida informada no filtro: " + dataTexto);
                mostrarAviso(ex.getMessage(), "Filtro de Data Inválido");
                txtFiltroData.requestFocusInWindow();
                return null;
            }
        }

        return new FiltroCompromisso(tipo, dataFiltro, txtFiltroBusca.getText());
    }

    private FiltroCompromisso construirFiltroSilent() {
        FiltroCompromisso.TipoFiltro tipo =
                (FiltroCompromisso.TipoFiltro) cmbFiltroTipo.getSelectedItem();
        LocalDate dataFiltro = null;
        if (!isMascaraVazia(txtFiltroData.getText())) {
            try {
                dataFiltro = parseData(txtFiltroData.getText());
            } catch (IllegalArgumentException ignored) {
                dataFiltro = null;
            }
        }
        return new FiltroCompromisso(tipo, dataFiltro, txtFiltroBusca.getText());
    }

    private Compromisso lerCompromissoDoFormulario(boolean exigirCodigo) {
        final String method = "lerCompromissoDoFormulario";
        Integer codigo = null;

        if (exigirCodigo) {
            codigo = parseCodigoFormulario();
            if (codigo == null) {
                LOGGER.logp(Level.FINE, CLASS_NAME, method,
                        "Validação falhou: código ausente para alteração/conclusão.");
                mostrarAviso("Selecione um compromisso na tabela antes de alterar.", "Seleção Obrigatória");
                return null;
            }
        }

        String descricao = txtDescricao.getText() != null ? txtDescricao.getText().trim() : "";
        if (descricao.isEmpty()) {
            mostrarAviso("Informe a descrição do compromisso.", "Campo Obrigatório");
            txtDescricao.requestFocusInWindow();
            return null;
        }

        LocalDate data;
        try {
            data = parseData(txtData.getText());
        } catch (IllegalArgumentException ex) {
            LOGGER.logp(Level.FINE, CLASS_NAME, method,
                    () -> "Data inválida no formulário: " + txtData.getText());
            mostrarAviso(ex.getMessage(), "Data Inválida");
            txtData.requestFocusInWindow();
            return null;
        }

        LocalTime horario;
        try {
            horario = parseHorario(txtHorario.getText());
        } catch (IllegalArgumentException ex) {
            LOGGER.logp(Level.FINE, CLASS_NAME, method,
                    () -> "Horário inválido no formulário: " + txtHorario.getText());
            mostrarAviso(ex.getMessage(), "Horário Inválido");
            txtHorario.requestFocusInWindow();
            return null;
        }

        String nomeContato = txtNomeContato.getText() != null ? txtNomeContato.getText().trim() : "";
        if (nomeContato.isEmpty()) {
            mostrarAviso("Informe o nome do contato.", "Campo Obrigatório");
            txtNomeContato.requestFocusInWindow();
            return null;
        }

        String telefone = txtTelefone.getText() != null ? txtTelefone.getText().trim() : "";
        String digitosTelefone = telefone.replaceAll("\\D", "");
        if (digitosTelefone.length() < 8) {
            mostrarAviso(
                    "Informe um telefone WhatsApp válido com DDD/DDI (mínimo 8 dígitos).",
                    "Telefone Inválido");
            txtTelefone.requestFocusInWindow();
            return null;
        }

        SituacaoCompromisso situacao = (SituacaoCompromisso) cmbSituacao.getSelectedItem();
        if (situacao == null) {
            situacao = SituacaoCompromisso.PENDENTE;
        }

        Compromisso compromisso = new Compromisso(
                codigo,
                descricao,
                data,
                horario,
                nomeContato,
                telefone,
                situacao);

        LOGGER.logp(Level.FINEST, CLASS_NAME, method,
                () -> "Dados lidos do formulário: " + compromisso);
        return compromisso;
    }

    static LocalDate parseData(String texto) {
        if (isMascaraVazia(texto)) {
            throw new IllegalArgumentException("Informe a data no formato dd/MM/aaaa.");
        }
        try {
            return LocalDate.parse(texto.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Data inválida (" + texto.trim() + "). Utilize o formato dd/MM/aaaa.", ex);
        }
    }

    static LocalTime parseHorario(String texto) {
        if (isMascaraVazia(texto)) {
            throw new IllegalArgumentException("Informe o horário no formato HH:mm.");
        }
        try {
            return LocalTime.parse(texto.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Horário inválido (" + texto.trim() + "). Utilize o formato HH:mm (00:00 a 23:59).", ex);
        }
    }

    static boolean isMascaraVazia(String texto) {
        if (texto == null) {
            return true;
        }
        String limpo = texto.replace("_", "")
                .replace("/", "")
                .replace(":", "")
                .trim();
        return limpo.isEmpty();
    }

    private Integer parseCodigoFormulario() {
        String texto = txtCodigo.getText();
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            int codigo = Integer.parseInt(texto.trim());
            return codigo > 0 ? codigo : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void preencherFormulario(Compromisso c) {
        txtCodigo.setText(c.getCodigo() != null ? String.valueOf(c.getCodigo()) : "");
        txtDescricao.setText(c.getDescricao() != null ? c.getDescricao() : "");
        txtData.setText(c.getData() != null ? c.getData().format(DATE_FORMATTER) : "");
        txtHorario.setText(c.getHorario() != null ? c.getHorario().format(TIME_FORMATTER) : "");
        txtNomeContato.setText(c.getNomeContato() != null ? c.getNomeContato() : "");
        txtTelefone.setText(c.getTelefone() != null ? c.getTelefone() : "");
        cmbSituacao.setSelectedItem(
                c.getSituacao() != null ? c.getSituacao() : SituacaoCompromisso.PENDENTE);
    }

    private void limparCamposFormulario() {
        txtCodigo.setText("");
        txtDescricao.setText("");
        limparCampoMascarado(txtData);
        limparCampoMascarado(txtHorario);
        txtNomeContato.setText("");
        txtTelefone.setText("");
        cmbSituacao.setSelectedItem(SituacaoCompromisso.PENDENTE);
        tabelaCompromissos.clearSelection();
    }

    private void selecionarLinhaPorCodigo(Integer codigo) {
        if (codigo == null) {
            return;
        }
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Compromisso item = tableModel.getCompromissoAt(i);
            if (item != null && codigo.equals(item.getCodigo())) {
                int viewRow = tabelaCompromissos.convertRowIndexToView(i);
                tabelaCompromissos.setRowSelectionInterval(viewRow, viewRow);
                return;
            }
        }
    }

    private void atualizarStatus(String mensagem) {
        lblStatus.setText(mensagem);
    }

    private void mostrarAviso(String mensagem, String titulo) {
        JOptionPane.showMessageDialog(this, mensagem, titulo, JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarErro(String mensagem, String titulo) {
        JOptionPane.showMessageDialog(this, mensagem, titulo, JOptionPane.ERROR_MESSAGE);
    }

    private static void adicionarCampoGrid(
            JPanel panel,
            GridBagConstraints gbc,
            int gridx,
            int gridy,
            String rotulo,
            java.awt.Component campo,
            double weightx) {
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.weightx = 0.0;
        panel.add(new JLabel(rotulo), gbc);

        gbc.gridx = gridx + 1;
        gbc.weightx = weightx;
        panel.add(campo, gbc);
    }

    private static JFormattedTextField criarCampoComMascara(String mascara, int columns) {
        try {
            MaskFormatter formatter = new MaskFormatter(mascara);
            formatter.setPlaceholderCharacter('_');
            formatter.setValueContainsLiteralCharacters(true);
            JFormattedTextField field = new JFormattedTextField(formatter);
            field.setColumns(columns);
            field.setFocusLostBehavior(JFormattedTextField.PERSIST);
            return field;
        } catch (ParseException e) {
            throw new IllegalStateException("Máscara inválida: " + mascara, e);
        }
    }

    private static void limparCampoMascarado(JFormattedTextField field) {
        field.setValue(null);
        field.setText("");
    }

    CompromissoTableModel getTableModel() {
        return tableModel;
    }

    JTable getTabelaCompromissos() {
        return tabelaCompromissos;
    }
}
