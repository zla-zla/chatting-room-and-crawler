package Frame;

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
import org.json.JSONObject;


public class LoginFrame extends JFrame {
    // 命令代码
    public static final int COMMAND_LOGIN=1;//新用户登录
    public static final int COMMAND_Register=2;//新用户注册
    public static final int COMMAND_GROUP=3;//聊天室消息
    public static final int COMMAND_SINGLE=4;//私聊消息
    public static final int COMMAND_DROP=5;//用户注销
    public static Socket socket;
    // 服务器端IP
    public static String SERVER_IP;
    // 服务器端端口号
    public static int SERVER_PORT = 10086;

    public static void main(String[] args) throws IOException {
        new LoginFrame();
    }
    // 获得当前屏幕的宽和高
    private double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    private double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    // 登录窗口宽和高
    private int frameWidth = 335;
    private int frameHeight = 250;
    // QQ号码文本框
    private JTextField text_name = null;
    // QQ密码框
    private JPasswordField text_pwd = null;

    public LoginFrame() throws IOException {
        SERVER_IP= InetAddress.getLocalHost().getHostAddress();
        socket = new Socket(SERVER_IP,SERVER_PORT);

        Container c=getContentPane();    //获取当前窗口的内容窗格
        //中间输入框部分
        c.add(getPaneLine());

        // 初始化登录按钮
        JButton btnLogin = new JButton();
        btnLogin.setBounds(120, 181, 63, 19);
        btnLogin.setText("登录");
        btnLogin.addActionListener(e -> {
            String name = text_name.getText().trim();
            String pwd = new String(text_pwd.getPassword()).trim();
            //输入判空
            if(name.equals("")||pwd.equals("")){
                JOptionPane.showMessageDialog(null, "请填写完整");
                return;
            }
            Map user = login(name, pwd);
            if (user != null) {
                try {
                    new ChattingRoomFrame(user, socket);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(null, "您QQ号码或密码不正确");
            }

        });

        // 初始化取消按钮
        JButton btnCancel = new JButton();
        btnCancel.setBounds(233, 181, 63, 19);
        btnCancel.setText("取消");
        btnCancel.addActionListener(e -> {
            System.exit(0);
        });

        // 初始化注册按钮
        JButton btnSetup = new JButton();
        btnSetup.setBounds(14, 181, 63, 19);
        btnSetup.setText("注册");
        btnSetup.addActionListener(e -> {
            new RegisterFrame(socket);
            this.dispose();
        });

        c.add(btnLogin);
        c.add(btnCancel);
        c.add(btnSetup);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(frameWidth, frameHeight);
        setTitle("登录界面");
        c.setLayout(null);
        // 计算窗口位于屏幕中心的坐标
        int x = (int) (screenWidth - frameWidth) / 2;
        int y = (int) (screenHeight - frameHeight) / 2;
        // 设置窗口位于屏幕中心
        setLocation(x, y);
        setVisible(true);
        // 注册窗口事件
        addWindowListener(new WindowAdapter() {
            // 单击窗口关闭按钮时调用
            public void windowClosing(WindowEvent e) {
                // 退出系统
                System.exit(0);
            }
        });

    }

    // 蓝线面板
    private JPanel getPaneLine() {
        JPanel paneLine = new JPanel();
        paneLine.setLayout(null);
        paneLine.setBounds(7, 54, 308, 118);

        // 初始化【QQ密码】标签
        JLabel lblUserPwd = new JLabel();
        lblUserPwd.setText("密码");
        lblUserPwd.setFont(new Font("Dialog", Font.PLAIN, 12));
        lblUserPwd.setBounds(21, 48, 54, 18);
        paneLine.add(lblUserPwd);
        // 初始化【QQ号码↓】标签
        JLabel lblUserId = new JLabel();
        lblUserId.setText("账号");
        lblUserId.setFont(new Font("Dialog", Font.PLAIN, 12));
        lblUserId.setBounds(21, 14, 55, 18);
        paneLine.add(lblUserId);
        // 初始化【QQ号码】文本框
        this.text_name = new JTextField();
        this.text_name.setBounds(84, 14, 132, 18);
        paneLine.add(this.text_name);
        // 初始化【QQ密码】密码框
        this.text_pwd = new JPasswordField();
        this.text_pwd.setBounds(84, 48, 132, 18);
        paneLine.add(this.text_pwd);

        return paneLine;
    }
    // 登录功能
    public static Map login(String name, String pwd) {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("command", COMMAND_LOGIN);
            jsonObj.put("user_name", name);
            jsonObj.put("user_pwd", pwd);
            // 以字符串传输json
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
            if ((Integer) receivedjsonObj.get("result") == 1) {
                Map user = receivedjsonObj.toMap();
                return user;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

