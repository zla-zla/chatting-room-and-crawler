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
UserDAO类：提供数据库访问接口，实现数据库增删改查操作
user表：
1.List<Map<String, String>> findAll()：返回user表中所有数据
2.Map<String, String> findById(String user_name)：根据主键(用户名)查询单个用户信息

friend表：
3.List<Map<String, String>> findFriends(String id):根据主键(用户名)查询好友列表
*/

public class UserDAO {
    public static void main(String[] args) throws SQLException, ParseException {
////        insertMess(new Date(),"张三","啊啊啊");
//        String datestr = "20220618";
//        DateFormat dFormat = new SimpleDateFormat("yyyyMMdd");
//        Date date = null;
//        date = dFormat.parse(datestr);
////        System.out.println(date.toString());
//        HashSet<String>history=queryHistory(date);
//        for(String s:history) System.out.println(s);

    }
    // 查询所有用户信息
    public static HashSet<String> findAllUser() {
        HashSet<String> list = new HashSet<String>();
        // SQL语句
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
    // 按照主键(用户名)查询
    public Map<String, String> findById(String user_name) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        // SQL语句
        String sql = "select user_name,user_pwd from user where user_name = ?";
        try {
            conn = DBHelper.getConnection();//连接数据里
            pstmt = conn.prepareStatement(sql);//执行sql语句
            pstmt.setString(1, user_name);//填写sql语句中的参数
            rs = pstmt.executeQuery();//执行sql语句返回结果
            if (rs.next()) {
                Map<String, String> row = new HashMap<String, String>();
                row.put("user_pwd", rs.getString("user_pwd"));
                row.put("user_name", rs.getString("user_name"));
                return row;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { // 释放资源
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
    // 注册时插入新用户
    public static boolean insertUser(String name,String pwd) throws SQLException {
        HashSet<String> list = new HashSet<String>();
        // SQL语句
        String sql = "insert into user values (?,?)";
        Connection conn = DBHelper.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1,name);
        pstmt.setString(2,pwd);
        pstmt.executeUpdate();
        return true;
    }
    //  添加历史信息
    public static boolean insertMess(Date date,String name,String text) throws SQLException {
        HashSet<String> list = new HashSet<String>();
        // SQL语句
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
    //查询历史记录
    public static ArrayList<String> queryHistory(Date date) throws SQLException {
        ArrayList<String> list = new ArrayList<String>();
        // SQL语句
        java.sql.Timestamp d1 = new java.sql.Timestamp(date.getTime());
        Calendar   calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE,1); //把日期往后增加一天,整数  往后推,负数往前移动
        date=calendar.getTime(); //这个时间就是日期往后推一天的结果
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
    // 根据主键(用户名)查询好友名字的集合
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
        } finally { // 释放资源
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