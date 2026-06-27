package JavaSeaWarfare.Network;

import JavaSeaWarfare.GUI.Screen;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.SwingUtilities;

public class Net {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Screen screen;
    private boolean isServer;
    private int port = 12345;

    public Net(Screen screen, boolean isServer, String ip) {
        this.screen = screen;
        this.isServer = isServer;

        // Thread separada para estabelecer a conexão e ouvir as mensagens
        new Thread(() -> {
            try {
                if (isServer) {
                    screen.updateStatus("Aguardando oponente se conectar...");
                    ServerSocket serverSocket = new ServerSocket(port);
                    this.socket = serverSocket.accept();
                    screen.updateStatus("Oponente conectado! Posicione os navios.");
                } else {
                    screen.updateStatus("Conectando ao servidor...");
                    this.socket = new Socket(ip, port);
                    screen.updateStatus("Conectado com sucesso! Posicione os navios.");
                }

                this.out = new PrintWriter(socket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                sendMessage("NOME " + screen.getLocalPlayer().getName());
                
                // starta o loop de escuta de mensagens na rede
                readsNet();

            } catch (Exception e) {
                System.err.println("Erro na conexão: " + e.getMessage());
                SwingUtilities.invokeLater(() -> screen.updateStatus("Erro de conexão."));
            }
        }).start();
    }

    // metodo que roda em segundo plano interceptando comandos do outro jogador
    private void readsNet() {
        try {
            String txt;
            while ((txt = in.readLine()) != null) {
                final String msg = txt;
                
                // componentes gráficos de dentro de uma Thread secundaria
                SwingUtilities.invokeLater(() -> {
                    screen.processNetworkMessage(msg);
                });
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> screen.updateStatus("Oponente desconectou."));
        }
    }

    // envia uma mensagem de texto para o outro computador - ? teste
    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }
    
    public void closeConection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            System.err.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }
}

