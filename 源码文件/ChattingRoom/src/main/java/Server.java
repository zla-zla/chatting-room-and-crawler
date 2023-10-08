import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import Frame.ChattingRoomFrame;

import com.formdev.flatlaf.FlatLightLaf;
import org.json.JSONObject;
import db.UserDAO;

import javax.swing.*;

/*
 * ����������
 * ��Ա������socketMap���浱ǰ��¼��Ա��socket��,executorService���̳߳أ�,port,serverSocket
 * ������Server():���������˿ڣ�ÿ�����¿ͻ�����ʱ��һ����ͨ���̲߳������̳߳�
 */
public class Server {
    public static final int COMMAND_LOGIN=1;//���û���¼
    public static final int COMMAND_Register=2;//���û�ע��
    public static final int COMMAND_GROUP=3;//��������Ϣ
    public static final int COMMAND_SINGLE=4;//˽����Ϣ
    public static final int COMMAND_DROP=5;//�û�ע��
    public static final int COMMAND_FROCE=6;//�û�ע��
    public static final int COMMAND_REUSLT=7;//������ģ���Ҫ����
    public static final int COMMAND_HISTORY=8;//Ҫ���ѯ��ʷ��¼
    public static final int COMMAND_CONTROLD=9;//����Աǿ������
    public static final int COMMAND_CONTROLS=10;//����ԱȺ����Ϣ


    public static void main(String[] args) throws IOException {
        try {
            UIManager.setLookAndFeel( new FlatLightLaf() );
        } catch( Exception ex ) {
            System.err.println( "Failed to initialize LaF" );
        }
        Server();
    }

    //����������
    static UserDAO dao = new UserDAO();
    private static HashSet<String> userList;
    private static Map<String,Socket> socketMap=new ConcurrentHashMap<>();//�������д洢��socket���¼��ǰ��¼��Ա
    static int port =  10086;//�˿ں�
    final static ExecutorService executorService = Executors.newFixedThreadPool(10);//�̳߳�
    private static ServerSocket serverSocket;//������socket

    //���������˿ڣ�ÿ�����¿ͻ�����ʱ��һ����ͨ���̲߳������̳߳�
    public static void Server() throws IOException {
        System.out.println(Charset.defaultCharset());
        new control();
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("����������������ڣ�" + serverSocket.getLocalSocketAddress());
            userList = dao.findAllUser();// ��ѯ�����û���Ϣ
            while (true) {
                // �����ͻ��˵����ӣ�ÿ��һ���ͻ���һ���µ�ͨ���߳�
                Socket socket = serverSocket.accept();
                executorService.execute(new Server().new listener(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }

    //ÿ���û�һ��ͨ���̣߳�������������û����͵���Ϣ������
    class listener implements Runnable{
        Socket socket;//�����û���socket
        String name;//�û�������
        boolean flag=true;//�Ƿ񱣳�����
        listener(Socket s){socket=s;}
        @Override
        public void run() {
            try {
                //��ȡ�ͻ���������
                InputStream in = this.socket.getInputStream();
                Scanner scanner = new Scanner(in);
                while (flag) {
                    String str = "";
                    if(scanner.hasNext()){
                        str = scanner.nextLine();
                    }
                    else break;
                    System.out.println(str);
                    JSONObject jsonObj = new JSONObject(str);
                    int cmd = (int) jsonObj.get("command");
                    //��¼����
                    if (cmd == COMMAND_LOGIN){
                        // ͨ���û�����ѯ�û���Ϣƥ���˺�����
                        String tmp_name = (String) jsonObj.get("user_name");
                        Map<String, String> user = dao.findById(tmp_name);
                        // �жϿͻ��˷��͹��������������ݿ�������Ƿ�һ��
                        if (user != null && jsonObj.get("user_pwd").equals(user.get("user_pwd"))) {
                            name = tmp_name;
                            socketMap.put(name,this.socket);//socket��������û�

                            //���е�¼
                            JSONObject sendJsonObj = new JSONObject(user);//�������json��
                            sendJsonObj.put("result", 1);// ���result:0��ֵ�ԣ�1��ʾ�ɹ���0��ʾʧ��
                            Set<String> online = socketMap.keySet();// ������Ա�б�
                            sendJsonObj.put("online", online);
                            sendJsonObj.put("userList", userList);// �����ҳ�Ա������
                            sendMessage(socket,sendJsonObj.toString());// ����DatagramPacket����������ͻ��˷�������
                            //�㲥��ǰ�û�������
                            JSONObject broadcast = new JSONObject();
                            broadcast.put("command",1);
                            broadcast.put("name",name);
                            for(Socket socket:socketMap.values()){
                                sendMessage(socket,broadcast.toString());
                            }
                        }
                        else {
                            // ��ʧ����Ϣ
                            JSONObject sendJsonObj = new JSONObject();
                            sendJsonObj.put("result", 0);
                            sendMessage(socket,sendJsonObj.toString());
                        }
                    }
                    //ע�Ṧ��
                    else if(cmd == COMMAND_Register){
                        name = (String) jsonObj.get("user_name");
                        String pwd=(String) jsonObj.get("pwd");
                        dao.insertUser(name,pwd);//�������û�����
                        userList = dao.findAllUser();// �������û�������Ϣ

                        //�㲥���º���ܳ�Ա��
                        JSONObject broadcast = new JSONObject();
                        broadcast.put("command",COMMAND_Register);
                        broadcast.put("userList",userList);
                        for(Socket socket:socketMap.values()){
                            sendMessage(socket,broadcast.toString());
                        }
                        //�㲥��ǰ�û�������
                        broadcast = new JSONObject();
                        broadcast.put("command",COMMAND_LOGIN);
                        broadcast.put("name",name);
                        for(Socket socket:socketMap.values()){
                            sendMessage(socket,broadcast.toString());
                        }
                        //���е�¼
                        Map<String, String> user = dao.findById(name);
                        socketMap.put(name,this.socket);//socket��������û�
                        JSONObject sendJsonObj = new JSONObject(user);//�������json��
                        sendJsonObj.put("result", 1);// ���result:0��ֵ�ԣ�1��ʾ�ɹ���0��ʾʧ��
                        Set<String> online = socketMap.keySet();// ������Ա�б�
                        sendJsonObj.put("online", online);
                        sendJsonObj.put("userList", userList);// �����ҳ�Ա������
                        sendMessage(socket,sendJsonObj.toString());// ����DatagramPacket����������ͻ��˷�������
                    }
                    //������Ⱥ������
                    else if(cmd == COMMAND_GROUP){
                        jsonObj.put("name",name);
                        //�洢��ʷ��¼
                        Date date=new Date();
                        dao.insertMess(date,name,jsonObj.get("text").toString());
                        //Ⱥ����Ϣ
                        for(Socket socket:socketMap.values()){
                            sendMessage(socket,jsonObj.toString());
                        }

                    }
                    //˽�Ĺ���
                    else if(cmd == COMMAND_SINGLE){
                        if(socketMap.containsKey(jsonObj.get("target"))){
                            jsonObj.put("name",name);
                            String target=(String) jsonObj.get("target");
                            Socket s = socketMap.get(target);
                            sendMessage(s,jsonObj.toString());
                            jsonObj.put("result", 1);
                        }
                        else jsonObj.put("result", 0);
                        jsonObj.put("command",COMMAND_REUSLT);
                        sendMessage(socket,jsonObj.toString());
                    }
                    //ע������
                    else if(cmd == COMMAND_DROP){
                        //�޸ķ�������
                        socketMap.remove(name);
                        //�㲥���û�����
                        JSONObject broadcast = new JSONObject();
                        broadcast.put("command",COMMAND_DROP);
                        broadcast.put("name",name);
                        for(Socket socket:socketMap.values()){
                            sendMessage(socket,broadcast.toString());
                        }
                        //�ر�����
                        flag = false;
                        socket.close();
                    }
                    //������ʷ����
                    else if(cmd == COMMAND_HISTORY){
                        String datestr=jsonObj.get("date").toString();
                        DateFormat dFormat = new SimpleDateFormat("yyyyMMdd");
                        Date date=dFormat.parse(datestr);
                        ArrayList<String>history=dao.queryHistory(date);
                        //��װ���json�����ظ��û�
                        JSONObject sendJsonObj = new JSONObject();
                        sendJsonObj.put("history",history);
                        sendJsonObj.put("command",COMMAND_HISTORY);
                        sendMessage(socket,sendJsonObj.toString());
                    }
                    //����Աǿ������
                    else if(cmd == COMMAND_CONTROLD){
                        String name=jsonObj.get("name").toString();
                        dropf(name);

                    }
                    //����ԱȺ����Ϣ
                    else if(cmd == COMMAND_CONTROLS){
                        String text=jsonObj.get("text").toString();
                        sendf(text);
                    }
                }
            }catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

    }

    static void sendMessage(Socket client, String s) throws IOException {
        OutputStream out = client.getOutputStream();
        PrintStream printStream = new PrintStream(out);
        printStream.println(s);
        printStream.flush();
    }

    static class control extends JFrame {
        // ��õ�ǰ��Ļ�Ŀ�͸�
        private double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        private double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        // ��¼���ڿ�͸�
        private int frameWidth = 400;
        private int frameHeight = 300;

        public control(){
            Container c = getContentPane();

            JTextField user = new JTextField();
            user.setBounds(50,75,140,40);
            JButton drop = new JButton();
            drop.setBounds(50,150,140,40);
            drop.setText("ǿ������");
            c.add(user);
            c.add(drop);

            JTextField text = new JTextField();
            text.setBounds(200,75,140,40);
            JButton send = new JButton();
            send.setBounds(200,150,140,40);
            send.setText("����Ⱥ����Ϣ");
            c.add(text);
            c.add(send);




            // ���ô���λ����Ļ����
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setSize(frameWidth, frameHeight);
            setTitle("���ƽ���");
            c.setLayout(null);
            setVisible(true);
            // ע�ᴰ���¼�
            addWindowListener(new WindowAdapter() {
                // �������ڹرհ�ťʱ����
                public void windowClosing(WindowEvent e) {
                    // �˳�ϵͳ
                    System.exit(0);
                }
            });
            drop.addActionListener(e->{
                String name=user.getText().trim();
                dropf(name);
                //todo ���߹���
            });
            send.addActionListener(e->{
                String txt=text.getText().trim();
                sendf(txt);
            });

        }

    }
    static void dropf(String name){
        //����socket���ҵ��û����ֲ��ر�����socket
        for(Map.Entry<String,Socket>entry:socketMap.entrySet()){
            if(entry.getKey().equals(name)){
                try {
                    //����ǿ����������
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("command", COMMAND_FROCE);
                    sendMessage(entry.getValue(),jsonObj.toString());
                    //�ر�socket
                    entry.getValue().shutdownInput();
                    entry.getValue().shutdownOutput();
                    entry.getValue().close();
                    System.out.println(11111111);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    static void sendf(String txt){
        JSONObject jsonObj=new JSONObject();
        jsonObj.put("text",txt);
        jsonObj.put("name","����Ա");
        jsonObj.put("command",COMMAND_GROUP);
        jsonObj.put("picInfo","");
        String font="����|18|255-0-0|";
        font+=txt;
        jsonObj.put("font",font);

        for(Socket socket:socketMap.values()){
            try {
                sendMessage(socket,jsonObj.toString());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}

