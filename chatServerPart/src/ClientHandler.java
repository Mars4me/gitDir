import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

// реализуем интерфейс Runnable, который позволяет работать с потоками
public class ClientHandler implements Runnable {
    // экземпляр нашего сервера
    private Server server;
    // исходящее сообщение
    private PrintWriter outMessage;
    // входящее собщение
    private Scanner inMessage;
    private static final String HOST = "localhost";
    private static final int PORT = 8189;
    // клиентский сокет
    private Socket clientSocket = null;
    // количество клиентов в чате, статичное поле
    private static int clients_count = 0;
    
    // конструктор, который принимает клиентский сокет и сервер
    public ClientHandler(Socket socket, Server server) {
        try {
            clients_count++;
            this.server = server;
            this.clientSocket = socket;
            this.outMessage = new PrintWriter(socket.getOutputStream());
            this.inMessage = new Scanner(socket.getInputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    @Override
    public void run() {
        try {
            while (true) {
                // сервер отправляет сообщение
                server.sendMessageToAllClients("Новый участник вошёл в чат!");
                server.sendMessageToAllClients("Клиентов в чате = " + clients_count);
                break;
            }

            while (true) {
                // Если от клиента пришло сообщение
                if (inMessage.hasNext()) {
                    String clientMessage = inMessage.nextLine();
                    System.out.println("сообщение клиента: "+ clientMessage);
                    if (isAdmin(clientMessage)) adminCommands(clientMessage);
//                    if (isAdmin(clientMessage)){
//                        if (clientMessage.contains("/s_exit")) {
//                            server.sendMessageToAllClients("Сервер отключен");
//                            System.out.println("Сервер закрыт по запросу администратора");
//                            System.exit(2);
//                        }
//                    }
                    // если клиент отправляет данное сообщение, то цикл прерывается и
                    // клиент выходит из чата
                    if (clientMessage.equalsIgnoreCase("##session##end##")) {
                        break;
                    }
                    // выводим в консоль сообщение (для теста)
                    System.out.println(clientMessage);
                    // отправляем данное сообщение всем клиентам
                    server.sendMessageToAllClients(clientMessage);
                }
                // останавливаем выполнение потока на 100 мс
                Thread.sleep(100);
            }
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        finally {
            this.close();
        }
    }
    // отправляем сообщение
    public void sendMsg(String msg) {
        try {
            outMessage.println(msg);
            outMessage.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    // клиент выходит из чата
    public void close() {
        // удаляем клиента из списка
        server.removeClient(this);
        clients_count--;
        server.sendMessageToAllClients("Клиентов в чате = " + clients_count);
    }

    // является ли клиент администратором
    public boolean isAdmin(String str) {
        List<String> admins = Arrays.asList("admin", "adminVitalii","VitaliiBoSS");
        for(int i = 0; i <admins.size();i++)
            if (str.substring(0, str.indexOf(" ") - 1).equalsIgnoreCase(admins.get(i))) return true;
        return false;
    }

    //метод для работы с командами администратора
    public void adminCommands(String str) {
        if(str.contains("/")) {
            if (str.contains("/s_exit")) {
                server.sendMessageToAllClients("Сервер отключен");
                System.out.println("Сервер закрыт по запросу администратора");
                System.exit(3);
            }
            if (str.contains("/s_cleanChat")) {
                server.sendMessageToAllClients("##clean_chat##");
                System.out.println("cleanChat");
            }
            if (str.contains("/s_cancel")) {
                System.out.println("s_cancel");
            }
        }
        }
    }
