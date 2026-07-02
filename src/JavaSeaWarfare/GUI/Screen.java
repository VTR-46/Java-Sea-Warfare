package JavaSeaWarfare.GUI;

import JavaSeaWarfare.Map.Map;
import JavaSeaWarfare.Logs.Logs;
import JavaSeaWarfare.Network.Net;
import JavaSeaWarfare.Game.Player;
import JavaSeaWarfare.Game.Ship;
import JavaSeaWarfare.Sounds.Sounds;
import javax.swing.*;
import java.awt.*;

public class Screen extends JFrame {

    // Interfaces Visuais               PTBR
    private JButton[][] opponentButtons;
    private JButton[][] customButtons;
    private JLabel lblStatus;
    private JButton btnGuidance;

    private JLabel lblMy;
    private JLabel lblOpponent;

    private Player localPlayer;
    private String opponentName = "Desconhecido";
    private Net net;
    private boolean MyTurn;
    private Sounds sons;

    // Controle da Fase de Posicionamento
    private boolean phasePositioning = true;
    private boolean opponentReady = false;
    private boolean horizontal = true; // é quem controla se o navio deita ou fica em pé na hora do posicionamento
    private int currentShip = 0;
    
    private String[] shipNames = {"Porta-Aviões", "Encouraçado", "Contratorpedeiro", "Submarino"};
    private int[] shipSize = {5, 4, 3, 2};

    public Screen(String playerName, boolean host, String ip) {
        this.localPlayer = new Player(playerName);
        this.MyTurn = host; // servidor sempre atira primeiro depois do setup

        // -----UI-----
        setTitle("Batalha Naval - " + playerName);
        setSize(900, 500); //tamanho da jadenla
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- BARRA DE STATUS SUPERIOR ---
        lblStatus = new JLabel("Conectando...", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblStatus, BorderLayout.NORTH);

        // --- PAINEL CENTRAL (DOIS TABULEIROS LADO A LADO) ---
        JPanel centralPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centralPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // mapa do Jogador local - Esquerda
        JPanel leftPanel = new JPanel(new BorderLayout());
        lblMy = new JLabel("Sua Frota (" + localPlayer.getName() + ")", SwingConstants.CENTER); // CORREÇÃO AQUI
        leftPanel.add(lblMy, BorderLayout.NORTH);

        JPanel ownGrid = new JPanel(new GridLayout(Map.TAMANHO, Map.TAMANHO));
        customButtons = new JButton[Map.TAMANHO][Map.TAMANHO];
        for (int i = 0; i < Map.TAMANHO; i++) {
            for (int j = 0; j < Map.TAMANHO; j++) {
                JButton btn = new JButton("~");
                btn.setBackground(Color.CYAN);
                final int l = i, c = j;

                // acao para posicionar navio ao clicar
                btn.addActionListener(e -> positionShip(l, c));

                customButtons[i][j] = btn;
                ownGrid.add(btn);
            }
        }
        leftPanel.add(ownGrid, BorderLayout.CENTER);

        // para girar o navio de horizonta e vertical
        btnGuidance = new JButton("Orientação: HORIZONTAL");
        btnGuidance.setBackground(Color.YELLOW);
        btnGuidance.addActionListener(e -> {
            horizontal = !horizontal;
            btnGuidance.setText("Orientação: " + (horizontal ? "HORIZONTAL" : "VERTICAL"));
        });
        leftPanel.add(btnGuidance, BorderLayout.SOUTH);

        // mapa do Oponente / Radar - Direita
        JPanel rightPanel = new JPanel(new BorderLayout());
        lblOpponent = new JLabel("Radar de Ataque (Aguardando...)", SwingConstants.CENTER); // CORREÇÃO AQUI
        rightPanel.add(lblOpponent, BorderLayout.NORTH);

        JPanel opponentGrid = new JPanel(new GridLayout(Map.TAMANHO, Map.TAMANHO));
        opponentButtons = new JButton[Map.TAMANHO][Map.TAMANHO];
        for (int i = 0; i < Map.TAMANHO; i++) {
            for (int j = 0; j < Map.TAMANHO; j++) {
                JButton btn = new JButton("~");
                btn.setBackground(Color.CYAN);
                final int l = i, c = j;

                // acao para atirar no inimigo
                btn.addActionListener(e -> fireOpponent(l, c));

                opponentButtons[i][j] = btn;
                opponentGrid.add(btn);
            }
        }
        rightPanel.add(opponentGrid, BorderLayout.CENTER);

        // add os dois tabuleiros à tela
        centralPanel.add(leftPanel);
        centralPanel.add(rightPanel);
        add(centralPanel, BorderLayout.CENTER);

        // inicializa a conexão e o sons do jogo
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
            return; // Se já colocou tudo, ignora cliques aqui
        }
        int size = shipSize[currentShip];

        if (canPosition(row, col, size, horizontal)) {
            Ship s = new Ship(shipNames[currentShip], size);

            // Preenche o navio visualmente e na memria
            for (int j = 0; j < size; j++) {
                int l = row + (horizontal ? 0 : j);
                int c = col + (horizontal ? j : 0);

                s.addPosition(l, c);
                customButtons[l][c].setBackground(Color.DARK_GRAY); // Pinta de cinza para o jogador ver
                customButtons[l][c].setEnabled(false); // Desativa para não clicar de novo por cima
            }

            localPlayer.addShip(s);
            currentShip++;

            // verificacao se  colocau todos os navios
            if (currentShip >= shipSize.length) {
                phasePositioning = false;
                btnGuidance.setEnabled(false); // desativa o botão de girar 

                // avisa o adversario pela rede que terminou de arrumar o mapa
                net.sendMessage("PRONTO");
                checkStartGame();
            } else {
                updateStatus("Fase de Preparação"); // atualiza o nome do prox navio
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
                return false; // saiu do mapa
            }
            if (localPlayer.getOwnCellState(l, c) == Map.NAVIO) {
                return false; // bateu em navio
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
        // trava os tiros se estiver na fase de colocar navios, se o oponente não estiver pronto ou se não for sua vez.
        if (phasePositioning || !opponentReady || !MyTurn) {
            return;
        }

        JButton btn = opponentButtons[row][col];
        if (!btn.getText().equals("~")) {
            return; // impede atirar onde já atirou
        }
        MyTurn = false;
        updateShiftPanel();
        net.sendMessage("TIRO " + row + " " + col);
    }

    public void processNetworkMessage(String msg) {
        String[] parts = msg.split(" ");
        String comand = parts[0];

        if (comand.equals("NOME")) {
            // O substring(5) pega todo o texto depois de "NOME " para suportar nomes com espaço
            this.opponentName = msg.substring(5);
            lblOpponent.setText("Radar de Ataque (" + this.opponentName + ")");
        }

        // sincronizador: oponente avisa que terminou de posicionar a frota dele
        if (comand.equals("PRONTO")) {
            opponentReady = true;
            checkStartGame();
        } // o oponente atacou
        else if (comand.equals("TIRO")) {
            int l = Integer.parseInt(parts[1]);
            int c = Integer.parseInt(parts[2]);

            int result = localPlayer.receiveAttack(l, c);

            // ATUALIZAÇÃO VISUAL NO MAPA para ver onde o tiro pegou
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

            if (localPlayer.isDefeated()) {
                Logs.saveVictory(opponentName, localPlayer.getName());

                net.sendMessage("VITORIA");
                    
                JOptionPane.showMessageDialog(this, "💥 Fim de jogo! Sua frota foi destruída.");
                System.exit(0);
            }

            MyTurn = true;
            updateShiftPanel();

        } // resposta se acerta ou erra o tiro no radar
        else if (comand.equals("RESULTADO")) {
            int result = Integer.parseInt(parts[1]);
            int l = Integer.parseInt(parts[2]);
            int c = Integer.parseInt(parts[3]);

            JButton btn = opponentButtons[l][c];

            if (result == Map.ACERTO) {
                sons.playOponentShipHit();
                btn.setText("X");
                btn.setBackground(Color.RED);
            } else {
                btn.setText("*");
                btn.setBackground(Color.BLUE);
                sons.playMiss();
            }
            
            updateShiftPanel();
        }
        // Condicao para exibir a mensagem de vitória
        else if (comand.equals("VITORIA")) {
            sons.playWin();
            JOptionPane.showMessageDialog(this, "🏆 PARABÉNS! Você destruiu toda a frota de " + opponentName + " e VENCEU a batalha!");
            net.closeConection();
            System.exit(0);
        }
    }

    public Player getLocalPlayer() {
        return localPlayer;
    }
}
