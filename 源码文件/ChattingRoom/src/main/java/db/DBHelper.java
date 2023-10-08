package db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

//���ڴ������ݿ�����
public class DBHelper {
    static String url;
    static Properties info = new Properties();
    // ����������أ���̬���룩
    static {
        // �������ļ��л�������ļ�������
        InputStream input = DBHelper.class.getClassLoader()
                .getResourceAsStream("config.properties");
        try {
            info.load(input);
            url = info.getProperty("url");
            String driverClassName = info.getProperty("driver");
            Class.forName(driverClassName);
            System.out.println("����������سɹ�...");
        } catch (ClassNotFoundException e) {
            System.out.println("�����������ʧ��...");
        } catch (IOException e) {
            System.out.println("���������ļ�ʧ��...");
        }
    }
    public static Connection getConnection() throws SQLException {
        // �������ݿ�����
        Connection conn = DriverManager.getConnection(url, info);
        return conn;
    }
}