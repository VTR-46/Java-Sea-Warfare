package JavaSeaWarfare;

import JavaSeaWarfare.GUI.Screen;
import JavaSeaWarfare.Logs.Logs;
import javax.swing.JOptionPane;
import JavaSeaWarfare.GUI.Menu;
import javax.swing.SwingUtilities;

public class MainServer {
    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(() -> {
            Menu menuPrincipal = new Menu();
            menuPrincipal.setVisible(true);
        });
    }
}
