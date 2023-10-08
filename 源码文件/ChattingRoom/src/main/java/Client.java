import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import Frame.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class Client {
//    public static DatagramSocket socket;
    public static void main(String[] args) throws IOException {
        try {
            UIManager.setLookAndFeel( new FlatLightLaf() );
        } catch( Exception ex ) {
            System.err.println( "Failed to initialize LaF" );
        }
        System.out.println(Charset.defaultCharset());
        new LoginFrame();
    }
}