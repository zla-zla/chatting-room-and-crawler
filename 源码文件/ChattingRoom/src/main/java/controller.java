
import Frame.ChattingRoomFrame;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Scanner;


public class controller {
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

    ChattingRoomFrame control;

    public static void main(String[] args) throws IOException {
        new controller();
    }

    controller() throws IOException {
        Socket socket = new Socket(InetAddress.getLocalHost().getHostAddress(),10086);
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("command", COMMAND_LOGIN);
        jsonObj.put("user_name", "管理员");
        jsonObj.put("user_pwd", "111");
        OutputStream out = socket.getOutputStream();
        PrintStream printStream = new PrintStream(out);
        printStream.println(jsonObj.toString());
        printStream.flush();
        // 接收返回的json结果
        InputStream in = socket.getInputStream();
        Scanner sc = new Scanner(in);
        String msg="";
        if(sc.hasNext()){
            msg = sc.nextLine();
        }
        JSONObject receivedjsonObj = new JSONObject(msg);
        System.out.println(receivedjsonObj);
        Map usermap = receivedjsonObj.toMap();
        control = new ChattingRoomFrame(usermap,socket);


        control.left.remove(2);
        Container c = new Container();

        JTextField user = new JTextField();
        user.setPreferredSize(new Dimension(140,40));
        JButton drop = new JButton();
        drop.setPreferredSize(new Dimension(140,40));
        drop.setText("强制下线");
        c.add(user);
        c.add(drop);

        JTextField text = new JTextField();
        text.setPreferredSize(new Dimension(140,40));
        JButton send = new JButton();
        send.setPreferredSize(new Dimension(140,40));
        send.setText("发送群体消息");
        c.add(text);
        c.add(send);

        c.setLayout(new FlowLayout());

        control.left.setBottomComponent(c);

        drop.addActionListener(e->{
            String name=user.getText().trim();
            JSONObject jObj = new JSONObject();
            jObj.put("command", COMMAND_CONTROLD);
            jObj.put("name",name);
            try {
                OutputStream ou = socket.getOutputStream();
                PrintStream print = new PrintStream(ou);
                print.println(jObj);
                print.flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        send.addActionListener(e->{
            String txt=text.getText().trim();
            JSONObject jObj = new JSONObject();
            jObj.put("command", COMMAND_CONTROLS);
            jObj.put("text",txt);
            try {
                OutputStream ou = socket.getOutputStream();
                PrintStream print = new PrintStream(ou);
                print.println(jObj);
                print.flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

}
