import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * создание клиента со всеми необходимыми утилитами, точка входа в программу в классе Client
 */

class ClientSomthing {
    
    private Socket socket;
    private BufferedReader in; // поток чтения из сокета
    private BufferedWriter out; // поток чтения в сокет
    private BufferedReader inputUser; // поток чтения с консоли
    private String addr; // ip адрес клиента
    private int port; // порт соединения
    private String nickname; // имя клиента
    private Date time;
    private String dtime;
    private SimpleDateFormat dt1;
    
    /**
     * для создания необходимо принять адрес и номер порта
     *
     * @param addr
     * @param port
     */
    
    public ClientSomthing(String addr, int port) {
        this.addr = addr;
        this.port = port;
        try {
            this.socket = new Socket(addr, port);
        } catch (IOException e) {
            System.err.println("Socket failed");
        }
        try {
            // потоки чтения из сокета / записи в сокет, и чтения с консоли
            inputUser = new BufferedReader(new InputStreamReader(System.in));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.pressNickname(); // перед началом необходимо спросит имя
            new ReadMsg().start(); // нить читающая сообщения из сокета в бесконечном цикле
            new WriteMsg().start(); // нить пишущая сообщения в сокет приходящие с консоли в бесконечном цикле
        } catch (IOException e) {
            // Сокет должен быть закрыт при любой
            // ошибке, кроме ошибки конструктора сокета:
            ClientSomthing.this.downService();
        }
        // В противном случае сокет будет закрыт
        // в методе run() нити.
    }
    
    /**
     * просьба ввести имя,
     * и отсылка эхо с приветсвием на сервер
     */
    
    private void pressNickname() {
        System.out.print("Press your nick: ");
        try {
            nickname = inputUser.readLine();
            out.write("Hello " + nickname + "\n");
            out.flush();
        } catch (IOException ignored) {
        }
        
    }
    
    /**
     * закрытие сокета
     */
    private void downService() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
            }
        } catch (IOException ignored) {}
    }
    
    // нить чтения сообщений с сервера
    private class ReadMsg extends Thread {
        @Override
        public void run() {
            
            String str;
            try {
                while (true) {
                    str = in.readLine(); // ждем сообщения с сервера
                    if (str.equals("stop")) {
                        ClientSomthing.this.downService(); // харакири
                        break; // выходим из цикла если пришло "stop"
                    }
                    System.out.println(str); // пишем сообщение с сервера на консоль
                }
            } catch (IOException e) {
                ClientSomthing.this.downService();
            }
        }
    }
    
    // нить отправляющая сообщения приходящие с консоли на сервер
    public class WriteMsg extends Thread {
        
        @Override
        public void run() {
            while (true) {
                String userWord;
                try {
                    time = new Date(); // текущая дата
                    dt1 = new SimpleDateFormat("HH:mm:ss"); // берем только время до секунд
                    dtime = dt1.format(time); // время
                    userWord = inputUser.readLine(); // сообщения с консоли
                    if (userWord.equals("stop")) {
                        out.write("stop" + "\n");
                        ClientSomthing.this.downService(); // харакири
                        break; // выходим из цикла если пришло "stop"
                    } else {
                        out.write("(" + dtime + ") " + nickname + ": " + userWord + "\n"); // отправляем на сервер
                    }
                    out.flush(); // чистим
                } catch (IOException e) {
                    ClientSomthing.this.downService(); // в случае исключения тоже харакири
                    
                }
                
            }
        }
    }
}

public class Client {
    
    public static String ipAddr = "localhost";
    public static int port = 8080;
    
    /**
     * создание клиент-соединения с узананными адресом и номером порта
     * @param args
     */
    
    public static void main(String[] args) {
        new ClientSomthing(ipAddr, port);
    }
}