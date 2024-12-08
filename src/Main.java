import java.io.IOException;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        String[] input = line.split(":");
        if(input.length==2){
            ChatUser chatUser = new ChatUser(input[0],input[1],scanner);
            //commands:
            //1./commands/
            //shows all commands
            //2./changeName/<new name>
            //changes your name to new name
            //3./personalMessage/<id>/<message>
            //send personal message
            //4./leaveChat/
            //leave chat
            //5./getInfo/
            //shows your name, id and number of members in your chat
            chatUser.start();
            chatUser.join();
            scanner.close();
        } else if (input.length == 1) {
            ChatRoom chatRoom = new ChatRoom(input[0]);
            chatRoom.start();
            Thread.currentThread().join();
        }else{
            System.out.println("Something went wrong");
        }
        System.out.println("program ended");
    }
}