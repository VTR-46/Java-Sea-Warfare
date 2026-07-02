package JavaSeaWarfare.GUI;

import JavaSeaWarfare.Map.Map;
import JavaSeaWarfare.Logs.Logs;
import JavaSeaWarfare.Network.Net;
import JavaSeaWarfare.Game.Player;
import JavaSeaWarfare.Game.Ship;
import JavaSeaWarfare.Sounds.Sounds;
import JavaSeaWarfare.GUI.Menu;
import javax.swing.*;
import java.awt.*;

public class Screen extends JFrame {

    // Interfaces Visuais
    private JButton[][] opponentButtons;
    private JButton[][] customButtons;
    private JLabel lblStatus;
    private JButton btnGuidance;

    private JLabel lblMy;
    private JLabel lblOpponent;

    private Player localPlayer;
    private String opponentName = "Desconhecido";
    private Net net;
    private Sounds sons;

    // Controle de Turno e Reinício
    private boolean isHost; // Lembra quem é o servidor para reiniciar corretamente
    private boolean MyTurn;
    private boolean wantsToRestart = false;
    private boolean opponentWantsToRestart = false;

    // Controle da Fase de Posicionamento
    private boolean phasePositioning = true;
    private boolean opponentReady = false;
    private boolean horizontal = true;
    private int currentShip = 0;

    private String[] shipNames = {"Porta-Aviões", "Encouraçado", "Contratorpedeiro", "Submarino"};
    private int[] shipSize = {5, 4, 3, 2};

    public Screen(String playerName, boolean host, String ip) {
        this.localPlayer = new Player(playerName);
        this.isHost = host;
        this.MyTurn = host;

        // -----UI-----
        setTitle("Batalha Naval - " + playerName);
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- BARRA DE STATUS SUPERIOR ---
        lblStatus = new JLabel("Conectando...", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblStatus, BorderLayout.NORTH);

        // --- PAINEL CENTRAL ---
        JPanel centralPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centralPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Esquerda (Sua Frota)
        JPanel leftPanel = new JPanel(new BorderLayout());
        lblMy = new JLabel("Sua Frota (" + localPlayer.getName() + ")", SwingConstants.CENTER);
        leftPanel.add(lblMy, BorderLayout.NORTH);

        JPanel ownGrid = new JPanel(new GridLayout(Map.TAMANHO, Map.TAMANHO));
        customButtons = new JButton[Map.TAMANHO][Map.TAMANHO];
        for (int i = 0; i < Map.TAMANHO; i++) {
            for (int j = 0; j < Map.TAMANHO; j++) {
                JButton btn = new JButton("~");
                btn.setBackground(Color.CYAN);
                final int l = i, c = j;
                btn.addActionListener(e -> positionShip(l, c));
                customButtons[i][j] = btn;
                ownGrid.add(btn);
            }
        }
        leftPanel.add(ownGrid, BorderLayout.CENTER);

        btnGuidance = new JButton("Orientação: HORIZONTAL");
        btnGuidance.setBackground(Color.YELLOW);
        btnGuidance.addActionListener(e -> {
            horizontal = !horizontal;
            btnGuidance.setText("Orientação: " + (horizontal ? "HORIZONTAL" : "VERTICAL"));
        });
        leftPanel.add(btnGuidance, BorderLayout.SOUTH);

        // Direita (Radar Inimigo)
        JPanel rightPanel = new JPanel(new BorderLayout());
        lblOpponent = new JLabel("Radar de Ataque (Aguardando...)", SwingConstants.CENTER);
        rightPanel.add(lblOpponent, BorderLayout.NORTH);

        JPanel opponentGrid = new JPanel(new GridLayout(Map.TAMANHO, Map.TAMANHO));
        opponentButtons = new JButton[Map.TAMANHO][Map.TAMANHO];
        for (int i = 0; i < Map.TAMANHO; i++) {
            for (int j = 0; j < Map.TAMANHO; j++) {
                JButton btn = new JButton("~");
                btn.setBackground(Color.CYAN);
                final int l = i, c = j;
                btn.addActionListener(e -> fireOpponent(l, c));
                opponentButtons[i][j] = btn;
                opponentGrid.add(btn);
            }
        }
        rightPanel.add(opponentGrid, BorderLayout.CENTER);

        centralPanel.add(leftPanel);
        centralPanel.add(rightPanel);
        add(centralPanel, BorderLayout.CENTER);

        // Inicializa a conexão e os sons
        this.net = new Net(this, host, ip);
        this.sons = new Sounds();

        sons.playBGM();
    }

    // ==========================================
    // LOGICA DE POSICIONAMENTO MANUAL DE NAVIOS
    // ==========================================
    public void updateStatus(String txt) {
        if (phasePositioning) {
            lblStatus.setText(txt + " | Posicione: " + shipNames[currentShip] + " (" + shipSize[currentShip] + " blocos)");
        } else {
            lblStatus.setText(txt);
        }
    }

    private void positionShip(int row, int col) {
        if (!phasePositioning) {
            return;
        }

        int size = shipSize[currentShip];

        if (canPosition(row, col, size, horizontal)) {
            Ship s = new Ship(shipNames[currentShip], size);

            for (int j = 0; j < size; j++) {
                int l = row + (horizontal ? 0 : j);
                int c = col + (horizontal ? j : 0);

                s.addPosition(l, c);
                customButtons[l][c].setBackground(Color.DARK_GRAY);
                customButtons[l][c].setEnabled(false);
            }

            localPlayer.addShip(s);
            currentShip++;

            if (currentShip >= shipSize.length) {
                phasePositioning = false;
                btnGuidance.setEnabled(false);
                net.sendMessage("PRONTO");
                checkStartGame();
            } else {
                updateStatus("Fase de Preparação");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Posição inválida! O navio sai do mapa ou bate em outro.");
        }
    }

    private boolean canPosition(int initialLine, int initialCol, int size, boolean horizontal) {
        for (int i = 0; i < size; i++) {
            int l = initialLine + (horizontal ? 0 : i);
            int c = initialCol + (horizontal ? i : 0);

            if (l >= Map.TAMANHO || c >= Map.TAMANHO) {
                return false;
            }
            if (localPlayer.getOwnCellState(l, c) == Map.NAVIO) {
                return false;
            }
        }
        return true;
    }

    // ==============================
    // LOGICA DE TURNOS E REDE
    // ===========================
    private void checkStartGame() {
        if (!phasePositioning && opponentReady) {
            updateShiftPanel();
        } else if (!phasePositioning) {
            lblStatus.setText("Frota pronta! Aguardando oponente posicionar a dele...");
        }
    }

    private void updateShiftPanel() {
        if (MyTurn) {
            lblStatus.setText("SUA VEZ! Ataque o radar inimigo.");
        } else {
            lblStatus.setText("TURNO INIMIGO... Aguarde o ataque.");
        }
    }

    private void fireOpponent(int row, int col) {
        if (phasePositioning || !opponentReady || !MyTurn) {
            return;
        }

        JButton btn = opponentButtons[row][col];
        if (!btn.getText().equals("~")) {
            return;
        }

        MyTurn = false;
        updateShiftPanel();
        net.sendMessage("TIRO " + row + " " + col);
    }

    public void processNetworkMessage(String msg) {
        String[] parts = msg.split(" ");
        String comand = parts[0];

        if (comand.equals("NOME")) {
            this.opponentName = msg.substring(5);
            lblOpponent.setText("Radar de Ataque (" + this.opponentName + ")");
        } else if (comand.equals("PRONTO")) {
            opponentReady = true;
            checkStartGame();
        } else if (comand.equals("TIRO")) {
            int l = Integer.parseInt(parts[1]);
            int c = Integer.parseInt(parts[2]);

            int result = localPlayer.receiveAttack(l, c);

            if (result == Map.ACERTO) {
                sons.playMyShipHit();
                customButtons[l][c].setBackground(Color.RED);
                customButtons[l][c].setText("X");
            } else {
                customButtons[l][c].setBackground(Color.BLUE);
                customButtons[l][c].setText("*");
                sons.playMiss();
            }

            net.sendMessage("RESULTADO " + result + " " + l + " " + c);

            // VERIFICAÇÃO DE DERROTA
            if (localPlayer.isDefeated()) {
                Logs.saveVictory(opponentName, localPlayer.getName());
                net.sendMessage("VITORIA");
                
                sons.stopBGM();
                // Chama a nova função passando o texto da derrota
                promptRestart("💥 Fim de jogo! Sua frota foi destruída."); 
            } else {
                MyTurn = true;
                updateShiftPanel();
            }

        } else if (comand.equals("RESULTADO")) {
            int result = Integer.parseInt(parts[1]);
            int l = Integer.parseInt(parts[2]);
            int c = Integer.parseInt(parts[3]);

            JButton btn = opponentButtons[l][c];

            if (result == Map.ACERTO) {
                sons.playOponentShipHit();
                btn.setText("X");
                btn.setBackground(Color.RED);
            } else {
                sons.playMiss();
                btn.setText("*");
                btn.setBackground(Color.BLUE);
            }

            updateShiftPanel();
        } // VERIFICAÇÃO DE VITÓRIA
        else if (comand.equals("VITORIA")) {
            sons.stopBGM();
            // Chama a nova função passando o texto da vitória
            promptRestart("🏆 PARABÉNS! Você destruiu toda a frota de " + opponentName + " e VENCEU a batalha!"); 
        }
        // ==========================================
        // TRATAMENTO DA DECISÃO DE REINÍCIO
        // ==========================================
        else if (comand.equals("AGAIN")) {
            if (parts[1].equals("YES")) {
                opponentWantsToRestart = true;
                checkRestartSync();
            } else {
                JOptionPane.showMessageDialog(this, opponentName + " decidiu sair da partida. O jogo será encerrado.");
                
                net.closeConection();
                System.exit(0);
            }
        }
    }

    // ==========================================
    // SISTEMA DE REINÍCIO (SEM FECHAR O JOGO)
    // ==========================================
    
    // pergunta ao jogador local
    private void promptRestart(String mensagemFinal) {
        
        int option = JOptionPane.showConfirmDialog(this, 
                mensagemFinal + "\n\nDeseja jogar uma nova partida contra " + opponentName + "?", 
                "Fim de Partida", 
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            wantsToRestart = true;
            net.sendMessage("AGAIN YES");
            checkRestartSync();
        } else {
            net.sendMessage("AGAIN NO");
            
            // 400 milissegundos para garantir que a mensagem viaja pela internet antes do jogo se "suicidar"
            try { Thread.sleep(400); } catch (Exception e) {} 
            
            net.closeConection();
            System.exit(0);
        }
    }

    // verifica se os dois concordaram
    private void checkRestartSync() {
        if (wantsToRestart && opponentWantsToRestart) {
            resetGame();
        } else if (wantsToRestart && !opponentWantsToRestart) {
            lblStatus.setText("Aguardando " + opponentName + " decidir...");
        }
    }

    // limpa o tabuleiro e recomeça a partida do zero
    private void resetGame() {
        
        localPlayer.resetBoardsAndShips();

        
        phasePositioning = true;
        opponentReady = false;
        horizontal = true;
        currentShip = 0;
        MyTurn = isHost; 
        wantsToRestart = false;
        opponentWantsToRestart = false;

        
        for (int i = 0; i < Map.TAMANHO; i++) {
            for (int j = 0; j < Map.TAMANHO; j++) {
                customButtons[i][j].setText("~");
                customButtons[i][j].setBackground(Color.CYAN);
                customButtons[i][j].setEnabled(true);

                opponentButtons[i][j].setText("~");
                opponentButtons[i][j].setBackground(Color.CYAN);
                opponentButtons[i][j].setEnabled(true);
            }
        }

        btnGuidance.setEnabled(true);
        btnGuidance.setText("Orientação: HORIZONTAL");

        updateStatus("Fase de Preparação");
    }

    public Player getLocalPlayer() {
        return localPlayer;
    }
}
