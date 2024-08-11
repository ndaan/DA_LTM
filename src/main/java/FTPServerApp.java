import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.PropertiesUserManager;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.util.Arrays;
public class FTPServerApp {
    public static void main(String[] args) {
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(2121);
        serverFactory.addListener("default", listenerFactory.createListener());
        File userFile = new File("myusers.properties");
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setFile(userFile);
        userManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());
        BaseUser user = new BaseUser();
        user.setName("Users");
        user.setPassword("12345");
        user.setHomeDirectory("D:/Ltmang/FTP_SEVER");
        user.setEnabled(true);
        user.setAuthorities(Arrays.asList(new WritePermission()));

        BaseUser user2 = new BaseUser();
        user2.setName("User2");
        user2.setPassword("56789");
        user2.setHomeDirectory("D:/Ltmang/FTP_SEVER/Sever2");
        user2.setEnabled(true);
        user2.setAuthorities(Arrays.asList(new WritePermission()));

        try {
            PropertiesUserManager userManager = (PropertiesUserManager) userManagerFactory.createUserManager();
            userManager.save(user);
            userManager.save(user2);
            serverFactory.setUserManager(userManager);
            FtpServer server = serverFactory.createServer();
            server.start();
            System.out.println("FTP server started.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
