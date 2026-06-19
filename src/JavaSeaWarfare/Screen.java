package JavaSeaWarfare;

import javax.swing.*;
import java.awt.*;

public class Screen extends JFrame {

    // Interfaces Visuais               PTBR
    private JButton[][] botoesAdversario;
    private JButton[][] botoesProprios;
    private JLabel lblStatus;
    private JButton btnOrientacao;

    private Player jogadorLocal;
    private Net net;
    private boolean minhaVez;
    
    // Controle da Fase de Posicionamento
    private boolean phasePositioning = true;
    private boolean opponentReady = false;
    private boolean horizontal = true; // é quem controla se o navio deita ou fica em pé na hora do posicionamento
    private int currentShip = 0;
    
    private String[] shipNames = {"Porta-Aviões", "Encouraçado", "Contratorpedeiro", "Submarino"};
    private int[] shipSize = {5, 4, 3, 2};

    public Screen(String playerName, boolean hospedar, String ip) {
        this.jogadorLocal = new Player(playerName);
        this.minhaVez = hospedar; // servidor sempre atira primeiro depois do setup

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
        JPanel painelCentral = new JPanel(new GridLayout(1, 2, 20, 0));
        painelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // mapa do Jogador local - Esquerda
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Sua Frota", SwingConstants.CENTER), BorderLayout.NORTH);
        
        JPanel gradePropria = new JPanel(new GridLayout(Map.TAMANHO, Map.TAMANHO));
        botoesProprios = new JButton[Map.TAMANHO][Map.TAMANHO];
        for (int i = 0; i < Map.TAMANHO; i++) {
            for (int j = 0; j < Map.TAMANHO; j++) {
                JButton btn = new JButton("~");
                btn.setBackground(Color.CYAN);
                final int l = i, c = j;
                
                // acao para posicionar navio ao clicar
                btn.addActionListener(e -> positionShip(l, c));
                
                botoesProprios[i][j] = btn;
                gradePropria.add(btn);
            }
        }
        leftPanel.add(gradePropria, BorderLayout.CENTER);

        // para girar o navio de horizonta e vertical
        btnOrientacao = new JButton("Orientação: HORIZONTAL");
        btnOrientacao.setBackground(Color.YELLOW);
        btnOrientacao.addActionListener(e -> {
            horizontal = !horizontal;
            btnOrientacao.setText("Orientação: " + (horizontal ? "HORIZONTAL" : "VERTICAL"));
        });
        leftPanel.add(btnOrientacao, BorderLayout.SOUTH);

        // mapa do Oponente / Radar - Direita
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Radar de Ataque", SwingConstants.CENTER), BorderLayout.NORTH);
        
        JPanel opponentGrid = new JPanel(new GridLayout(Map.TAMANHO, Map.TAMANHO));
        botoesAdversario = new JButton[Map.TAMANHO][Map.TAMANHO];
        for (int i = 0; i < Map.TAMANHO; i++) {
            for (int j = 0; j < Map.TAMANHO; j++) {
                JButton btn = new JButton("~");
                btn.setBackground(Color.CYAN);
                final int l = i, c = j;
                
                // acao para atirar no inimigo
                btn.addActionListener(e -> fireOpponent(l, c));
                
                botoesAdversario[i][j] = btn;
                opponentGrid.add(btn);
            }
        }
        rightPanel.add(opponentGrid, BorderLayout.CENTER);

        // add os dois tabuleiros à tela
        painelCentral.add(leftPanel);
        painelCentral.add(rightPanel);
        add(painelCentral, BorderLayout.CENTER);

        // inicializa a conexão
        this.net = new Net(this, hospedar, ip);
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
        if (!phasePositioning) return; // Se já colocou tudo, ignora cliques aqui

        int size = shipSize[currentShip];

        if (canPosition(row, col, size, horizontal)) {
            Ship s = new Ship(shipNames[currentShip], size);

            // Preenche o navio visualmente e na memria
            for (int j = 0; j < size; j++) {
                int l = row + (horizontal ? 0 : j);
                int c = col + (horizontal ? j : 0);
                
                s.addPosition(l, c);
                botoesProprios[l][c].setBackground(Color.DARK_GRAY); // Pinta de cinza para o jogador ver
                botoesProprios[l][c].setEnabled(false); // Desativa para não clicar de novo por cima
            }

            jogadorLocal.addShip(s);
            currentShip++;

            // verificacao se  colocau todos os navios
            if (currentShip >= shipSize.length) {
                phasePositioning = false;
                btnOrientacao.setEnabled(false); // desativa o botão de girar 
                
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

            if (l >= Map.TAMANHO || c >= Map.TAMANHO) return false; // saiu do mapa
            if (jogadorLocal.getOwnCellState(l, c) == Map.NAVIO) return false; // bateu em navio
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
        if (minhaVez) {
            lblStatus.setText("SUA VEZ! Ataque o radar inimigo.");
        } else {
            lblStatus.setText("TURNO INIMIGO... Aguarde o ataque.");
        }
    }

    private void fireOpponent(int row, int col) {
        // trava os tiros se estiver na fase de colocar navios, se o oponente não estiver pronto ou se não for sua vez.
        if (phasePositioning || !opponentReady || !minhaVez) return;

        JButton btn = botoesAdversario[row][col];
        if (!btn.getText().equals("~")) return; // impede atirar onde já atirou

        minhaVez = false;
        updateShiftPanel();
        net.sendMessage("TIRO " + row + " " + col);
    }

    public void processNetworkMessage(String msg) {
        String[] parts = msg.split(" ");
        String comand = parts[0];

        // sincronizador: oponente avisa que terminou de posicionar a frota dele
        if (comand.equals("PRONTO")) {
            opponentReady = true;
            checkStartGame();
        } 
        // o oponente atacou
        else if (comand.equals("TIRO")) {
            int l = Integer.parseInt(parts[1]);
            int c = Integer.parseInt(parts[2]);

            int result = jogadorLocal.receiveAttack(l, c);

            // ATUALIZAÇÃO VISUAL NO MAPA para ver onde o tiro pegou
            if (result == Map.ACERTO) {
                botoesProprios[l][c].setBackground(Color.RED);
                botoesProprios[l][c].setText("X");
            } else {
                botoesProprios[l][c].setBackground(Color.BLUE);
                botoesProprios[l][c].setText("*");
            }

            net.sendMessage("RESULTADO " + result + " " + l + " " + c);

            if (jogadorLocal.isDefeated()) {
                Logs.saveVictory("Oponente", jogadorLocal.getName());
                net.closeConection();
                JOptionPane.showMessageDialog(this, "Fim de jogo! Sua frota foi destruída.");
                System.exit(0);
            }

            minhaVez = true;
            updateShiftPanel();

        } 
        // resposta se acerta ou erra o tiro no radar
        else if (comand.equals("RESULTADO")) {
            int result = Integer.parseInt(parts[1]);
            int l = Integer.parseInt(parts[2]);
            int c = Integer.parseInt(parts[3]);

            JButton btn = botoesAdversario[l][c];

            if (result == Map.ACERTO) {
                btn.setText("X");
                btn.setBackground(Color.RED);
            } else {
                btn.setText("*");
                btn.setBackground(Color.BLUE);
            }
            
            updateShiftPanel();
        }
    }
}