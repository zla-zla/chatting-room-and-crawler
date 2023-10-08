

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
    private ArrayList<String> wordList=new ArrayList<String>();		//�������д�
    private ArrayList<Integer> wordNum=new ArrayList<Integer>();	//�����Ӧ���дʵĳ��ִ���

    //�·�������
    private JPanel input=new JPanel(new BorderLayout(5,5));
    private JPanel btnz=new JPanel();
    private JTextArea url=new JTextArea();
    private JButton crawl = new JButton("��ʼ��ȡ");
    private JButton mulCrawl = new JButton("ͬʱ��ȡ�����ַ");
    private JButton delete = new JButton("ɾ����ǰҳ");

    private JTabbedPane tab=new JTabbedPane();

    //����������ʽ��ƥ���
    private String regExHtml="<[^>]+>";		//ƥ���ǩ
    private String regExScript = "<script[^>]*?>[\\s\\S]*?<\\/script>";		//ƥ��script��ǩ
    private String regExStyle = "<style[^>]*?>[\\s\\S]*?<\\/style>";		//ƥ��style��ǩ
    private String regExSpace="[\\s]{2,}";	//ƥ�������ո��س���
    private String regExImg="&[\\S]*?;+";	//ƥ����ҳ��ͼ��������
    //����������ʽ
    private Pattern pattern3=Pattern.compile(regExHtml, Pattern.CASE_INSENSITIVE);
    private Pattern pattern1=Pattern.compile(regExScript,Pattern.CASE_INSENSITIVE);
    private Pattern pattern2=Pattern.compile(regExStyle,Pattern.CASE_INSENSITIVE);
    private Pattern pattern4=Pattern.compile(regExSpace, Pattern.CASE_INSENSITIVE);
    private Pattern pattern5=Pattern.compile(regExImg,Pattern.CASE_INSENSITIVE);

    //�ַ�����
    public static void main(String[] agrs)
    {
        new crawler();    //����һ��ʵ��������
    }

    public crawler(){
        //�༭�·�������
        url.setLineWrap(true);
        url.setWrapStyleWord(true);
        btnz.add(crawl);
        btnz.add(mulCrawl);
        btnz.add(delete);
        input.add(btnz,BorderLayout.EAST);
        input.add(url,BorderLayout.CENTER);

        //ҳ���ܲ������ϣ��Ϸ�ѡ�ҳ�棬�·�������
        jp.add(tab,BorderLayout.CENTER);
        jp.add(input,BorderLayout.SOUTH);

        getContentPane().add(jp);
        setTitle("����");    //������ʾ���ڱ���
        setBounds(300,150,w,h);    //���ô�����ʾ�ߴ�
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    //�ô����Ƿ���Թر�
        setResizable(false);//���ô��ڲ��ɱ仯
        setVisible(true);    //���ô����Ƿ�ɼ�

        //���¼�����
        crawl.addActionListener(l->{
            String website = url.getText();
            if ("".equals(website)) { //������
                JOptionPane.showMessageDialog(this, "����������ַ!",
                        "������ȡ", JOptionPane.ERROR_MESSAGE);
            }
            else{
                String html=getHtml(website);	//��ʼ��ȡ
                String text=getText(html);	//ƥ����ҳ�ı�
                tab.add("����"+index,new page(text));
                index++;
            }
        });
        mulCrawl.addActionListener(l-> {
            JFileChooser fChooser=new JFileChooser();	//�ļ�ѡ���
            int ok=fChooser.showOpenDialog(this);
            if(ok!=JFileChooser.APPROVE_OPTION)	return;	//�ж��Ƿ�����ѡ��
            wordList.clear();	//���֮ǰ�ļ�¼
            File choosenLib = fChooser.getSelectedFile();    //��ȡѡ����ļ�
            BufferedReader br = null;
            try {    //��ȡѡ���ļ��еļ�¼
                br = new BufferedReader(new InputStreamReader(new FileInputStream(choosenLib), "UTF-8"));
                while (true) {
                    String str = br.readLine();
                    if (str == null) break;
                    wordList.add(str);    //��ӵ���¼��
                    wordNum.add(0);        //���ö�Ӧ�ĳ�ʼֵ
                }
                br.close();    //�ر��ļ���
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                JOptionPane.showMessageDialog(null, "�ļ�������");
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                JOptionPane.showMessageDialog(null, "�ļ���ȡʧ��");
                e1.printStackTrace();
            }

            fChooser = new JFileChooser();
            ok = fChooser.showOpenDialog(this);
            if (ok != JFileChooser.APPROVE_OPTION) return;    //�ж��Ƿ�����ѡ��
            choosenLib = fChooser.getSelectedFile();    //��ȡѡ����ļ�
            ArrayList<String> urls = new ArrayList<>();
            new SpiderAll(this, choosenLib).start();


        });
        delete.addActionListener(l->{
            tab.remove(tab.getSelectedIndex());
        });
    }

    class SpiderAll extends Thread{
        private File file=null;		//��ַ���ı��ļ�
        //���캯����ʼ��
        public SpiderAll(JFrame fa,File f) {
            file=f;
        }
        public void run() {
            try {
                //��ȡ��ַ���е���ַ
                BufferedReader brr=new BufferedReader(new FileReader(file));
                //��ƥ������д���ı���
                PrintStream ps=new PrintStream(new File("data.txt"));
                ps.println("���дʼ�¼����:");
                int size=wordList.size();
                while(true) {
                    String website=brr.readLine();
                    if(website==null)	break;
                    ps.println(website+"��������: ");
                    String html=getHtml(website);	//��ȡhtml����
                    String text=getText(html);		//ƥ����ҳ�ı�
                    for(int i=0;i<size;i++) {		//����ҳ�ı��н���ƥ��
                        String word=wordList.get(i);
                        int index=0,account=0,len=word.length();
                        while((index=text.indexOf(word,index))>=0) {
                            account++;
                            int temp=wordNum.get(i);	//��������
                            wordNum.set(i,++temp);
                            index+=len;		//����ƥ������
                        }
                        ps.println(word+"  ����  "+account+"��");	//д�뵱ǰ����
                    }
                    ps.println();
                }
                brr.close();	//�ر��ļ���
                System.out.println("��ȡ���");
                ps.println("����������:     ");		//д��������
                for(int i=0;i<size;i++) {
                    ps.println(wordList.get(i)+"  ����    "+wordNum.get(i)+"��");
                }
                ps.close();		//�ر��ļ���
                JOptionPane.showMessageDialog(null, "��ȡ��ϣ�����ļ��鿴!");
            }catch (Exception e) {
                // TODO: handle exception
                JOptionPane.showMessageDialog(null, "��ȡʧ��");
            }
        }
    }

    class page extends JPanel{
//        JPanel p=new JPanel(new BorderLayout());

        //���չʾ��
        JScrollPane txtll = new JScrollPane();//��ȡ�����ݵĹ�������
        JTextArea txt = new JTextArea();//�ı���

        //�ұ߿�����
        Box right=createVerticalBox();
        Box sens=createVerticalBox();
        JScrollPane senll = new JScrollPane();//���д��б�Ĺ�������
        JButton choose=new JButton("ѡ�����дʴʿ�");

        private ArrayList<JLabel>senWord=new ArrayList<>();//���дʱ�ǩ�б�
        private ArrayList<String> wordList=new ArrayList<String>();
        private ArrayList<Integer> wordNum=new ArrayList<Integer>();	//�����Ӧ���дʵĳ��ִ���
        private HashMap<String,ArrayList<Object>>hightLight=new HashMap<>();//�����дʵĸ�����������
        private HashMap<String,Integer>ColorState=new HashMap<>();//�����дʵ�ǰ��ɫ
        private HashMap<String,Integer>count=new HashMap<>();//�����дʵ�ǰ��ɫ
        //��ɫת����
        private ArrayList<Color>ColorTrans=new ArrayList<Color>(Arrays.asList(Color.yellow,Color.red,Color.green,Color.blue));
        public page(String text){

            setLayout(new BorderLayout());
            txt.setLineWrap(true);//�����Զ����й���
            txt.setWrapStyleWord(true);// ������в����ֹ���
            txt.setEditable(false);//����������治�ɱ༭
            txt.append(text);
            txtll.setViewportView(txt);
            txtll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            //�ұ߿������
            senll.setSize(40,80);
            senll.setViewportView(sens);
            right.add(senll);
            right.add(choose);

            //������������
            add(txtll,BorderLayout.CENTER);
            add(right,BorderLayout.EAST);

            choose.addActionListener(l->{
                getLib();
            });
        }

        //���ļ��ж�ȡ���д�
        public void getLib() {
            JFileChooser fChooser=new JFileChooser();	//�ļ�ѡ���
            int ok=fChooser.showOpenDialog(this);
            if(ok!=JFileChooser.APPROVE_OPTION)	return;	//�ж��Ƿ�����ѡ��
            wordList.clear();	//���֮ǰ�ļ�¼
            File choosenLib=fChooser.getSelectedFile();	//��ȡѡ����ļ�
            BufferedReader br=null;
            try {	//��ȡѡ���ļ��еļ�¼new FileReader(choosenLib)
                br=new BufferedReader(new InputStreamReader(new FileInputStream(choosenLib), "UTF-8"));
                while(true) {
                    String str=br.readLine();
                    if(str==null)	break;
                    wordList.add(str);	//��ӵ���¼��
                    wordNum.add(0);		//���ö�Ӧ�ĳ�ʼֵ
                    JLabel jbl=new JLabel(str);
                    //+"("+count.get(str).toString()+")"
                    senWord.add(jbl);

                    sens.add(jbl);
                    //˫��������ɫ��
                    jbl.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            // ˫�����дʻ�ɫ�����ݵ����ǩ�������ҵ��ùؼ��ֶ�Ӧ���Ǽ�����������һһɾ��
                            // ���ı������¶Ըùؼ�����ɫ���ò�ͬ��ɫ��
                            if (e.getClickCount() == 2) {
                                String s=jbl.getText();
                                System.out.println(s);
                                Highlighter hg=txt.getHighlighter();//��ȡ�ı��ܵĸ�������
                                ArrayList<Object>arr=hightLight.get(s);
                                for(Object obj:arr){
                                    hg.removeHighlight(obj);
                                }
                                //�����һ����ɫ���»���
                                ColorState.put(s,(ColorState.get(s)+1)%ColorTrans.size());
                                DefaultHighlighter.DefaultHighlightPainter painter=new
                                        DefaultHighlighter.DefaultHighlightPainter(ColorTrans.get(ColorState.get(s)));	//���ø�����ʾ��ɫΪ��ɫ
                                //���»��Ƹ���
                                ArrayList<Object>tmp=new ArrayList<>();
                                int index=0;
                                String text=txt.getText();	//�õ��ı�����ı�
                                while((index=text.indexOf(str,index))>=0) {
                                    try {
                                        tmp.add(hg.addHighlight(index, index+str.length(), painter));//������ʾƥ�䵽�Ĵ���
                                        index+=str.length();	//����ƥ����������ƥ��
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
                br.close();	//�ر��ļ���
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                JOptionPane.showMessageDialog(null, "�ļ�������");
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                JOptionPane.showMessageDialog(null, "�ļ���ȡʧ��");
                e1.printStackTrace();
            }
            showSensword();
        }

        //������ʾ
        public void showSensword() {
            Highlighter hg=txt.getHighlighter();	//�����ı���ĸ�����ʾ
            hg.removeAllHighlights();	//���֮ǰ�ĸ�����ʾ��¼
            String text=txt.getText();	//�õ��ı�����ı�
            DefaultHighlighter.DefaultHighlightPainter painter=new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);	//���ø�����ʾ��ɫΪ��ɫ
            //����ÿһ�����дʣ���whileѭ�����������ַ����ҵ�ƥ�䵽��ÿһ��λ�ã���ÿһ��λ��������һ������
            for(String str:wordList) {	//ƥ�����е�ÿһ�����д�
                ArrayList<Object>tmp=new ArrayList<>();
                int index=0,cnt=0;
                while((index=text.indexOf(str,index))>=0) {
                    try {
                        tmp.add(hg.addHighlight(index, index+str.length(), painter));//������ʾƥ�䵽�Ĵ���
                        index+=str.length();	//����ƥ����������ƥ��
                        cnt++;
                    } catch (BadLocationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                hightLight.put(str,tmp);
                ColorState.put(str,0);//��ʼ��Ϊ��ɫ
                System.out.println(cnt);
                count.put(str,(Integer) cnt);
            }
        }

    }


    //ʹ��URL��ȡ��ҳ��html����
    public String getHtml(String website) {
        String str=null;
        String text="";		//������ҳ������
        try {
            URL url=new URL(website);	//������Ӧ��URL����
            URLConnection urlConne=url.openConnection();	//����
            urlConne.connect();
            //��ȡ������
            BufferedReader br=new BufferedReader(new InputStreamReader(urlConne.getInputStream(),"UTF-8"));
            System.out.println("��ʼ��ȡ");
            while(true) {	//��ȡ������
                str=br.readLine();
                if(str==null)	break;
                text+=(str+"\n");
            }
            br.close();		//�ر�������
        }catch (Exception e) {
            // TODO: handle exception
            JOptionPane.showMessageDialog(null, website+"��ȡԴ����ʧ��");
        }
        System.out.println("��ȡ����");
        return text;	//����html�����ı�
    }

    //��html��������ƥ��,��ȡ�����е��ı�
    public String getText(String str) {

        Matcher matcher=pattern1.matcher(str);
        str=matcher.replaceAll("");		//ƥ����ͨ��ǩ
        matcher=pattern2.matcher(str);
        str=matcher.replaceAll("");		//ƥ��script��ǩ
        matcher=pattern3.matcher(str);
        str=matcher.replaceAll("");		//ƥ��style��ǩ
        matcher=pattern4.matcher(str);
        str=matcher.replaceAll("\n");	//ƥ�������س���ո�
        matcher=pattern5.matcher(str);
        str=matcher.replaceAll("");		//ƥ����ҳͼ�����ֵ�����
        return str;		//�����ı�
    }



}

