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
 * 服务器程序
 * 成员变量：socketMap（存当前登录人员的socket）,executorService（线程池）,port,serverSocket
 * 主程序Server():持续监听端口，每当有新客户连接时开一个新通信线程并丢进线程池
 */
public class Server {
    public static final int COMMAND_LOGIN=1;//新用户登录
    public static final int COMMAND_Register=2;//新用户注册
    public static final int COMMAND_GROUP=3;//聊天室消息
    public static final int COMMAND_SINGLE=4;//私聊消息
    public static final int COMMAND_DROP=5;//用户注销
    public static final int COMMAND_FROCE=6;//用户注销
    public static final int COMMAND_REUSLT=7;//结果报文，需要忽略
    public static final int COMMAND_HISTORY=8;//要求查询历史记录
    public static final int COMMAND_CONTROLD=9;//管理员强制下线
    public static final int COMMAND_CONTROLS=10;//管理员群发消息


    public static void main(String[] args) throws IOException {
        try {
            UIManager.setLookAndFeel( new FlatLightLaf() );
        } catch( Exception ex ) {
            System.err.println( "Failed to initialize LaF" );
        }
        Server();
    }

    //服务器变量
    static UserDAO dao = new UserDAO();
    private static HashSet<String> userList;
    private static Map<String,Socket> socketMap=new ConcurrentHashMap<>();//服务器中存储的socket表记录当前登录人员
    static int port =  10086;//端口号
    final static ExecutorService executorService = Executors.newFixedThreadPool(10);//线程池
    private static ServerSocket serverSocket;//服务器socket

    //持续监听端口，每当有新客户连接时开一个新通信线程并丢进线程池
    public static void Server() throws IOException {
        System.out.println(Charset.defaultCharset());
        new control();
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("服务端启动，运行在：" + serverSocket.getLocalSocketAddress());
            userList = dao.findAllUser();// 查询所有用户信息
            while (true) {
                // 监听客户端的连接，每来一个客户开一个新的通信线程
                Socket socket = serverSocket.accept();
                executorService.execute(new Server().new listener(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }

    //每个用户一个通信线程，负责持续接收用户发送的信息并处理
    class listener implements Runnable{
        Socket socket;//连接用户的socket
        String name;//用户的名字
        boolean flag=true;//是否保持在线
        listener(Socket s){socket=s;}
        @Override
        public void run() {
            try {
                //获取客户端输入流
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
                    //登录功能
                    if (cmd == COMMAND_LOGIN){
                        // 通过用户名查询用户信息匹配账号密码
                        String tmp_name = (String) jsonObj.get("user_name");
                        Map<String, String> user = dao.findById(tmp_name);
                        // 判断客户端发送过来的密码与数据库的密码是否一致
                        if (user != null && jsonObj.get("user_pwd").equals(user.get("user_pwd"))) {
                            name = tmp_name;
                            socketMap.put(name,this.socket);//socket表加入新用户

                            //进行登录
                            JSONObject sendJsonObj = new JSONObject(user);//创建结果json包
                            sendJsonObj.put("result", 1);// 添加result:0键值对，1表示成功，0表示失败
                            Set<String> online = socketMap.keySet();// 在线人员列表
                            sendJsonObj.put("online", online);
                            sendJsonObj.put("userList", userList);// 聊天室成员总名单
                            sendMessage(socket,sendJsonObj.toString());// 创建DatagramPacket对象，用于向客户端发送数据
                            //广播当前用户上线了
                            JSONObject broadcast = new JSONObject();
                            broadcast.put("command",1);
                            broadcast.put("name",name);
                            for(Socket socket:socketMap.values()){
                                sendMessage(socket,broadcast.toString());
                            }
                        }
                        else {
                            // 送失败消息
                            JSONObject sendJsonObj = new JSONObject();
                            sendJsonObj.put("result", 0);
                            sendMessage(socket,sendJsonObj.toString());
                        }
                    }
                    //注册功能
                    else if(cmd == COMMAND_Register){
                        name = (String) jsonObj.get("user_name");
                        String pwd=(String) jsonObj.get("pwd");
                        dao.insertUser(name,pwd);//插入新用户数据
                        userList = dao.findAllUser();// 更新总用户名单信息

                        //广播更新后的总成员表
                        JSONObject broadcast = new JSONObject();
                        broadcast.put("command",COMMAND_Register);
                        broadcast.put("userList",userList);
                        for(Socket socket:socketMap.values()){
                            sendMessage(socket,broadcast.toString());
                        }
                        //广播当前用户上线了
                        broadcast = new JSONObject();
                        broadcast.put("command",COMMAND_LOGIN);
                        broadcast.put("name",name);
                        for(Socket socket:socketMap.values()){
                            sendMessage(socket,broadcast.toString());
                        }
                        //进行登录
                        Map<String, String> user = dao.findById(name);
                        socketMap.put(name,this.socket);//socket表加入新用户
                        JSONObject sendJsonObj = new JSONObject(user);//创建结果json包
                        sendJsonObj.put("result", 1);// 添加result:0键值对，1表示成功，0表示失败
                        Set<String> online = socketMap.keySet();// 在线人员列表
                        sendJsonObj.put("online", online);
                        sendJsonObj.put("userList", userList);// 聊天室成员总名单
                        sendMessage(socket,sendJsonObj.toString());// 创建DatagramPacket对象，用于向客户端发送数据
                    }
                    //聊天室群发功能
                    else if(cmd == COMMAND_GROUP){
                        jsonObj.put("name",name);
                        //存储历史记录
                        Date date=new Date();
                        dao.insertMess(date,name,jsonObj.get("text").toString());
                        //群发消息
                        for(Socket socket:socketMap.values()){
                            sendMessage(socket,jsonObj.toString());
                        }

                    }
                    //私聊功能
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
                    //注销功能
                    else if(cmd == COMMAND_DROP){
                        //修改服务器表
                        socketMap.remove(name);
                        //广播该用户下线
                        JSONObject broadcast = new JSONObject();
                        broadcast.put("command",COMMAND_DROP);
                        broadcast.put("name",name);
                        for(Socket socket:socketMap.values()){
                            sendMessage(socket,broadcast.toString());
                        }
                        //关闭连接
                        flag = false;
                        socket.close();
                    }
                    //请求历史数据
                    else if(cmd == COMMAND_HISTORY){
                        String datestr=jsonObj.get("date").toString();
                        DateFormat dFormat = new SimpleDateFormat("yyyyMMdd");
                        Date date=dFormat.parse(datestr);
                        ArrayList<String>history=dao.queryHistory(date);
                        //封装结果json包返回给用户
                        JSONObject sendJsonObj = new JSONObject();
                        sendJsonObj.put("history",history);
                        sendJsonObj.put("command",COMMAND_HISTORY);
                        sendMessage(socket,sendJsonObj.toString());
                    }
                    //管理员强制下线
                    else if(cmd == COMMAND_CONTROLD){
                        String name=jsonObj.get("name").toString();
                        dropf(name);

                    }
                    //管理员群发消息
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
        // 获得当前屏幕的宽和高
        private double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        private double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        // 登录窗口宽和高
        private int frameWidth = 400;
        private int frameHeight = 300;

        public control(){
            Container c = getContentPane();

            JTextField user = new JTextField();
            user.setBounds(50,75,140,40);
            JButton drop = new JButton();
            drop.setBounds(50,150,140,40);
            drop.setText("强制下线");
            c.add(user);
            c.add(drop);

            JTextField text = new JTextField();
            text.setBounds(200,75,140,40);
            JButton send = new JButton();
            send.setBounds(200,150,140,40);
            send.setText("发送群体消息");
            c.add(text);
            c.add(send);




            // 设置窗口位于屏幕中心
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setSize(frameWidth, frameHeight);
            setTitle("控制界面");
            c.setLayout(null);
            setVisible(true);
            // 注册窗口事件
            addWindowListener(new WindowAdapter() {
                // 单击窗口关闭按钮时调用
                public void windowClosing(WindowEvent e) {
                    // 退出系统
                    System.exit(0);
                }
            });
            drop.addActionListener(e->{
                String name=user.getText().trim();
                dropf(name);
                //todo 下线功能
            });
            send.addActionListener(e->{
                String txt=text.getText().trim();
                sendf(txt);
            });

        }

    }
    static void dropf(String name){
        //遍历socket表找到用户名字并关闭它的socket
        for(Map.Entry<String,Socket>entry:socketMap.entrySet()){
            if(entry.getKey().equals(name)){
                try {
                    //发送强制下线命令
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("command", COMMAND_FROCE);
                    sendMessage(entry.getValue(),jsonObj.toString());
                    //关闭socket
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
        jsonObj.put("name","管理员");
        jsonObj.put("command",COMMAND_GROUP);
        jsonObj.put("picInfo","");
        String font="黑体|18|255-0-0|";
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

