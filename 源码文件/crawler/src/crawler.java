

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.swing.Box.createVerticalBox;

public class crawler extends JFrame {

    private int h=600;
    private int w=800;
    private int index=1;
    private JPanel jp=new JPanel(new BorderLayout(5,5));
    private ArrayList<String> wordList=new ArrayList<String>();		//保存敏感词
    private ArrayList<Integer> wordNum=new ArrayList<Integer>();	//保存对应敏感词的出现次数

    //下方输入区
    private JPanel input=new JPanel(new BorderLayout(5,5));
    private JPanel btnz=new JPanel();
    private JTextArea url=new JTextArea();
    private JButton crawl = new JButton("开始爬取");
    private JButton mulCrawl = new JButton("同时爬取多个网址");
    private JButton delete = new JButton("删除当前页");

    private JTabbedPane tab=new JTabbedPane();

    //设置正则表达式的匹配符
    private String regExHtml="<[^>]+>";		//匹配标签
    private String regExScript = "<script[^>]*?>[\\s\\S]*?<\\/script>";		//匹配script标签
    private String regExStyle = "<style[^>]*?>[\\s\\S]*?<\\/style>";		//匹配style标签
    private String regExSpace="[\\s]{2,}";	//匹配连续空格或回车等
    private String regExImg="&[\\S]*?;+";	//匹配网页上图案的乱码
    //定义正则表达式
    private Pattern pattern3=Pattern.compile(regExHtml, Pattern.CASE_INSENSITIVE);
    private Pattern pattern1=Pattern.compile(regExScript,Pattern.CASE_INSENSITIVE);
    private Pattern pattern2=Pattern.compile(regExStyle,Pattern.CASE_INSENSITIVE);
    private Pattern pattern4=Pattern.compile(regExSpace, Pattern.CASE_INSENSITIVE);
    private Pattern pattern5=Pattern.compile(regExImg,Pattern.CASE_INSENSITIVE);

    //字符编码
    public static void main(String[] agrs)
    {
        new crawler();    //创建一个实例化对象
    }

    public crawler(){
        //编辑下方输入区
        url.setLineWrap(true);
        url.setWrapStyleWord(true);
        btnz.add(crawl);
        btnz.add(mulCrawl);
        btnz.add(delete);
        input.add(btnz,BorderLayout.EAST);
        input.add(url,BorderLayout.CENTER);

        //页面总布局整合，上方选项卡页面，下方输入区
        jp.add(tab,BorderLayout.CENTER);
        jp.add(input,BorderLayout.SOUTH);

        getContentPane().add(jp);
        setTitle("爬虫");    //设置显示窗口标题
        setBounds(300,150,w,h);    //设置窗口显示尺寸
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    //置窗口是否可以关闭
        setResizable(false);//设置窗口不可变化
        setVisible(true);    //设置窗口是否可见

        //绑定事件函数
        crawl.addActionListener(l->{
            String website = url.getText();
            if ("".equals(website)) { //无内容
                JOptionPane.showMessageDialog(this, "请先输入网址!",
                        "不能爬取", JOptionPane.ERROR_MESSAGE);
            }
            else{
                String html=getHtml(website);	//开始爬取
                String text=getText(html);	//匹配网页文本
                tab.add("窗口"+index,new page(text));
                index++;
            }
        });
        mulCrawl.addActionListener(l-> {
            JFileChooser fChooser=new JFileChooser();	//文件选择框
            int ok=fChooser.showOpenDialog(this);
            if(ok!=JFileChooser.APPROVE_OPTION)	return;	//判断是否正常选择
            wordList.clear();	//清空之前的记录
            File choosenLib = fChooser.getSelectedFile();    //获取选择的文件
            BufferedReader br = null;
            try {    //读取选中文件中的记录
                br = new BufferedReader(new InputStreamReader(new FileInputStream(choosenLib), "UTF-8"));
                while (true) {
                    String str = br.readLine();
                    if (str == null) break;
                    wordList.add(str);    //添加到记录中
                    wordNum.add(0);        //设置对应的初始值
                }
                br.close();    //关闭文件流
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                JOptionPane.showMessageDialog(null, "文件不存在");
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                JOptionPane.showMessageDialog(null, "文件读取失败");
                e1.printStackTrace();
            }

            fChooser = new JFileChooser();
            ok = fChooser.showOpenDialog(this);
            if (ok != JFileChooser.APPROVE_OPTION) return;    //判断是否正常选择
            choosenLib = fChooser.getSelectedFile();    //获取选择的文件
            ArrayList<String> urls = new ArrayList<>();
            new SpiderAll(this, choosenLib).start();


        });
        delete.addActionListener(l->{
            tab.remove(tab.getSelectedIndex());
        });
    }

    class SpiderAll extends Thread{
        private File file=null;		//网址库文本文件
        //构造函数初始化
        public SpiderAll(JFrame fa,File f) {
            file=f;
        }
        public void run() {
            try {
                //读取网址库中的网址
                BufferedReader brr=new BufferedReader(new FileReader(file));
                //将匹配数据写入文本中
                PrintStream ps=new PrintStream(new File("data.txt"));
                ps.println("敏感词记录如下:");
                int size=wordList.size();
                while(true) {
                    String website=brr.readLine();
                    if(website==null)	break;
                    ps.println(website+"数据如下: ");
                    String html=getHtml(website);	//获取html代码
                    String text=getText(html);		//匹配网页文本
                    for(int i=0;i<size;i++) {		//在网页文本中进行匹配
                        String word=wordList.get(i);
                        int index=0,account=0,len=word.length();
                        while((index=text.indexOf(word,index))>=0) {
                            account++;
                            int temp=wordNum.get(i);	//更新数据
                            wordNum.set(i,++temp);
                            index+=len;		//更新匹配条件
                        }
                        ps.println(word+"  出现  "+account+"次");	//写入当前数据
                    }
                    ps.println();
                }
                brr.close();	//关闭文件流
                System.out.println("爬取完毕");
                ps.println("总数据如下:     ");		//写入总数据
                for(int i=0;i<size;i++) {
                    ps.println(wordList.get(i)+"  出现    "+wordNum.get(i)+"次");
                }
                ps.close();		//关闭文件流
                JOptionPane.showMessageDialog(null, "爬取完毕！请打开文件查看!");
            }catch (Exception e) {
                // TODO: handle exception
                JOptionPane.showMessageDialog(null, "爬取失败");
            }
        }
    }

    class page extends JPanel{
//        JPanel p=new JPanel(new BorderLayout());

        //左边展示区
        JScrollPane txtll = new JScrollPane();//爬取的内容的滚动容器
        JTextArea txt = new JTextArea();//文本区

        //右边控制区
        Box right=createVerticalBox();
        Box sens=createVerticalBox();
        JScrollPane senll = new JScrollPane();//敏感词列表的滚动容器
        JButton choose=new JButton("选择敏感词词库");

        private ArrayList<JLabel>senWord=new ArrayList<>();//敏感词标签列表
        private ArrayList<String> wordList=new ArrayList<String>();
        private ArrayList<Integer> wordNum=new ArrayList<Integer>();	//保存对应敏感词的出现次数
        private HashMap<String,ArrayList<Object>>hightLight=new HashMap<>();//该敏感词的高亮对象数组
        private HashMap<String,Integer>ColorState=new HashMap<>();//该敏感词当前颜色
        private HashMap<String,Integer>count=new HashMap<>();//该敏感词当前颜色
        //颜色转换表
        private ArrayList<Color>ColorTrans=new ArrayList<Color>(Arrays.asList(Color.yellow,Color.red,Color.green,Color.blue));
        public page(String text){

            setLayout(new BorderLayout());
            txt.setLineWrap(true);//激活自动换行功能
            txt.setWrapStyleWord(true);// 激活断行不断字功能
            txt.setEditable(false);//设置聊天版面不可编辑
            txt.append(text);
            txtll.setViewportView(txt);
            txtll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            //右边控制面板
            senll.setSize(40,80);
            senll.setViewportView(sens);
            right.add(senll);
            right.add(choose);

            //整合左右区域
            add(txtll,BorderLayout.CENTER);
            add(right,BorderLayout.EAST);

            choose.addActionListener(l->{
                getLib();
            });
        }

        //从文件中读取敏感词
        public void getLib() {
            JFileChooser fChooser=new JFileChooser();	//文件选择框
            int ok=fChooser.showOpenDialog(this);
            if(ok!=JFileChooser.APPROVE_OPTION)	return;	//判断是否正常选择
            wordList.clear();	//清空之前的记录
            File choosenLib=fChooser.getSelectedFile();	//获取选择的文件
            BufferedReader br=null;
            try {	//读取选中文件中的记录new FileReader(choosenLib)
                br=new BufferedReader(new InputStreamReader(new FileInputStream(choosenLib), "UTF-8"));
                while(true) {
                    String str=br.readLine();
                    if(str==null)	break;
                    wordList.add(str);	//添加到记录中
                    wordNum.add(0);		//设置对应的初始值
                    JLabel jbl=new JLabel(str);
                    //+"("+count.get(str).toString()+")"
                    senWord.add(jbl);

                    sens.add(jbl);
                    //双击更换颜色。
                    jbl.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            // 双击敏感词换色。根据点击标签的文字找到该关键字对应的那几个高亮对象并一一删除
                            // 在文本中重新对该关键字上色，用不同颜色。
                            if (e.getClickCount() == 2) {
                                String s=jbl.getText();
                                System.out.println(s);
                                Highlighter hg=txt.getHighlighter();//获取文本总的高亮对象
                                ArrayList<Object>arr=hightLight.get(s);
                                for(Object obj:arr){
                                    hg.removeHighlight(obj);
                                }
                                //获得下一个颜色重新绘制
                                ColorState.put(s,(ColorState.get(s)+1)%ColorTrans.size());
                                DefaultHighlighter.DefaultHighlightPainter painter=new
                                        DefaultHighlighter.DefaultHighlightPainter(ColorTrans.get(ColorState.get(s)));	//设置高亮显示颜色为黄色
                                //重新绘制高亮
                                ArrayList<Object>tmp=new ArrayList<>();
                                int index=0;
                                String text=txt.getText();	//得到文本框的文本
                                while((index=text.indexOf(str,index))>=0) {
                                    try {
                                        tmp.add(hg.addHighlight(index, index+str.length(), painter));//高亮显示匹配到的词语
                                        index+=str.length();	//更新匹配条件继续匹配
                                    } catch (BadLocationException i) {
                                        // TODO Auto-generated catch block
                                        i.printStackTrace();
                                    }
                                }
                                hightLight.put(str,tmp);
                            }
                        }
                    });
                    SwingUtilities.updateComponentTreeUI(this);
                }
                br.close();	//关闭文件流
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                JOptionPane.showMessageDialog(null, "文件不存在");
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                JOptionPane.showMessageDialog(null, "文件读取失败");
                e1.printStackTrace();
            }
            showSensword();
        }

        //高亮显示
        public void showSensword() {
            Highlighter hg=txt.getHighlighter();	//设置文本框的高亮显示
            hg.removeAllHighlights();	//清除之前的高亮显示记录
            String text=txt.getText();	//得到文本框的文本
            DefaultHighlighter.DefaultHighlightPainter painter=new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);	//设置高亮显示颜色为黄色
            //对于每一个敏感词，用while循环遍历整个字符串找到匹配到的每一个位置，在每一个位置上增加一个高亮
            for(String str:wordList) {	//匹配其中的每一个敏感词
                ArrayList<Object>tmp=new ArrayList<>();
                int index=0,cnt=0;
                while((index=text.indexOf(str,index))>=0) {
                    try {
                        tmp.add(hg.addHighlight(index, index+str.length(), painter));//高亮显示匹配到的词语
                        index+=str.length();	//更新匹配条件继续匹配
                        cnt++;
                    } catch (BadLocationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                hightLight.put(str,tmp);
                ColorState.put(str,0);//初始都为黄色
                System.out.println(cnt);
                count.put(str,(Integer) cnt);
            }
        }

    }


    //使用URL爬取网页的html代码
    public String getHtml(String website) {
        String str=null;
        String text="";		//保存网页的内容
        try {
            URL url=new URL(website);	//建立对应的URL对象
            URLConnection urlConne=url.openConnection();	//连接
            urlConne.connect();
            //获取输入流
            BufferedReader br=new BufferedReader(new InputStreamReader(urlConne.getInputStream(),"UTF-8"));
            System.out.println("开始爬取");
            while(true) {	//爬取到结束
                str=br.readLine();
                if(str==null)	break;
                text+=(str+"\n");
            }
            br.close();		//关闭输入流
        }catch (Exception e) {
            // TODO: handle exception
            JOptionPane.showMessageDialog(null, website+"爬取源代码失败");
        }
        System.out.println("爬取结束");
        return text;	//返回html代码文本
    }

    //对html进行正则匹配,提取出其中的文本
    public String getText(String str) {

        Matcher matcher=pattern1.matcher(str);
        str=matcher.replaceAll("");		//匹配普通标签
        matcher=pattern2.matcher(str);
        str=matcher.replaceAll("");		//匹配script标签
        matcher=pattern3.matcher(str);
        str=matcher.replaceAll("");		//匹配style标签
        matcher=pattern4.matcher(str);
        str=matcher.replaceAll("\n");	//匹配连续回车或空格
        matcher=pattern5.matcher(str);
        str=matcher.replaceAll("");		//匹配网页图案出现的乱码
        return str;		//返回文本
    }



}

