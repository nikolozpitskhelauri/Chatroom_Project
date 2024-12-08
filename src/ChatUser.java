import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatUser extends Thread{
    private Scanner userScanner;
    private String name;
    private int id;
    private boolean running;
    private Socket socket;
    private String IP;
    private String port;

    ChatUser(String IP1, String port1, Scanner scanner) throws IOException {
        socket = new Socket(IP1, Integer.parseInt(port1));
        System.out.println("--Connected--");
        userScanner=scanner;
        name = "anonimus";
        this.IP = IP1;
        this.port = port1;
    }

    ChatUser(String name, int id){
        this.name = name;
        this.id = id;
    }

    public long getId() {
        return id;
    }


    public String getChatUserName() {
        return name;
    }

    @Override
    public void run() {
        running = true;
        Thread writerThread = new Thread(()->
        {
            try {
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                while (running) {
                    String line = userScanner.nextLine();
                    outputStream.writeObject(line);
                    outputStream.flush();
                }
                System.out.println("ended outputs");
            } catch (IOException e) {
                System.out.println("connection lost: "+e.getMessage());
                try {
                    socket = new Socket(IP, Integer.parseInt(port));
                    System.out.println("reconnected");
                } catch (IOException ex) {
                    System.out.println("failed to reconnect");
                }
            }
        }
        );

        Thread readerThread = new Thread(()->
        {
            try {
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                while (running) {
                    String line = (String)inputStream.readObject();
                    if(line.substring(0,4).equals("/id/"))
                        id=Integer.parseInt(line.substring(4));
                    else if (line.equals("you left the chat")) {
                        running = false;
                        System.out.println(line);
                    } else
                        System.out.println(line);
                }
                System.out.println("ended inputs");
            } catch (IOException e) {
                System.out.println("connection lost: "+e.getMessage());
                try {
                    socket = new Socket(IP, Integer.parseInt(port));
                    System.out.println("reconnected");
                } catch (IOException ex) {
                    System.out.println("failed to reconnect");
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        );
        readerThread.start();
        writerThread.start();
        try {
            readerThread.join();
            writerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        userScanner.close();
    }

    @Override
    public String toString() {
        return super.toString();
    }


}
