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

//群聊聊天室
public class ChattingRoomFrame extends JFrame {
    //命令区
    public static final int COMMAND_LOGIN=1;//新用户登录
    public static final int COMMAND_Register=2;//新用户注册
    public static final int COMMAND_GROUP=3;//聊天室消息
    public static final int COMMAND_SINGLE=4;//私聊消息
    public static final int COMMAND_DROP=5;//用户注销
    public static final int COMMAND_FROCE=6;//用户注销
    public static final int COMMAND_REUSLT=7;//结果报文，需要忽略
    public static final int COMMAND_HISTORY=8;//要求查询历史记录

    //测试用main函数，不用理会
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(InetAddress.getLocalHost().getHostAddress(),10086);
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("command", COMMAND_LOGIN);
        jsonObj.put("user_name", "张三");
        jsonObj.put("user_pwd", "1");
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
        Map user = receivedjsonObj.toMap();
        new ChattingRoomFrame(user,socket);
    }

    //运行所需变量
    int h=600;//窗口高
    int w=800;//窗口宽
    String username;//当前用户的名字
    Socket socket;//当前用户连接的socket
    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");//日期格式
    FontAndText dateFont = new FontAndText("", "宋体", 20, Color.BLUE);//固定用户名+日期的信息头样式
    int pos1;//全局存储当前插入的起始位置，插入表情时以此为基准
    FontAndText myFont = null;//全局存储用户选择的文本样式


    //运行所需容器
    List<String> userlist;//用户总成员列表（修改后不再用了，可以删掉了）
    Set<String> online;//在线人员名单
    List<JLabel> userLabelList;//用户标签列表
    Map<String,SingleChatFrame>singlewindow=new HashMap<>();//管理私聊窗口（私聊对象：私聊窗口）
    Map<String,ArrayList<String>>messBuffer=new HashMap<>();//消息缓冲（私聊对象：发送信息缓冲）
    List<PicInfo> myPicInfo = new LinkedList<>();//自己的表情信息
    List<PicInfo> receivePicInfo = new LinkedList<>();//接收到的表情信息

    //标志
    boolean flag=true;//在线

    //控件
    JSplitPane split = new JSplitPane();
    public JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    JScrollPane right = new JScrollPane();
    JPanel onlineUser = new JPanel();
    JLabel lbMenber = new JLabel("聊天室成员");

    //聊天区
    JScrollPane chatScroll = new JScrollPane();//聊天板的滚动底板
    JTextPane chatBoard=new JTextPane();//聊天板
    StyledDocument docChat = chatBoard.getStyledDocument();//聊天板的通用样式化文档


    //输入区
    public JPanel inputArea = new JPanel(new BorderLayout());//输入区域
    Box btnZone = Box.createHorizontalBox();//表情，历史记录，勿扰模式等开关控制区
    JTextPane content = new JTextPane();//用户输入框
    StyledDocument docContent = content.getStyledDocument();//输入框的通用样式化文档

    JButton emoji=new JButton("表情");
    JLabel lbdsb=new JLabel("勿扰模式");
    JToggleButton disturb = new JToggleButton();
    JButton history = new JButton("历史消息");


    String[] str_name = {"宋体", "黑体", "Dialog", "Gulim"};
    String[] str_Size = {"12", "14", "18", "22", "30", "40"};
    String[] str_Color = {"黑色", "红色", "蓝色", "黄色", "绿色"};
    Box down=Box.createHorizontalBox();;
    JComboBox fontName = new JComboBox(str_name);
    JComboBox fontSize = new JComboBox(str_Size);
    JComboBox fontColor = new JComboBox(str_Color);
    JButton submit = new JButton("发送");
    
    Box menberListBox=createVerticalBox();//总成员列表面板
    HistoryBoard hisbd=new HistoryBoard();//历史记录板
    PicsJWindow picWindow=new PicsJWindow(this);//表情框



    public ChattingRoomFrame(Map user, Socket s) throws IOException {
        //变量赋值
        this.socket = s;
        this.username = (String) user.get("user_name");
        // 获取聊天室总成员
        this.userlist = (List<String>)user.get("userList");
        // 获取在线成员
        if(user.get("online")==null)this.online = new HashSet<>();//可以删去
        else this.online = new HashSet<>((List<String>)user.get("online"));
        

        //水平分割窗口(80%聊天区，20%用户列表)
        split.setSize(w,h);
        split.setDividerLocation(0.8);
        split.setEnabled(false);

        //左边部分垂直划分（聊天区70%，输入区30%）
        left.setSize((int)0.8*w,h);
        left.setDividerLocation(0.7);
        left.setEnabled(false);

        //聊天区
        chatBoard.setEditable(false);//设置聊天版面不可编辑
        chatScroll.setViewportView(chatBoard);
        chatScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        left.setTopComponent(chatScroll);

        //输入区

        disturb.setBorderPainted(false);//设置不绘制按钮边框
        ImageIcon  image=new ImageIcon(ChattingRoomFrame.class.getResource("/icon/开关-开.png"));
        image.setImage(image.getImage().getScaledInstance(20,10,Image.SCALE_DEFAULT));
        disturb.setSelectedIcon(image);
        image=new ImageIcon(ChattingRoomFrame.class.getResource("/icon/开关-关.png"));
        image.setImage(image.getImage().getScaledInstance(20,10,Image.SCALE_DEFAULT));
        disturb.setIcon(image);

        btnZone.add(emoji);
        btnZone.add(history);
        btnZone.add(lbdsb);
        btnZone.add(disturb);

        inputArea.add(btnZone,BorderLayout.NORTH);
        inputArea.add(content,BorderLayout.CENTER);
        down.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        down.add(new JLabel("字体:"));
        down.add(fontName);
        down.add(Box.createHorizontalStrut(3));
        down.add(new JLabel("字号:"));
        down.add(fontSize);
        down.add(Box.createHorizontalStrut(3));
        down.add(new JLabel("颜色:"));
        down.add(fontColor);
        down.add(Box.createHorizontalStrut(3));
        down.add(submit);

        inputArea.add(down,BorderLayout.SOUTH);

        left.setBottomComponent(inputArea);

        //聊天室成员滚动面板
        right.setViewportView(onlineUser);
        onlineUser.setLayout(new BorderLayout(0, 0));

        lbMenber.setHorizontalAlignment(SwingConstants.CENTER);
        onlineUser.add(lbMenber, BorderLayout.NORTH);

        //初始化聊天室成员列表
        userLabelList = new ArrayList<JLabel>();
        for (String us:online) {
            JLabel lbu = new JLabel(us);
            lbu.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // 用户图标双击鼠标时显示对话框
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

        //界面整合
        split.setLeftComponent(left);
        split.setRightComponent(right);
        this.add(split);

        //绑定各种监听事件

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
        //关闭窗口事件
        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                try {
                    logout();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        //发送消息事件
        //快捷键发送消息
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
            if ("".equals(content.getText())) { //无内容
                JOptionPane.showMessageDialog(ChattingRoomFrame.this, "不能发送空消息!",
                        "不能发送", JOptionPane.ERROR_MESSAGE);
            } else {
                System.out.println(content.getText());
                System.out.println(getFontAttrib().toString());
                try {
                    //聊天室群发消息
                    JSONObject jsonObj = new JSONObject();
                    myFont = getFontAttrib();//文本样式
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

        //新线程持续监听接收消息并显示到chatBoard
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
                    //有新成员注册，更新总成员表
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
                        //先检查singlewindow是否已经打开了窗口，如果打开了，控制该窗口添加消息
                        String name=(String) receivedjsonObj.get("name");//name是发送方的名字
                        String text=(String) receivedjsonObj.get("text");//text是对方发送的内容
                        if(singlewindow.containsKey(name))
                            singlewindow.get(name).addMes(name,text);
                        //如果还没打开窗口，先缓存，并将用户标红
                        else{
                            if(!messBuffer.containsKey(name))
                                messBuffer.put(name,new ArrayList<>());
                            messBuffer.get(name).add(text);
                            for (JLabel ulbl : userLabelList)
                                if (name.equals(ulbl.getText()))
                                    ulbl.setForeground(Color.red);
                            //如果关闭勿扰模式则直接弹出弹窗
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
                    // 有用户下线
                    else if(mes==COMMAND_DROP){
                        String name = (String) receivedjsonObj.get("name");
                        online.remove(name);
                        refreshFriendList(name,"0");
                    }
                    // 被强制下线
                    else if(mes==COMMAND_FROCE){
                        socket.shutdownOutput();
                        socket.shutdownInput();
                        socket.close();
                        JOptionPane.showMessageDialog(ChattingRoomFrame.this, "您已被强制下线!",
                                "不能发送", JOptionPane.ERROR_MESSAGE);
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

        //设置窗口参数
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("用户："+username);
        setBounds(300,150,w,h);
        setVisible(true);
    }

    public void addRecMsg(String uname, String message,String picInfo,String font) {
        setExtendedState(Frame.NORMAL);
        //setVisible(true);
        String msg = uname + " " + sf.format(new Date());
        dateFont.setText(msg);//用户信息和时间
        insert(dateFont);
        pos1 = chatBoard.getCaretPosition();//记录插入的起始点
        if (!picInfo.equals("")) {/*存在表情信息*/
            FontAndText attr = getReceiveFont(font);
            insert(attr);
            receivedPicInfo(picInfo);
            insertPics();
        } else {
            FontAndText attr = getReceiveFont(font);
            insert(attr);
        }
    }

    //向聊天板中插入表情
    private void insertPics() {
        if (this.receivePicInfo.size() <= 0) {
            return;
        } else {
            for (int i = 0; i < receivePicInfo.size(); i++) {
                PicInfo pic = receivePicInfo.get(i);
                String fileName;
                chatBoard.setCaretPosition(pos1 + pic.getPos()); /*设置插入位置*/
                fileName = "/qqdefaultface/" + pic.getVal() + ".gif";/*修改图片路径*/
                chatBoard.insertIcon(new ImageIcon(PicsJWindow.class.getResource(fileName))); /*插入图片*/
            }
            receivePicInfo.clear();
        }
        chatBoard.setCaretPosition(docChat.getLength()); /*设置滚动到最下边*/
    }

    //输入区插入图片
    public void insertSendPic(ImageIcon imgIc) {
        content.insertIcon(imgIc); // 插入图片
    }

    /**
     * 将收到的消息转化为自定义的字体类对象
     */
    public FontAndText getReceiveFont(String message) {
        String[] msgs = message.split("[|]");
        String fontName = "";
        int fontSize = 0;
        String[] color;
        String text = message;
        Color fontC = new Color(222, 222, 222);
        if (msgs.length >= 4) {/*这里简单处理，表示存在字体信息*/
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

    //重组表情信息，重组后的信息串  格式为   位置&代号+位置&代号+……
    private String buildPicInfo() {
        StringBuilder sb = new StringBuilder("");
        //遍历jtextpane找出所有的图片信息封装成指定格式
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


    //聊天面板的插入文本
    private void insert(FontAndText attrib) {
        try { // 插入文本
            docChat.insertString(docChat.getLength(), attrib.getText() + "\n",
                    attrib.getAttrSet());
            System.out.println("123"+attrib.toString());
            System.out.println(attrib.getText());
            chatBoard.setCaretPosition(docChat.getLength()); // 设置滚动到最下边
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    //获取发送文字的格式
    private FontAndText getFontAttrib() {
        FontAndText att = new FontAndText();
        att.setText(content.getText());//文本信息
        att.setName((String) fontName.getSelectedItem());
        att.setSize(Integer.parseInt((String) fontSize.getSelectedItem()));
        String temp_color = (String) fontColor.getSelectedItem();
        if (temp_color.equals("黑色")) {
            att.setColor(new Color(0, 0, 0));
        } else if (temp_color.equals("红色")) {
            att.setColor(new Color(255, 0, 0));
        } else if (temp_color.equals("蓝色")) {
            att.setColor(new Color(0, 0, 255));
        } else if (temp_color.equals("黄色")) {
            att.setColor(new Color(255, 255, 0));
        } else if (temp_color.equals("绿色")) {
            att.setColor(new Color(0, 255, 0));
        }
        return att;
    }

    //重组收到的表情信息串
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


    //返回表情按钮，以便获取位置
    JButton getEmoji(){
        return emoji;
    }
    //重绘用户面板
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
                    // 用户图标双击鼠标时显示对话框
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
    //上下线刷新
    public void refreshFriendList(String userName, String flag) {
        // 更新聊天室成员列表
        if (flag.equals("1")) {
            online.add(userName);
            genMenberList();
        }
        else{
            online.remove(userName);
            genMenberList();
        }
    }
    //关闭窗口事件
    private void logout() throws IOException {
        int select = JOptionPane.showConfirmDialog(null,
                "确定退出吗？\n\n退出程序将中断与服务器的连接!", "退出聊天室",
                JOptionPane.YES_NO_OPTION);
        if (select == JOptionPane.YES_OPTION) {
            //向服务器发送退出聊天室信息
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("command", COMMAND_DROP);//注销命令

            OutputStream out = socket.getOutputStream();
            PrintStream printStream = new PrintStream(out);
            printStream.println(jsonObj);
            printStream.flush();

            //关闭当前socket
            socket.close();
            flag=false;
        }else{
            this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        }
    }


    //私聊窗口
    class SingleChatFrame extends JFrame {
        JTextArea chatBoard;
        String name;//私聊窗口的name是对方即target的名字
        public SingleChatFrame(String n){
            name=n;
            JSplitPane panel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            panel.setDividerLocation(0.7);

            //聊天区
            chatBoard = new JTextArea();
            chatBoard.setLineWrap(true);
            chatBoard.setEditable(false);
            panel.setTopComponent(chatBoard);

            //输入区
            JSplitPane inputArea = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            inputArea.setDividerLocation(0.8);
            JTextArea content = new JTextArea();
            inputArea.setTopComponent(content);
            JButton submit = new JButton("发送");
            inputArea.setBottomComponent(submit);
            panel.setBottomComponent(inputArea);

            getContentPane().add(panel);

            this.addComponentListener(new ComponentAdapter(){
                public void componentResized(ComponentEvent e) {
                    panel.setDividerLocation(0.7);
                    inputArea.setDividerLocation(0.8);
                }
            });
            //发送消息事件
            submit.addActionListener(l-> {
                if ("".equals(content.getText())) { //无内容
                    JOptionPane.showMessageDialog(null, "不能发送空消息!",
                            "不能发送", JOptionPane.ERROR_MESSAGE);
                } else {
                    //todo 发送消息
                    //私发消息
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
                    // 接收返回的json结果
                }
            });

            //设置窗口参数
            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            this.setSize(600, 500);
            this.setTitle("正在和"+name+"聊天");
            int x = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
            int y = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
            this.setLocation((x - this.getWidth()) / 2, (y-this.getHeight())/ 2);
            this.setVisible(true);

            //判断是否有消息缓存，如果有则取出并删除缓存
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
                JOptionPane.showMessageDialog(null, "发送失败，对方尚未在线!",
                        "发送失败", JOptionPane.ERROR_MESSAGE);
            }
            else{
                chatBoard.append(receivedjsonObj.get("name")+":\n"+receivedjsonObj.get("text")+'\n');
            }
        }
        public void addMes(String opname,String text){
            chatBoard.append(opname+":\n"+text+'\n');
        }
        //关闭窗口时要在singlewindow中删除自己


    }
    //历史消息面板
    class HistoryBoard extends JFrame{
        private JTextArea chatBoard;
        public HistoryBoard(){
            BorderLayout bor = new BorderLayout();
            setLayout(bor);

            //历史信息面板
            JScrollPane historyBoard = new JScrollPane();
            chatBoard = new JTextArea();
            chatBoard.setLineWrap(true);//激活自动换行功能
            chatBoard.setWrapStyleWord(true);// 激活断行不断字功能
            chatBoard.setEditable(false);//设置聊天版面不可编辑
            historyBoard.setViewportView(chatBoard);
            historyBoard.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            this.add(historyBoard,BorderLayout.CENTER);

            //下方输入区
            JPanel down=new JPanel();
            down.setLayout(new BorderLayout());
            JTextField dateInput = new JTextField();
            JButton submit = new JButton();
            submit.setText("查询");
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

            setTitle("历史记录查询");
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
//自定义图标类，用于标签，主要是加个可修改的代号
class ChatPic extends ImageIcon{
    int im;//图片代号
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
//自定义JWindow类制作选择表情框
class PicsJWindow extends JWindow {
    private static final long serialVersionUID = 1L;
    public static final String FACE_IMAGE_DIR = "/qqdefaultface/";//表情路径
    public static final String GIF_SUB = ".gif";//文件后缀
    GridLayout gridLayout1 = new GridLayout(7, 15);//7*15的网格存放所有的105个表情
    JLabel[] ico = new JLabel[105]; //表情标签数组
    int i;
    ChattingRoomFrame owner;

    public PicsJWindow(ChattingRoomFrame owner) {
        super(owner);
        this.owner = owner;
        try {
            init();
            this.setAlwaysOnTop(true);//始终在桌面最顶层显示窗体
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    //把qqdefaultface文件中的表情全部载入进数组中
    private void init() throws Exception {
        this.setPreferredSize(new Dimension(28 * 15, 28 * 7));
        JPanel p = new JPanel();
        p.setOpaque(true);//绘制其边界内的所有像素，即设置组件不透明
        this.setContentPane(p);//把p设为该JWindow容器的内容面板
        p.setLayout(gridLayout1);
        p.setBackground(SystemColor.text);
        String fileName = "";//标签的文件名
        for (i = 0; i < ico.length; i++) {
            fileName = FACE_IMAGE_DIR + i + GIF_SUB;//文件路径，即路径+序号+后缀
            ico[i] = new JLabel(new ChatPic(ChattingRoomFrame.class.getResource(fileName), i), SwingConstants.CENTER);
            ico[i].setBorder(BorderFactory.createLineBorder(new Color(225, 225, 225), 1));
            ico[i].setToolTipText(i + "");//在控件上显示提示信息
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
            System.out.println("展示");
            determineAndSetLocation();
        }
        else System.out.println("隐藏");
        super.setVisible(show);
    }

    private void determineAndSetLocation() {
        Point loc = owner.getEmoji().getLocationOnScreen();/*控件相对于屏幕的位置*/
        setBounds(loc.x - getPreferredSize().width / 3, loc.y - getPreferredSize().height,
                getPreferredSize().width, getPreferredSize().height);
    }

    private JWindow getObj() {
        return this;
    }

}
//字体和文本类
class FontAndText {
    String msg = "", name = "宋体"; // 要输入的文本和字体名称
    int size = 0; //字号
    Color color = new Color(225, 225, 225); // 文字颜色
    SimpleAttributeSet attrSet = null; // 属性集
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
        }//设置字体
        StyleConstants.setBold(attrSet, false);//加粗
        StyleConstants.setItalic(attrSet, false);//斜体
        StyleConstants.setFontSize(attrSet, size);//大小
        if (color != null)
            StyleConstants.setForeground(attrSet, color);//颜色
        return attrSet;
    }

    public String toString() {
        //将消息分为四块便于在网络上传播
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
//图片信息（位置和编号）
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