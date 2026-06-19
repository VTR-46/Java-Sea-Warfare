package JavaSeaWarfare;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logs {
    private static final String ARQUIVO = "historico_batalha_naval.txt";

    // salva o resultado no final da partida
    public static void saveVictory(String vencedor, String perdedor) {

        try (FileWriter fw = new FileWriter(ARQUIVO, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            
            String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String register = "[" + dateTime + "] Vencedor: " + vencedor + " | Derrotado: " + perdedor;
            
            bw.write(register);
            bw.newLine(); // pula para a próxima linha
            
        } catch (IOException e) {
            System.err.println("Erro ao salvar histórico: " + e.getMessage());
        }
    }

    // le o arquivo de texto para mostrar na tela
    public static String lerHistorico() {
        File file = new File(ARQUIVO);
        if (!file.exists()) {
            return "Nenhuma partida registrada ainda.";
        }

        StringBuilder sb = new StringBuilder();
        try (FileReader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr)) {
            
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            
        } catch (IOException e) {
            return "Erro ao ler histórico: " + e.getMessage();
        }
        return sb.toString();
    }
}