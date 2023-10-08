package Frame;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

public class RegisterFrame extends JFrame {
    public static final int COMMAND_LOGIN=1;//新用户登录
    public static final int COMMAND_Register=2;//新用户注册
    public static final int COMMAND_GROUP=3;//聊天室消息
    public static final int COMMAND_SINGLE=4;//私聊消息
    public static final int COMMAND_DROP=5;//用户注销

    // 获得当前屏幕的宽和高
    private double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    private double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    // 登录窗口宽和高
    private int frameWidth = 335;
    private int frameHeight = 250;
    // QQ号码文本框
    private JTextField txtUserId = null;
    // QQ密码框
    private JPasswordField txtUserPwd = null;

    Socket socket;

    public RegisterFrame(Socket s){
        socket=s;
        Container c=getContentPane();    //获取当前窗口的内容窗格

        //中间输入框部分
        c.add(getPaneLine());

        // 初始化注册按钮
        JButton btnCancel = new JButton();
        btnCancel.setBounds(120, 181, 63, 19);
        btnCancel.setText("注册");
        btnCancel.addActionListener(e -> {
            //TODO 注册操作
            String name = txtUserId.getText().trim();
            String pwd = new String(txtUserPwd.getPassword()).trim();
            //输入判空
            if(name.equals("")||pwd.equals("")){
                JOptionPane.showMessageDialog(null, "请填写完整");
                return;
            }
            Map user = register(name, pwd);
            if (user != null) {
                try {
                    new ChattingRoomFrame(user, socket);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(null, "遇到异常失败");
            }
        });

        // 初始化返回按钮
        JButton btnSetup = new JButton();
        btnSetup.setBounds(233, 181, 63, 19);
        btnSetup.setText("返回");
        btnSetup.addActionListener(e -> {
            try {
                new LoginFrame();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            this.dispose();
        });

        c.add(btnCancel);
        c.add(btnSetup);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(frameWidth, frameHeight);
        setTitle("注册界面");
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

    private Map register(String name, String pwd) {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("command", COMMAND_Register);
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
        this.txtUserId = new JTextField();
        this.txtUserId.setBounds(84, 14, 132, 18);
        paneLine.add(this.txtUserId);
        // 初始化【QQ密码】密码框
        this.txtUserPwd = new JPasswordField();
        this.txtUserPwd.setBounds(84, 48, 132, 18);
        paneLine.add(this.txtUserPwd);

        return paneLine;
    }

}
