package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/*
UserDAO�ࣺ�ṩ���ݿ���ʽӿڣ�ʵ�����ݿ���ɾ�Ĳ����
user��
1.List<Map<String, String>> findAll()������user������������
2.Map<String, String> findById(String user_name)����������(�û���)��ѯ�����û���Ϣ

friend��
3.List<Map<String, String>> findFriends(String id):��������(�û���)��ѯ�����б�
*/

public class UserDAO {
    public static void main(String[] args) throws SQLException, ParseException {
////        insertMess(new Date(),"����","������");
//        String datestr = "20220618";
//        DateFormat dFormat = new SimpleDateFormat("yyyyMMdd");
//        Date date = null;
//        date = dFormat.parse(datestr);
////        System.out.println(date.toString());
//        HashSet<String>history=queryHistory(date);
//        for(String s:history) System.out.println(s);

    }
    // ��ѯ�����û���Ϣ
    public static HashSet<String> findAllUser() {
        HashSet<String> list = new HashSet<String>();
        // SQL���
        String sql = "select * from user";
        try (
                Connection conn = DBHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();) {
            while (rs.next()) {
                list.add(rs.getString("user_name"));
            }
        } catch (SQLException e) {
        }
        return list;
    }
    // ��������(�û���)��ѯ
    public Map<String, String> findById(String user_name) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        // SQL���
        String sql = "select user_name,user_pwd from user where user_name = ?";
        try {
            conn = DBHelper.getConnection();//����������
            pstmt = conn.prepareStatement(sql);//ִ��sql���
            pstmt.setString(1, user_name);//��дsql����еĲ���
            rs = pstmt.executeQuery();//ִ��sql��䷵�ؽ��
            if (rs.next()) {
                Map<String, String> row = new HashMap<String, String>();
                row.put("user_pwd", rs.getString("user_pwd"));
                row.put("user_name", rs.getString("user_name"));
                return row;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { // �ͷ���Դ
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
        return null;
    }
    // ע��ʱ�������û�
    public static boolean insertUser(String name,String pwd) throws SQLException {
        HashSet<String> list = new HashSet<String>();
        // SQL���
        String sql = "insert into user values (?,?)";
        Connection conn = DBHelper.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1,name);
        pstmt.setString(2,pwd);
        pstmt.executeUpdate();
        return true;
    }
    //  �����ʷ��Ϣ
    public static boolean insertMess(Date date,String name,String text) throws SQLException {
        HashSet<String> list = new HashSet<String>();
        // SQL���
        String sql = "insert into history values (?,?,?)";
        Connection conn = DBHelper.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);

        java.sql.Timestamp d = new java.sql.Timestamp(date.getTime());
        pstmt.setTimestamp(1,d);
        pstmt.setString(2,name);
        pstmt.setString(3,text);
        pstmt.executeUpdate();
        return true;
    }
    //��ѯ��ʷ��¼
    public static ArrayList<String> queryHistory(Date date) throws SQLException {
        ArrayList<String> list = new ArrayList<String>();
        // SQL���
        java.sql.Timestamp d1 = new java.sql.Timestamp(date.getTime());
        Calendar   calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE,1); //��������������һ��,����  ������,������ǰ�ƶ�
        date=calendar.getTime(); //���ʱ���������������һ��Ľ��
        java.sql.Timestamp d2 = new java.sql.Timestamp(date.getTime());
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "select * from history where time >= ? and time <= ?";
        conn=DBHelper.getConnection();
        pstmt=conn.prepareStatement(sql);
        pstmt.setTimestamp(1,d1);
        pstmt.setTimestamp(2,d2);
        rs=pstmt.executeQuery();
        while (rs.next()) {
            list.add(rs.getString(1)+"\t"+rs.getString(2)+"\t"+rs.getString(3));
        }
        return list;
    }
    // ��������(�û���)��ѯ�������ֵļ���
    public HashSet<String> findFriends(String id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        HashSet<String> friends = new HashSet<String>();
        String sql = "select user_name FROM user " + " WHERE "
                + " user_name IN (select user_name2 as user_name from friend where user_name1 = ?)"
                + " OR user_name IN (select user_name1 as user_name from friend where user_name2 = ?)";
        try {
            conn = DBHelper.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, id);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                friends.add(rs.getString("user_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { // �ͷ���Դ
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
        return friends;
    }
}