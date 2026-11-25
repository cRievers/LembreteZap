package br.edu.poo2.lembretezap;

import br.edu.poo2.lembretezap.logging.LogConfig;
import br.edu.poo2.lembretezap.view.TelaPrincipalFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ponto de entrada da aplicação LembreteZap.
 * Inicializa o subsistema de logging, aplica o Look and Feel nativo do sistema
 * e exibe a janela principal na Event Dispatch Thread (EDT).
 */
public final class Main {

    private static final String CLASS_NAME = Main.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private Main() {
    }

    public static void main(String[] args) {
        LogConfig.init();
        LOGGER.logp(Level.FINE, CLASS_NAME, "main", "Iniciando aplicação LembreteZap.");

        aplicarLookAndFeelSistema();

        SwingUtilities.invokeLater(() -> {
            LOGGER.logp(Level.FINEST, CLASS_NAME, "main",
                    "Construindo TelaPrincipalFrame na Event Dispatch Thread.");
            TelaPrincipalFrame frame = new TelaPrincipalFrame();
            frame.setVisible(true);
        });
    }

    private static void aplicarLookAndFeelSistema() {
        try {
            String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(lookAndFeel);
            LOGGER.logp(Level.FINEST, CLASS_NAME, "aplicarLookAndFeelSistema",
                    () -> "Look and Feel aplicado: " + lookAndFeel);
        } catch (ClassNotFoundException
                 | InstantiationException
                 | IllegalAccessException
                 | UnsupportedLookAndFeelException e) {
            LOGGER.logp(Level.WARNING, CLASS_NAME, "aplicarLookAndFeelSistema",
                    "Não foi possível aplicar o Look and Feel do sistema; usando padrão.", e);
        }
    }
}
