package JavaSeaWarfare;

import JavaSeaWarfare.GUI.Screen;
import JavaSeaWarfare.Logs.Logs;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class MainServer {
    public static void main(String[] args) {
        
        // Pede o nome do jogador local
        String playerName = JOptionPane.showInputDialog(null, "Almirante, digite seu nome:", "Identificação", JOptionPane.QUESTION_MESSAGE);
        // caso nao informe nenhum nome
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Almirante Desconhecido";
        }

        String[] options = {"Hospedar Jogo (Servidor)", "Conectar a um Jogo (Cliente)", "Ver Histórico"};
        int choice = JOptionPane.showOptionDialog(null, "Selecione o modo de jogo:", 
                "Java Sea Warfare Launcher", JOptionPane.DEFAULT_OPTION, 
                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            final String nome = playerName;
            SwingUtilities.invokeLater(() -> {
                new Screen(nome, true, "localhost").setVisible(true);
            });
        } else if (choice == 1) {
            final String name = playerName;
            String ip = JOptionPane.showInputDialog("Digite o IP do servidor:", "localhost");
            if (ip != null && !ip.trim().isEmpty()) {
                SwingUtilities.invokeLater(() -> {
                    new Screen(name, false, ip).setVisible(true);
                });
            }
        } else if (choice == 2) {
            String historico = Logs.lerHistorico();
            JOptionPane.showMessageDialog(null, historico, "Histórico de Partidas", JOptionPane.INFORMATION_MESSAGE);
            main(args); 
        }
    }
}

//Necessario a organização dos arquivos devido a quantidade de classes 
//Rever nomes de variaveis e metodods em ptbr