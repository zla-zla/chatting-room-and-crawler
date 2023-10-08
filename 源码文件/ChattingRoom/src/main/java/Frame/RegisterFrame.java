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
    public static final int COMMAND_LOGIN=1;//���û���¼
    public static final int COMMAND_Register=2;//���û�ע��
    public static final int COMMAND_GROUP=3;//��������Ϣ
    public static final int COMMAND_SINGLE=4;//˽����Ϣ
    public static final int COMMAND_DROP=5;//�û�ע��

    // ��õ�ǰ��Ļ�Ŀ�͸�
    private double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    private double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    // ��¼���ڿ�͸�
    private int frameWidth = 335;
    private int frameHeight = 250;
    // QQ�����ı���
    private JTextField txtUserId = null;
    // QQ�����
    private JPasswordField txtUserPwd = null;

    Socket socket;

    public RegisterFrame(Socket s){
        socket=s;
        Container c=getContentPane();    //��ȡ��ǰ���ڵ����ݴ���

        //�м�����򲿷�
        c.add(getPaneLine());

        // ��ʼ��ע�ᰴť
        JButton btnCancel = new JButton();
        btnCancel.setBounds(120, 181, 63, 19);
        btnCancel.setText("ע��");
        btnCancel.addActionListener(e -> {
            //TODO ע�����
            String name = txtUserId.getText().trim();
            String pwd = new String(txtUserPwd.getPassword()).trim();
            //�����п�
            if(name.equals("")||pwd.equals("")){
                JOptionPane.showMessageDialog(null, "����д����");
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
                JOptionPane.showMessageDialog(null, "�����쳣ʧ��");
            }
        });

        // ��ʼ�����ذ�ť
        JButton btnSetup = new JButton();
        btnSetup.setBounds(233, 181, 63, 19);
        btnSetup.setText("����");
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
        setTitle("ע�����");
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

    private Map register(String name, String pwd) {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("command", COMMAND_Register);
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
        this.txtUserId = new JTextField();
        this.txtUserId.setBounds(84, 14, 132, 18);
        paneLine.add(this.txtUserId);
        // ��ʼ����QQ���롿�����
        this.txtUserPwd = new JPasswordField();
        this.txtUserPwd.setBounds(84, 48, 132, 18);
        paneLine.add(this.txtUserPwd);

        return paneLine;
    }

}
