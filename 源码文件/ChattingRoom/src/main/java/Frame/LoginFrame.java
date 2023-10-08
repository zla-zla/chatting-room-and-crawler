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
    // �������
    public static final int COMMAND_LOGIN=1;//���û���¼
    public static final int COMMAND_Register=2;//���û�ע��
    public static final int COMMAND_GROUP=3;//��������Ϣ
    public static final int COMMAND_SINGLE=4;//˽����Ϣ
    public static final int COMMAND_DROP=5;//�û�ע��
    public static Socket socket;
    // ��������IP
    public static String SERVER_IP;
    // �������˶˿ں�
    public static int SERVER_PORT = 10086;

    public static void main(String[] args) throws IOException {
        new LoginFrame();
    }
    // ��õ�ǰ��Ļ�Ŀ�͸�
    private double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    private double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    // ��¼���ڿ�͸�
    private int frameWidth = 335;
    private int frameHeight = 250;
    // QQ�����ı���
    private JTextField text_name = null;
    // QQ�����
    private JPasswordField text_pwd = null;

    public LoginFrame() throws IOException {
        SERVER_IP= InetAddress.getLocalHost().getHostAddress();
        socket = new Socket(SERVER_IP,SERVER_PORT);

        Container c=getContentPane();    //��ȡ��ǰ���ڵ����ݴ���
        //�м�����򲿷�
        c.add(getPaneLine());

        // ��ʼ����¼��ť
        JButton btnLogin = new JButton();
        btnLogin.setBounds(120, 181, 63, 19);
        btnLogin.setText("��¼");
        btnLogin.addActionListener(e -> {
            String name = text_name.getText().trim();
            String pwd = new String(text_pwd.getPassword()).trim();
            //�����п�
            if(name.equals("")||pwd.equals("")){
                JOptionPane.showMessageDialog(null, "����д����");
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
                JOptionPane.showMessageDialog(null, "��QQ��������벻��ȷ");
            }

        });

        // ��ʼ��ȡ����ť
        JButton btnCancel = new JButton();
        btnCancel.setBounds(233, 181, 63, 19);
        btnCancel.setText("ȡ��");
        btnCancel.addActionListener(e -> {
            System.exit(0);
        });

        // ��ʼ��ע�ᰴť
        JButton btnSetup = new JButton();
        btnSetup.setBounds(14, 181, 63, 19);
        btnSetup.setText("ע��");
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
        setTitle("��¼����");
        c.setLayout(null);
        // ���㴰��λ����Ļ���ĵ�����
        int x = (int) (screenWidth - frameWidth) / 2;
        int y = (int) (screenHeight - frameHeight) / 2;
        // ���ô���λ����Ļ����
        setLocation(x, y);
        setVisible(true);
        // ע�ᴰ���¼�
        addWindowListener(new WindowAdapter() {
            // �������ڹرհ�ťʱ����
            public void windowClosing(WindowEvent e) {
                // �˳�ϵͳ
                System.exit(0);
            }
        });

    }

    // �������
    private JPanel getPaneLine() {
        JPanel paneLine = new JPanel();
        paneLine.setLayout(null);
        paneLine.setBounds(7, 54, 308, 118);

        // ��ʼ����QQ���롿��ǩ
        JLabel lblUserPwd = new JLabel();
        lblUserPwd.setText("����");
        lblUserPwd.setFont(new Font("Dialog", Font.PLAIN, 12));
        lblUserPwd.setBounds(21, 48, 54, 18);
        paneLine.add(lblUserPwd);
        // ��ʼ����QQ���������ǩ
        JLabel lblUserId = new JLabel();
        lblUserId.setText("�˺�");
        lblUserId.setFont(new Font("Dialog", Font.PLAIN, 12));
        lblUserId.setBounds(21, 14, 55, 18);
        paneLine.add(lblUserId);
        // ��ʼ����QQ���롿�ı���
        this.text_name = new JTextField();
        this.text_name.setBounds(84, 14, 132, 18);
        paneLine.add(this.text_name);
        // ��ʼ����QQ���롿�����
        this.text_pwd = new JPasswordField();
        this.text_pwd.setBounds(84, 48, 132, 18);
        paneLine.add(this.text_pwd);

        return paneLine;
    }
    // ��¼����
    public static Map login(String name, String pwd) {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("command", COMMAND_LOGIN);
            jsonObj.put("user_name", name);
            jsonObj.put("user_pwd", pwd);
            // ���ַ�������json
            OutputStream out = socket.getOutputStream();
            PrintStream printStream = new PrintStream(out);
            printStream.println(jsonObj.toString());
            printStream.flush();
            // ���շ��ص�json���
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

