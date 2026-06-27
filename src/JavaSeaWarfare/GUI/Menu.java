package JavaSeaWarfare.GUI;

import JavaSeaWarfare.Logs.Logs;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;

public class Menu extends JFrame {

    private Image imgBackground;

    public Menu() {
        
        setTitle("Java Sea Warfare - Menu Principal");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centralizar na tela

        // carregar a imagem de fundo
        try {

            java.net.URL imgURL = getClass().getResource("img/fundoMENU.png");
            
            if (imgURL != null) {

                imgBackground = ImageIO.read(imgURL); 
            }
            
        } catch (Exception e) {
            System.err.println("Imagem de fundo não encontrada. O menu ficará com cor sólida.");
        }

        // painel customizado que desenha a imagem no fundo
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (imgBackground != null) {
                    // Desenha a imagem preenchendo todo o painel
                    g.drawImage(imgBackground, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // cor de fundo caso a imagem falhe
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        // 
        mainPanel.setLayout(new GridBagLayout());

        // Painel transparente para agrupar os botões e inputs
        JPanel panelMenu = new JPanel();
        panelMenu.setLayout(new GridLayout(6, 1, 10, 15)); 
        panelMenu.setOpaque(false); 
        panelMenu.setPreferredSize(new Dimension(300, 300));

        // --- COMPONENTES DO MENU ---
        
        JLabel lblTitulo = new JLabel("JAVA SEA WARFARE", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Impact", Font.PLAIN, 32));
        lblTitulo.setForeground(Color.WHITE); // Texto branco para contrastar

        JLabel lblNome = new JLabel("Digite seu nome, Almirante:", SwingConstants.CENTER);
        lblNome.setForeground(Color.WHITE);
        lblNome.setFont(new Font("Arial", Font.BOLD, 14));

        JTextField txtNome = new JTextField("Almirante");
        txtNome.setHorizontalAlignment(JTextField.CENTER);
        txtNome.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton btnHospedar = new JButton("Hospedar Jogo (Servidor)");
        JButton btnConectar = new JButton("Conectar a um Jogo (Cliente)");
        JButton btnHistorico = new JButton("Ver Histórico");

        // --- AÇÕES DOS BOTÕES (A mesma lógica que estava no MainServer) ---

        btnHospedar.addActionListener(e -> {
            String nome = txtNome.getText().trim();
            if (nome.isEmpty()) nome = "Almirante";
            
            new Screen(nome, true, "localhost").setVisible(true);
            this.dispose(); // Fecha a tela de menu
        });

        btnConectar.addActionListener(e -> {
            String nome = txtNome.getText().trim();
            if (nome.isEmpty()) nome = "Almirante";
            
            String ip = JOptionPane.showInputDialog(this, "Digite o IP do servidor:", "localhost");
            if (ip != null && !ip.trim().isEmpty()) {
                new Screen(nome, false, ip).setVisible(true);
                this.dispose(); // Fecha a tela de menu
            }
        });

        btnHistorico.addActionListener(e -> {
            String historico = Logs.lerHistorico();
            JOptionPane.showMessageDialog(this, historico, "Histórico de Partidas", JOptionPane.INFORMATION_MESSAGE);
        });

        // os componentes ao painel de menu
        panelMenu.add(lblTitulo);
        panelMenu.add(lblNome);
        panelMenu.add(txtNome);
        panelMenu.add(btnHospedar);
        panelMenu.add(btnConectar);
        panelMenu.add(btnHistorico);

        // Adiciona o menu ao painel principal
        mainPanel.add(panelMenu);

        //  painel principal como o conteúdo da janela
        setContentPane(mainPanel);
    }
}