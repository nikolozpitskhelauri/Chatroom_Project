import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class ChatRoom extends Thread{
    private int port;
    private ArrayList<ChatUser> users;
    private int id;
    ChatRoom(String port_){
        port = Integer.parseInt(port_);
        inputs = new ArrayList<>();
        output = new ArrayList<>();
        users = new ArrayList<>();
        id = 0;
    }
    private ArrayList<ObjectInputStream> inputs;
    private ArrayList<ObjectOutputStream> output;

    @Override
    public void run() {
        Connections connections = new Connections();
        connections.start();
        try {
            connections.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private  class Connections extends Thread{
       private ArrayList<T> threads;
        Connections(){
            threads = new ArrayList<>();
        }

        @Override
        public void run() {
            Thread Accept = new Thread(()->{
                try {
                    ServerSocket serverSocket = new ServerSocket(port);
                    System.out.println("--Server Connected--");;
                    while(true) {
                        Socket socket = serverSocket.accept();
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                        objectOutputStream.writeObject("/id/"+id);
                        objectOutputStream.flush();
                        inputs.add(objectInputStream);
                        output.add(objectOutputStream);
                        System.out.println("--New User Connected--");
                        users.add(new ChatUser("anonimus", id));
                        add();
                        id++;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
            Accept.start();
            try {
                Accept.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        void add(){
            T t = new T(id);
            threads.add(t);
            threads.stream().forEach((T t1)->{t1.setSizeOfList(id+1);});
            t.start();
        }
        class T extends Thread {
            private int i;
            private AtomicInteger sizeOfList;
            T(int i1){
                i = i1;
                sizeOfList = new AtomicInteger(0);
            }

            public void setSizeOfList(int sizeOfList) {
                this.sizeOfList.set(sizeOfList);
            }

            @Override
            public synchronized void run() {
                    try {
                        ObjectInputStream bridge_i = inputs.get(i);
                        String changeName = "/changeName/";
                        String personalMessage = "^/personalMessage/\\d+/";
                        Pattern isChangeName = Pattern.compile(changeName);
                        Pattern isPersonalMessage = Pattern.compile(personalMessage);
                        ObjectOutputStream person_i_outputStream = output.get(i);
                        person_i_outputStream.writeObject("Welcome!");
                        person_i_outputStream.flush();
                        for (int index = 0; index < sizeOfList.get() && users.get(index)!=null; index++) {
                            if (index != i) {
                                ObjectOutputStream objectOutputStream = output.get(index);
                                objectOutputStream.writeObject("New member added");
                                objectOutputStream.flush();
                            }
                        }
                        while(users.get(i)!=null) {
                            String line = (String) bridge_i.readObject();
                            System.out.println("message received " + line);
                            if (isChangeName.matcher(line).find()) {
                                users.get(i).setName(line.substring(changeName.length()));
                                person_i_outputStream.writeObject("Name successfully changed");
                                person_i_outputStream.flush();
                            } else if (isPersonalMessage.matcher(line).find()) {
                                String[] helperString = line.split("/");
                                int receiverId = Integer.parseInt(helperString[2]);
                                String privateMessage = helperString[3];
                                if (receiverId >= threads.size() || receiverId < 0) {
                                    person_i_outputStream.writeObject("wrong id");
                                    person_i_outputStream.flush();
                                } else {
                                    ObjectOutputStream secretReceiver = output.get(receiverId);
                                    secretReceiver.writeObject("--" + users.get(i).getId() + "--" + users.get(i).getChatUserName() + "--" + "private message--" + privateMessage);
                                }
                            } else if (line.equals("/leaveChat/")) {
                                for (int index = 0; index < sizeOfList.get() && users.get(index) != null; index++) {
                                    if (index != i) {
                                        ObjectOutputStream objectOutputStream = output.get(index);
                                        objectOutputStream.writeObject("user --" + users.get(i).getId() + "--" + users.get(i).getChatUserName() + "--left chat");
                                        objectOutputStream.flush();
                                    }
                                }
                                person_i_outputStream.writeObject("you left the chat");
                                users.add(i, null);
                                users.remove(i + 1);
                            } else if (line.equals("/getInfo/")) {
                                int alive = 0;
                                for (T thread : threads) {
                                    if (thread.isAlive()) alive++;
                                }
                                person_i_outputStream.writeObject(new String(">>your id is " + users.get(i).getId()));
                                person_i_outputStream.writeObject(new String(">>your name is " + users.get(i).getChatUserName()));
                                person_i_outputStream.writeObject(new String(">>number of members: " + alive));
                            }else if(line.equals("/commands/")){
                                person_i_outputStream.writeObject(new String("--commands--"));
                                person_i_outputStream.writeObject(new String("1./commands/"));
                                person_i_outputStream.writeObject(new String("shows all commands"));
                                person_i_outputStream.writeObject(new String("2./changeName/<new name>"));
                                person_i_outputStream.writeObject(new String("changes your name to new name"));
                                person_i_outputStream.writeObject(new String("3./personalMessage/<id>/<message>"));
                                person_i_outputStream.writeObject(new String("send personal message"));
                                person_i_outputStream.writeObject(new String("4./leaveChat/"));
                                person_i_outputStream.writeObject(new String("leave chat"));
                                person_i_outputStream.writeObject(new String("5./getInfo/"));
                                person_i_outputStream.writeObject(new String("shows your name, id and number of members in your chat"));
                                person_i_outputStream.writeObject(new String("------------"));
                        }else{
                                String message = "--" + users.get(i).getId() + "--" + users.get(i).getChatUserName() + "--:" + line;
                                for (int index = 0; index < sizeOfList.get() && users.get(index)!=null; index++) {
                                    if (index != i) {
                                        ObjectOutputStream objectOutputStream = output.get(index);
                                        objectOutputStream.writeObject(message);
                                        objectOutputStream.flush();
                                    }
                                }
                            }
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
            }
        };
    }
}

