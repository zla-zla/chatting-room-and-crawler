package db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

//用于创建数据库连接
public class DBHelper {
    static String url;
    static Properties info = new Properties();
    // 驱动程序加载（静态代码）
    static {
        // 从配置文件中获得属性文件输入流
        InputStream input = DBHelper.class.getClassLoader()
                .getResourceAsStream("config.properties");
        try {
            info.load(input);
            url = info.getProperty("url");
            String driverClassName = info.getProperty("driver");
            Class.forName(driverClassName);
            System.out.println("驱动程序加载成功...");
        } catch (ClassNotFoundException e) {
            System.out.println("驱动程序加载失败...");
        } catch (IOException e) {
            System.out.println("加载属性文件失败...");
        }
    }
    public static Connection getConnection() throws SQLException {
        // 创建数据库连接
        Connection conn = DriverManager.getConnection(url, info);
        return conn;
    }
}