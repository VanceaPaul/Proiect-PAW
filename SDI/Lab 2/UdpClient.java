import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

public class UdpClient {
    private static final int SERVER_PORT = 9876;
    private static final int[] PACKET_SIZES = {128, 512, 1024};
    private static final int TIMEOUT_MS = 5000;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java UdpClient <Server IP>");
            System.out.println("Ex: java UdpClient 192.168.37.100");
            return;
        }

        String serverIp = args[0];
        System.out.println("Testing server at: " + serverIp);

        try (DatagramSocket clientSocket = new DatagramSocket()) {
            clientSocket.setSoTimeout(TIMEOUT_MS);
            InetAddress serverAddress = InetAddress.getByName(serverIp);

            for (int size : PACKET_SIZES) {
                testPacketSize(clientSocket, serverAddress, size);
            }
        } catch (Exception e) {
            System.err.println("Client Error: " + e.getMessage());
        }
    }

    private static void testPacketSize(DatagramSocket socket, InetAddress address, int size) {
        try {
            // Creeaza date aleatoare de dimensiunea specificata
            byte[] sendData = new byte[size];
            new Random().nextBytes(sendData);
            
            DatagramPacket sendPacket = new DatagramPacket(sendData, size, address, SERVER_PORT);
            
            // Inregistreaza timpul de start
            long startTime = System.nanoTime();
            
            // Trimite pachetul
            socket.send(sendPacket);

            // Asteapta raspuns
            byte[] receiveData = new byte[size]; 
            DatagramPacket receivePacket = new DatagramPacket(receiveData, size);
            socket.receive(receivePacket);
            
            // Inregistreaza timpul de sfarsit
            long endTime = System.nanoTime();
            
            long rttMicroseconds = (endTime - startTime) / 1000;

            System.out.printf("RTT for %4d bytes: %d microseconds (%.2f ms)%n", 
                              size, rttMicroseconds, rttMicroseconds / 1000.0);

        } catch (java.net.SocketTimeoutException e) {
            System.out.printf("RTT for %4d bytes: TIMEOUT%n", size);
        } catch (Exception e) {
            System.err.println("Error during test for " + size + " bytes: " + e.getMessage());
        }
    }
}