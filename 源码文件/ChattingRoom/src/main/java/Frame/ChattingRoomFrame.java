package Frame;


import org.json.JSONObject;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static javax.swing.Box.createVerticalBox;

//Ⱥ��������
public class ChattingRoomFrame extends JFrame {
    //������
    public static final int COMMAND_LOGIN=1;//���û���¼
    public static final int COMMAND_Register=2;//���û�ע��
    public static final int COMMAND_GROUP=3;//��������Ϣ
    public static final int COMMAND_SINGLE=4;//˽����Ϣ
    public static final int COMMAND_DROP=5;//�û�ע��
    public static final int COMMAND_FROCE=6;//�û�ע��
    public static final int COMMAND_REUSLT=7;//������ģ���Ҫ����
    public static final int COMMAND_HISTORY=8;//Ҫ���ѯ��ʷ��¼

    //������main�������������
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(InetAddress.getLocalHost().getHostAddress(),10086);
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("command", COMMAND_LOGIN);
        jsonObj.put("user_name", "����");
        jsonObj.put("user_pwd", "1");
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
        Map user = receivedjsonObj.toMap();
        new ChattingRoomFrame(user,socket);
    }

    //�����������
    int h=600;//���ڸ�
    int w=800;//���ڿ�
    String username;//��ǰ�û�������
    Socket socket;//��ǰ�û����ӵ�socket
    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");//���ڸ�ʽ
    FontAndText dateFont = new FontAndText("", "����", 20, Color.BLUE);//�̶��û���+���ڵ���Ϣͷ��ʽ
    int pos1;//ȫ�ִ洢��ǰ�������ʼλ�ã��������ʱ�Դ�Ϊ��׼
    FontAndText myFont = null;//ȫ�ִ洢�û�ѡ����ı���ʽ


    //������������
    List<String> userlist;//�û��ܳ�Ա�б��޸ĺ������ˣ�����ɾ���ˣ�
    Set<String> online;//������Ա����
    List<JLabel> userLabelList;//�û���ǩ�б�
    Map<String,SingleChatFrame>singlewindow=new HashMap<>();//����˽�Ĵ��ڣ�˽�Ķ���˽�Ĵ��ڣ�
    Map<String,ArrayList<String>>messBuffer=new HashMap<>();//��Ϣ���壨˽�Ķ��󣺷�����Ϣ���壩
    List<PicInfo> myPicInfo = new LinkedList<>();//�Լ��ı�����Ϣ
    List<PicInfo> receivePicInfo = new LinkedList<>();//���յ��ı�����Ϣ

    //��־
    boolean flag=true;//����

    //�ؼ�
    JSplitPane split = new JSplitPane();
    public JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    JScrollPane right = new JScrollPane();
    JPanel onlineUser = new JPanel();
    JLabel lbMenber = new JLabel("�����ҳ�Ա");

    //������
    JScrollPane chatScroll = new JScrollPane();//�����Ĺ����װ�
    JTextPane chatBoard=new JTextPane();//�����
    StyledDocument docChat = chatBoard.getStyledDocument();//������ͨ����ʽ���ĵ�


    //������
    public JPanel inputArea = new JPanel(new BorderLayout());//��������
    Box btnZone = Box.createHorizontalBox();//���飬��ʷ��¼������ģʽ�ȿ��ؿ�����
    JTextPane content = new JTextPane();//�û������
    StyledDocument docContent = content.getStyledDocument();//������ͨ����ʽ���ĵ�

    JButton emoji=new JButton("����");
    JLabel lbdsb=new JLabel("����ģʽ");
    JToggleButton disturb = new JToggleButton();
    JButton history = new JButton("��ʷ��Ϣ");


    String[] str_name = {"����", "����", "Dialog", "Gulim"};
    String[] str_Size = {"12", "14", "18", "22", "30", "40"};
    String[] str_Color = {"��ɫ", "��ɫ", "��ɫ", "��ɫ", "��ɫ"};
    Box down=Box.createHorizontalBox();;
    JComboBox fontName = new JComboBox(str_name);
    JComboBox fontSize = new JComboBox(str_Size);
    JComboBox fontColor = new JComboBox(str_Color);
    JButton submit = new JButton("����");
    
    Box menberListBox=createVerticalBox();//�ܳ�Ա�б����
    HistoryBoard hisbd=new HistoryBoard();//��ʷ��¼��
    PicsJWindow picWindow=new PicsJWindow(this);//�����



    public ChattingRoomFrame(Map user, Socket s) throws IOException {
        //������ֵ
        this.socket = s;
        this.username = (String) user.get("user_name");
        // ��ȡ�������ܳ�Ա
        this.userlist = (List<String>)user.get("userList");
        // ��ȡ���߳�Ա
        if(user.get("online")==null)this.online = new HashSet<>();//����ɾȥ
        else this.online = new HashSet<>((List<String>)user.get("online"));
        

        //ˮƽ�ָ��(80%��������20%�û��б�)
        split.setSize(w,h);
        split.setDividerLocation(0.8);
        split.setEnabled(false);

        //��߲��ִ�ֱ���֣�������70%��������30%��
        left.setSize((int)0.8*w,h);
        left.setDividerLocation(0.7);
        left.setEnabled(false);

        //������
        chatBoard.setEditable(false);//����������治�ɱ༭
        chatScroll.setViewportView(chatBoard);
        chatScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        left.setTopComponent(chatScroll);

        //������

        disturb.setBorderPainted(false);//���ò����ư�ť�߿�
        ImageIcon  image=new ImageIcon(ChattingRoomFrame.class.getResource("/icon/����-��.png"));
        image.setImage(image.getImage().getScaledInstance(20,10,Image.SCALE_DEFAULT));
        disturb.setSelectedIcon(image);
        image=new ImageIcon(ChattingRoomFrame.class.getResource("/icon/����-��.png"));
        image.setImage(image.getImage().getScaledInstance(20,10,Image.SCALE_DEFAULT));
        disturb.setIcon(image);

        btnZone.add(emoji);
        btnZone.add(history);
        btnZone.add(lbdsb);
        btnZone.add(disturb);

        inputArea.add(btnZone,BorderLayout.NORTH);
        inputArea.add(content,BorderLayout.CENTER);
        down.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        down.add(new JLabel("����:"));
        down.add(fontName);
        down.add(Box.createHorizontalStrut(3));
        down.add(new JLabel("�ֺ�:"));
        down.add(fontSize);
        down.add(Box.createHorizontalStrut(3));
        down.add(new JLabel("��ɫ:"));
        down.add(fontColor);
        down.add(Box.createHorizontalStrut(3));
        down.add(submit);

        inputArea.add(down,BorderLayout.SOUTH);

        left.setBottomComponent(inputArea);

        //�����ҳ�Ա�������
        right.setViewportView(onlineUser);
        onlineUser.setLayout(new BorderLayout(0, 0));

        lbMenber.setHorizontalAlignment(SwingConstants.CENTER);
        onlineUser.add(lbMenber, BorderLayout.NORTH);

        //��ʼ�������ҳ�Ա�б�
        userLabelList = new ArrayList<JLabel>();
        for (String us:online) {
            JLabel lbu = new JLabel(us);
            lbu.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // �û�ͼ��˫�����ʱ��ʾ�Ի���
                    if (e.getClickCount() == 2) {
                        singlewindow.put(us,new SingleChatFrame(us));
                        lbu.setForeground(Color.black);
                    }

                }
            });
            userLabelList.add(lbu);
            menberListBox.add(lbu);
        }
        onlineUser.add(menberListBox);

        //��������
        split.setLeftComponent(left);
        split.setRightComponent(right);
        this.add(split);

        //�󶨸��ּ����¼�

        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                ChattingRoomFrame.this.picWindow.dispose();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                ChattingRoomFrame.this.picWindow.dispose();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                ChattingRoomFrame.this.picWindow.dispose();
            }

        });
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println(555);
                picWindow.setVisible(false);
            }
        });
        //�رմ����¼�
        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                try {
                    logout();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        //������Ϣ�¼�
        //��ݼ�������Ϣ
        content.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    submit.doClick();
                }
            }
        });
        submit.addActionListener(l-> {
            if ("".equals(content.getText())) { //������
                JOptionPane.showMessageDialog(ChattingRoomFrame.this, "���ܷ��Ϳ���Ϣ!",
                        "���ܷ���", JOptionPane.ERROR_MESSAGE);
            } else {
                System.out.println(content.getText());
                System.out.println(getFontAttrib().toString());
                try {
                    //������Ⱥ����Ϣ
                    JSONObject jsonObj = new JSONObject();
                    myFont = getFontAttrib();//�ı���ʽ
                    jsonObj.put("font",myFont.toString());
                    String info = buildPicInfo();
                    jsonObj.put("picInfo",info);
                    jsonObj.put("command", COMMAND_GROUP);
                    jsonObj.put("text",content.getText());

                    OutputStream out = socket.getOutputStream();
                    PrintStream printStream = new PrintStream(out);
                    printStream.println(jsonObj);
                    printStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                content.setText("");
                content.requestFocus();
            }
        });
        emoji.addActionListener(l-> {
            picWindow.setVisible(true);
        });
        history.addActionListener(l-> {
            hisbd.setVisible(true);
        });

        //���̳߳�������������Ϣ����ʾ��chatBoard
        new Thread(()->{
            while (flag){
                try {
                    InputStream in = socket.getInputStream();
                    Scanner sc = new Scanner(in);
                    String msg="";
                    if(sc.hasNext()){
                        msg = sc.nextLine();
                    }
                    else break;
                    System.out.println(msg);
                    JSONObject receivedjsonObj = new JSONObject(msg);
                    Integer mes = (Integer)receivedjsonObj.get("command");
                    if(mes==COMMAND_LOGIN){
                        String name = (String) receivedjsonObj.get("name");
                        online.add(name);
                        refreshFriendList(name,"1");
                    }
                    //���³�Աע�ᣬ�����ܳ�Ա��
                    if(mes==COMMAND_Register){
//                        String name = (String) receivedjsonObj.get("name");
//                        online.add(name);
//                        this.userlist = (List<String>)receivedjsonObj.toMap().get("userList");
//                        genMenberList();
                    }
                    else if(mes==COMMAND_GROUP){
                        String name = (String) receivedjsonObj.get("name");
                        String text = (String) receivedjsonObj.get("text");
                        String picInfo=(String) receivedjsonObj.get("picInfo");
                        String font = (String) receivedjsonObj.get("font");
                        addRecMsg(name,text,picInfo,font);
                    }
                    else if(mes==COMMAND_SINGLE){
                        //�ȼ��singlewindow�Ƿ��Ѿ����˴��ڣ�������ˣ����Ƹô��������Ϣ
                        String name=(String) receivedjsonObj.get("name");//name�Ƿ��ͷ�������
                        String text=(String) receivedjsonObj.get("text");//text�ǶԷ����͵�����
                        if(singlewindow.containsKey(name))
                            singlewindow.get(name).addMes(name,text);
                        //�����û�򿪴��ڣ��Ȼ��棬�����û����
                        else{
                            if(!messBuffer.containsKey(name))
                                messBuffer.put(name,new ArrayList<>());
                            messBuffer.get(name).add(text);
                            for (JLabel ulbl : userLabelList)
                                if (name.equals(ulbl.getText()))
                                    ulbl.setForeground(Color.red);
                            //����ر�����ģʽ��ֱ�ӵ�������
                            if(!disturb.isSelected())
                                for(JLabel lbu:userLabelList)
                                    if(lbu.getText().equals(name)){
                                        singlewindow.put(name,new SingleChatFrame(name));
                                        lbu.setForeground(Color.black);
                                    }
                        }
                    }
                    else if(mes==COMMAND_REUSLT){
                        String target=(String) receivedjsonObj.get("target");
                        singlewindow.get(target).response(receivedjsonObj);
                    }
                    // ���û�����
                    else if(mes==COMMAND_DROP){
                        String name = (String) receivedjsonObj.get("name");
                        online.remove(name);
                        refreshFriendList(name,"0");
                    }
                    // ��ǿ������
                    else if(mes==COMMAND_FROCE){
                        socket.shutdownOutput();
                        socket.shutdownInput();
                        socket.close();
                        JOptionPane.showMessageDialog(ChattingRoomFrame.this, "���ѱ�ǿ������!",
                                "���ܷ���", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    else if(mes==COMMAND_HISTORY){
                        Map map=receivedjsonObj.toMap();
                        List<String> historytext=(List<String>) map.get("history");
                        hisbd.addHistory(historytext);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();

        //���ô��ڲ���
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("�û���"+username);
        setBounds(300,150,w,h);
        setVisible(true);
    }

    public void addRecMsg(String uname, String message,String picInfo,String font) {
        setExtendedState(Frame.NORMAL);
        //setVisible(true);
        String msg = uname + " " + sf.format(new Date());
        dateFont.setText(msg);//�û���Ϣ��ʱ��
        insert(dateFont);
        pos1 = chatBoard.getCaretPosition();//��¼�������ʼ��
        if (!picInfo.equals("")) {/*���ڱ�����Ϣ*/
            FontAndText attr = getReceiveFont(font);
            insert(attr);
            receivedPicInfo(picInfo);
            insertPics();
        } else {
            FontAndText attr = getReceiveFont(font);
            insert(attr);
        }
    }

    //��������в������
    private void insertPics() {
        if (this.receivePicInfo.size() <= 0) {
            return;
        } else {
            for (int i = 0; i < receivePicInfo.size(); i++) {
                PicInfo pic = receivePicInfo.get(i);
                String fileName;
                chatBoard.setCaretPosition(pos1 + pic.getPos()); /*���ò���λ��*/
                fileName = "/qqdefaultface/" + pic.getVal() + ".gif";/*�޸�ͼƬ·��*/
                chatBoard.insertIcon(new ImageIcon(PicsJWindow.class.getResource(fileName))); /*����ͼƬ*/
            }
            receivePicInfo.clear();
        }
        chatBoard.setCaretPosition(docChat.getLength()); /*���ù��������±�*/
    }

    //����������ͼƬ
    public void insertSendPic(ImageIcon imgIc) {
        content.insertIcon(imgIc); // ����ͼƬ
    }

    /**
     * ���յ�����Ϣת��Ϊ�Զ�������������
     */
    public FontAndText getReceiveFont(String message) {
        String[] msgs = message.split("[|]");
        String fontName = "";
        int fontSize = 0;
        String[] color;
        String text = message;
        Color fontC = new Color(222, 222, 222);
        if (msgs.length >= 4) {/*����򵥴�����ʾ����������Ϣ*/
            fontName = msgs[0];
            fontSize = Integer.parseInt(msgs[1]);
            color = msgs[2].split("[-]");
            if (color.length == 3) {
                int r = Integer.parseInt(color[0]);
                int g = Integer.parseInt(color[1]);
                int b = Integer.parseInt(color[2]);
                fontC = new Color(r, g, b);
            }
            text = "";
            for (int i = 3; i < msgs.length; i++) {
                text = text + msgs[i];
            }
        }
        FontAndText attr = new FontAndText();

        attr.setName(fontName);
        attr.setSize(fontSize);
        attr.setColor(fontC);

        attr.setText(text);

        System.out.println("getRecivedFont(String message):" + attr.toString());
        return attr;
    }

    //���������Ϣ����������Ϣ��  ��ʽΪ   λ��&����+λ��&����+����
    private String buildPicInfo() {
        StringBuilder sb = new StringBuilder("");
        //����jtextpane�ҳ����е�ͼƬ��Ϣ��װ��ָ����ʽ
        for (int i = 0; i < this.content.getText().length(); i++) {
            if (docContent.getCharacterElement(i).getName().equals("icon")) {
                Icon icon = StyleConstants.getIcon(content.getStyledDocument().getCharacterElement(i).getAttributes());
                ChatPic cupic = (ChatPic) icon;
                PicInfo picInfo = new PicInfo(i, cupic.getIm() + "");
                myPicInfo.add(picInfo);
                sb.append(i).append("&").append(cupic.getIm()).append("+");
            }
        }
        System.out.println(sb.toString());
        return sb.toString();
    }


    //�������Ĳ����ı�
    private void insert(FontAndText attrib) {
        try { // �����ı�
            docChat.insertString(docChat.getLength(), attrib.getText() + "\n",
                    attrib.getAttrSet());
            System.out.println("123"+attrib.toString());
            System.out.println(attrib.getText());
            chatBoard.setCaretPosition(docChat.getLength()); // ���ù��������±�
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    //��ȡ�������ֵĸ�ʽ
    private FontAndText getFontAttrib() {
        FontAndText att = new FontAndText();
        att.setText(content.getText());//�ı���Ϣ
        att.setName((String) fontName.getSelectedItem());
        att.setSize(Integer.parseInt((String) fontSize.getSelectedItem()));
        String temp_color = (String) fontColor.getSelectedItem();
        if (temp_color.equals("��ɫ")) {
            att.setColor(new Color(0, 0, 0));
        } else if (temp_color.equals("��ɫ")) {
            att.setColor(new Color(255, 0, 0));
        } else if (temp_color.equals("��ɫ")) {
            att.setColor(new Color(0, 0, 255));
        } else if (temp_color.equals("��ɫ")) {
            att.setColor(new Color(255, 255, 0));
        } else if (temp_color.equals("��ɫ")) {
            att.setColor(new Color(0, 255, 0));
        }
        return att;
    }

    //�����յ��ı�����Ϣ��
    public void receivedPicInfo(String picInfos) {
        String[] infos = picInfos.split("[+]");
        for (int i = 0; i < infos.length; i++) {
            String[] tem = infos[i].split("[&]");
            if (tem.length == 2) {
                PicInfo pic = new PicInfo(Integer.parseInt(tem[0]), tem[1]);
                receivePicInfo.add(pic);
            }
        }
    }


    //���ر��鰴ť���Ա��ȡλ��
    JButton getEmoji(){
        return emoji;
    }
    //�ػ��û����
    void genMenberList(){
        menberListBox.removeAll();
        menberListBox.repaint();
        menberListBox.invalidate();
        userLabelList = new ArrayList<JLabel>();
        for (String us:online) {
            JLabel lbu = new JLabel(us);
            lbu.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // �û�ͼ��˫�����ʱ��ʾ�Ի���
                    if (e.getClickCount() == 2) {
                        singlewindow.put(us,new SingleChatFrame(us));
                        lbu.setForeground(Color.black);
                    }
                }
            });
            userLabelList.add(lbu);
            menberListBox.add(lbu);
        }
        menberListBox.validate();
        menberListBox.updateUI();
    }
    //������ˢ��
    public void refreshFriendList(String userName, String flag) {
        // ���������ҳ�Ա�б�
        if (flag.equals("1")) {
            online.add(userName);
            genMenberList();
        }
        else{
            online.remove(userName);
            genMenberList();
        }
    }
    //�رմ����¼�
    private void logout() throws IOException {
        int select = JOptionPane.showConfirmDialog(null,
                "ȷ���˳���\n\n�˳������ж��������������!", "�˳�������",
                JOptionPane.YES_NO_OPTION);
        if (select == JOptionPane.YES_OPTION) {
            //������������˳���������Ϣ
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("command", COMMAND_DROP);//ע������

            OutputStream out = socket.getOutputStream();
            PrintStream printStream = new PrintStream(out);
            printStream.println(jsonObj);
            printStream.flush();

            //�رյ�ǰsocket
            socket.close();
            flag=false;
        }else{
            this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        }
    }


    //˽�Ĵ���
    class SingleChatFrame extends JFrame {
        JTextArea chatBoard;
        String name;//˽�Ĵ��ڵ�name�ǶԷ���target������
        public SingleChatFrame(String n){
            name=n;
            JSplitPane panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            panel.setDividerLocation(0.7);

            //������
            chatBoard = new JTextArea();
            chatBoard.setLineWrap(true);
            chatBoard.setEditable(false);
            panel.setTopComponent(chatBoard);

            //������
            JSplitPane inputArea = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            inputArea.setDividerLocation(0.8);
            JTextArea content = new JTextArea();
            inputArea.setTopComponent(content);
            JButton submit = new JButton("����");
            inputArea.setBottomComponent(submit);
            panel.setBottomComponent(inputArea);

            getContentPane().add(panel);

            this.addComponentListener(new ComponentAdapter(){
                public void componentResized(ComponentEvent e) {
                    panel.setDividerLocation(0.7);
                    inputArea.setDividerLocation(0.8);
                }
            });
            //������Ϣ�¼�
            submit.addActionListener(l-> {
                if ("".equals(content.getText())) { //������
                    JOptionPane.showMessageDialog(null, "���ܷ��Ϳ���Ϣ!",
                            "���ܷ���", JOptionPane.ERROR_MESSAGE);
                } else {
                    //todo ������Ϣ
                    //˽����Ϣ
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("command", COMMAND_SINGLE);
                    jsonObj.put("target",name);
                    jsonObj.put("text",content.getText());
                    OutputStream out = null;
                    try {
                        out = socket.getOutputStream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    PrintStream printStream = new PrintStream(out);
                    printStream.println(jsonObj);
                    printStream.flush();
                    content.setText("");
                    content.requestFocus();
                    // ���շ��ص�json���
                }
            });

            //���ô��ڲ���
            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            this.setSize(600, 500);
            this.setTitle("���ں�"+name+"����");
            int x = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
            int y = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
            this.setLocation((x - this.getWidth()) / 2, (y-this.getHeight())/ 2);
            this.setVisible(true);

            //�ж��Ƿ�����Ϣ���棬�������ȡ����ɾ������
            if(messBuffer.containsKey(name)){
                ArrayList<String>buffer=messBuffer.get(name);
                for(String s:buffer)addMes(name,s);
                messBuffer.remove(name);
            }
            this.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    singlewindow.remove(name);
                }
            });

        }

        public void response(JSONObject receivedjsonObj){
            if ((Integer) receivedjsonObj.get("result") != 1) {
                JOptionPane.showMessageDialog(null, "����ʧ�ܣ��Է���δ����!",
                        "����ʧ��", JOptionPane.ERROR_MESSAGE);
            }
            else{
                chatBoard.append(receivedjsonObj.get("name")+":\n"+receivedjsonObj.get("text")+'\n');
            }
        }
        public void addMes(String opname,String text){
            chatBoard.append(opname+":\n"+text+'\n');
        }
        //�رմ���ʱҪ��singlewindow��ɾ���Լ�


    }
    //��ʷ��Ϣ���
    class HistoryBoard extends JFrame{
        private JTextArea chatBoard;
        public HistoryBoard(){
            BorderLayout bor = new BorderLayout();
            setLayout(bor);

            //��ʷ��Ϣ���
            JScrollPane historyBoard = new JScrollPane();
            chatBoard = new JTextArea();
            chatBoard.setLineWrap(true);//�����Զ����й���
            chatBoard.setWrapStyleWord(true);// ������в����ֹ���
            chatBoard.setEditable(false);//����������治�ɱ༭
            historyBoard.setViewportView(chatBoard);
            historyBoard.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            this.add(historyBoard,BorderLayout.CENTER);

            //�·�������
            JPanel down=new JPanel();
            down.setLayout(new BorderLayout());
            JTextField dateInput = new JTextField();
            JButton submit = new JButton();
            submit.setText("��ѯ");
            down.add(dateInput,BorderLayout.CENTER);
            down.add(submit,BorderLayout.EAST);
            this.add(down,BorderLayout.SOUTH);


            submit.addActionListener(e->{
                String date=dateInput.getText().trim();
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("command", COMMAND_HISTORY);
                jsonObj.put("date", date);
                OutputStream out = null;
                try {
                    out = socket.getOutputStream();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                PrintStream printStream = new PrintStream(out);
                printStream.println(jsonObj);
                printStream.flush();
            });

            setTitle("��ʷ��¼��ѯ");
            setSize(400,600);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        }

        public void addHistory(List<String> list){
            chatBoard.setText("");
            for(String s:list){
                String time = s.split("\t")[0];
                String name = s.split("\t")[1];
                String text = s.split("\t")[2];
                chatBoard.append(name+"("+time+")"+"\n"+text+"\n");
            }
        }

    }
}
//�Զ���ͼ���࣬���ڱ�ǩ����Ҫ�ǼӸ����޸ĵĴ���
class ChatPic extends ImageIcon{
    int im;//ͼƬ����
    public int getIm() {
        return im;
    }
    public void setIm(int im) {
        this.im = im;
    }
    public ChatPic(URL url, int im){
        super(url);
        this.im = im;
    }
}
//�Զ���JWindow������ѡ������
class PicsJWindow extends JWindow {
    private static final long serialVersionUID = 1L;
    public static final String FACE_IMAGE_DIR = "/qqdefaultface/";//����·��
    public static final String GIF_SUB = ".gif";//�ļ���׺
    GridLayout gridLayout1 = new GridLayout(7, 15);//7*15�����������е�105������
    JLabel[] ico = new JLabel[105]; //�����ǩ����
    int i;
    ChattingRoomFrame owner;

    public PicsJWindow(ChattingRoomFrame owner) {
        super(owner);
        this.owner = owner;
        try {
            init();
            this.setAlwaysOnTop(true);//ʼ�������������ʾ����
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    //��qqdefaultface�ļ��еı���ȫ�������������
    private void init() throws Exception {
        this.setPreferredSize(new Dimension(28 * 15, 28 * 7));
        JPanel p = new JPanel();
        p.setOpaque(true);//������߽��ڵ��������أ������������͸��
        this.setContentPane(p);//��p��Ϊ��JWindow�������������
        p.setLayout(gridLayout1);
        p.setBackground(SystemColor.text);
        String fileName = "";//��ǩ���ļ���
        for (i = 0; i < ico.length; i++) {
            fileName = FACE_IMAGE_DIR + i + GIF_SUB;//�ļ�·������·��+���+��׺
            ico[i] = new JLabel(new ChatPic(ChattingRoomFrame.class.getResource(fileName), i), SwingConstants.CENTER);
            ico[i].setBorder(BorderFactory.createLineBorder(new Color(225, 225, 225), 1));
            ico[i].setToolTipText(i + "");//�ڿؼ�����ʾ��ʾ��Ϣ
            ico[i].addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == 1) {
                        JLabel cubl = (JLabel) (e.getSource());
                        ChatPic cupic = (ChatPic) (cubl.getIcon());
                        owner.insertSendPic(cupic);
                        cubl.setBorder(BorderFactory.createLineBorder(new Color(225, 225, 225), 1));
                        getObj().dispose();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    ((JLabel) e.getSource()).setBorder(BorderFactory.createLineBorder(Color.BLUE));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    ((JLabel) e.getSource()).setBorder(BorderFactory.createLineBorder(new Color(225, 225, 225), 1));
                }

            });
            p.add(ico[i]);
        }
        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                getObj().dispose();
            }
        });
    }

    @Override
    public void setVisible(boolean show) {
        if (show) {
            System.out.println("չʾ");
            determineAndSetLocation();
        }
        else System.out.println("����");
        super.setVisible(show);
    }

    private void determineAndSetLocation() {
        Point loc = owner.getEmoji().getLocationOnScreen();/*�ؼ��������Ļ��λ��*/
        setBounds(loc.x - getPreferredSize().width / 3, loc.y - getPreferredSize().height,
                getPreferredSize().width, getPreferredSize().height);
    }

    private JWindow getObj() {
        return this;
    }

}
//������ı���
class FontAndText {
    String msg = "", name = "����"; // Ҫ������ı�����������
    int size = 0; //�ֺ�
    Color color = new Color(225, 225, 225); // ������ɫ
    SimpleAttributeSet attrSet = null; // ���Լ�
    public FontAndText() {
    }
    public FontAndText(String msg, String fontName, int fontSize, Color color) {
        this.msg = msg;
        this.name = fontName;
        this.size = fontSize;
        this.color = color;
    }
    public SimpleAttributeSet getAttrSet() {
        attrSet = new SimpleAttributeSet();
        if (name != null) {
            StyleConstants.setFontFamily(attrSet, name);
        }//��������
        StyleConstants.setBold(attrSet, false);//�Ӵ�
        StyleConstants.setItalic(attrSet, false);//б��
        StyleConstants.setFontSize(attrSet, size);//��С
        if (color != null)
            StyleConstants.setForeground(attrSet, color);//��ɫ
        return attrSet;
    }

    public String toString() {
        //����Ϣ��Ϊ�Ŀ�����������ϴ���
        return name + "|"
                + size + "|"
                + color.getRed() + "-" + color.getGreen() + "-" + color.getBlue() + "|"
                + msg;
    }

    public String getText() {
        return msg;
    }

    public void setText(String text) {
        this.msg = text;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
//ͼƬ��Ϣ��λ�úͱ�ţ�
class PicInfo {
    int pos;
    String val;
    public PicInfo(int pos, String val) {
        this.pos = pos;
        this.val = val;
    }
    public int getPos() {
        return pos;
    }
    public String getVal() {
        return val;
    }

}