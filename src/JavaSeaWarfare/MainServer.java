package JavaSeaWarfare;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class MainServer {
    public static void main(String[] args) {
        String[] optionStart = {"Hospedar Jogo (Servidor)", "Conectar a um Jogo (Cliente)", "Ver Histórico"};   //Opçoes quuando inicia 
        
        int choice = JOptionPane.showOptionDialog(null, "Selecione o modo de jogo:", 
                "Java Sea Warfare Launcher", JOptionPane.DEFAULT_OPTION, 
                JOptionPane.INFORMATION_MESSAGE, null, optionStart, optionStart[0]);

        if (choice == 0) {
            SwingUtilities.invokeLater(() -> new Screen("Jogador Servidor", true, "localhost").setVisible(true));
        } else if (choice == 1) {
            String ip = JOptionPane.showInputDialog("Digite o IP do servidor:", "localhost");
            if (ip != null && !ip.trim().isEmpty()) {
                SwingUtilities.invokeLater(() -> new Screen("Jogador Cliente", false, ip).setVisible(true));
            }
        } else if (choice == 2) {
            // Requisito de Leitura de Arquivo Texto ======
            String history = Logs.lerHistorico();
            JOptionPane.showMessageDialog(null, history, "Histórico de Partidas", JOptionPane.INFORMATION_MESSAGE);
            main(args); // reabre o menu apos fechar o histórico
        }
    }
}

//Necessario a organização dos arquivos devido a quantidade de classes 
//Rever nomes de variaveis e metodods em ptbr