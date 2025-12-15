import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class UdpServer {
    private static final int PORT = 9876;

    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            System.out.println("UDP Server running on port " + PORT + "...");

            while (true) {
                // Dimensiunea maxima a pachetului pe care o asteptam
                byte[] receiveData = new byte[1024]; 
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                
                // Asteapta pachetul de la client
                serverSocket.receive(receivePacket);
                
                // Extrage datele (pentru a avea dimensiunea corecta la trimitere)
                byte[] receivedBytes = Arrays.copyOfRange(receivePacket.getData(), 0, receivePacket.getLength());
                
                // Trimite datele inapoi (echo) la adresa si portul clientului
                DatagramPacket sendPacket = new DatagramPacket(
                    receivedBytes, 
                    receivedBytes.length, 
                    receivePacket.getAddress(), 
                    receivePacket.getPort()
                );
                serverSocket.send(sendPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}