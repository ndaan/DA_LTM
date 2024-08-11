import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.*;
import java.util.Scanner;

public class FTPClientApp2 {
    private static FTPClient ftpClient = new FTPClient();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            ftpClient.connect("localhost", 2121);
            ftpClient.login("User2", "56789");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            boolean quit = false;
            while (!quit) {
                System.out.println("Chọn chức năng:");
                System.out.println("1. Gửi tệp tin lên server");
                System.out.println("2. Tải tệp tin từ server xuống");
                System.out.println("3. Xem danh sách thư mục trên server");
                System.out.println("4. Tạo tệp tin mới trên server");
                System.out.println("5. Tạo thư mục mới trên server");
                System.out.println("6. Thoát");
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1:
                        System.out.print("Nhập đường dẫn tệp tin cần gửi: ");
                        String localFilePath = scanner.nextLine();
                        File uploadFile = new File(localFilePath);
                        if (uploadFile.exists() && !uploadFile.isDirectory()) {
                            String remoteFileName = uploadFile.getName();
                            while (isFileExist(remoteFileName)) {
                                System.out.println("Tệp tin đã tồn tại trên server.");
                                System.out.print("Nhập tên mới cho tệp tin: ");
                                remoteFileName = scanner.nextLine();
                            }
                            try (FileInputStream fis = new FileInputStream(uploadFile)) {
                                boolean success = ftpClient.storeFile(remoteFileName, fis);
                                if (success) {
                                    System.out.println("Tải lên tệp thành công.");
                                } else {
                                    System.out.println("Tải lên tệp thất bại.");
                                }
                            }
                        } else {
                            System.out.println("Tệp tin không tồn tại hoặc là thư mục.");
                        }
                        break;
                    case 2:
                        System.out.print("Nhập tên tệp tin trên server: ");
                        String remoteFileName = scanner.nextLine();

                        // Đường dẫn mặc định đến thư mục lưu trữ
                        String downloadDir = "D:/Ltmang/DA_LTM/download";
                        File localFile = new File(downloadDir, "downloaded2_" + remoteFileName);

                        try (FileOutputStream fos = new FileOutputStream(localFile)) {
                            boolean success = ftpClient.retrieveFile(remoteFileName, fos);
                            if (success) {
                                System.out.println("Tải xuống tệp thành công. Lưu tại: " + localFile.getAbsolutePath());
                            } else {
                                System.out.println("Tải xuống tệp thất bại.");
                            }
                        }
                        break;

                    case 3:
                        System.out.println("Danh sách thư mục trên server:");
                        String[] directories = ftpClient.listNames();
                        if (directories != null && directories.length > 0) {
                            for (String dir : directories) {
                                System.out.println(dir);
                            }
                        } else {
                            System.out.println("Không có thư mục nào.");
                        }
                        break;
                    case 4:
                        System.out.print("Nhập tên tệp tin mới trên server (bao gồm đuôi .txt hoặc .docx): ");
                        String fileName = scanner.nextLine();
                        System.out.print("Nhập nội dung tệp tin: ");
                        String fileContent = scanner.nextLine();
                        createFileOnServer(fileName, fileContent);
                        break;
                    case 5:
                        System.out.print("Nhập tên thư mục mới trên server: ");
                        String folderName = scanner.nextLine();
                        createFolderOnServer(folderName);
                        break;
                    case 6:
                        quit = true;
                        break;
                    default:
                        System.out.println("Lựa chọn không hợp lệ.");
                        break;
                }
            }

            ftpClient.logout();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            scanner.close();
        }
    }

    private static boolean isFileExist(String remoteFileName) throws IOException {
        FTPFile[] files = ftpClient.listFiles();
        for (FTPFile file : files) {
            if (file.getName().equals(remoteFileName)) {
                return true;
            }
        }
        return false;
    }

    private static void createFileOnServer(String fileName, String content) {
        try {
            if (fileName.endsWith(".txt")) {
                try (ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes())) {
                    boolean success = ftpClient.storeFile(fileName, bais);
                    if (success) {
                        System.out.println("Tạo tệp tin .txt với nội dung thành công.");
                    } else {
                        System.out.println("Tạo tệp tin .txt thất bại.");
                    }
                }
            } else if (fileName.endsWith(".docx")) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    XWPFDocument document = new XWPFDocument();
                    XWPFParagraph paragraph = document.createParagraph();
                    XWPFRun run = paragraph.createRun();
                    run.setText(content);

                    document.write(baos);
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
                        boolean success = ftpClient.storeFile(fileName, bais);
                        if (success) {
                            System.out.println("Tạo tệp tin .docx với nội dung thành công.");
                        } else {
                            System.out.println("Tạo tệp tin .docx thất bại.");
                        }
                    }
                }
            } else {
                System.out.println("Vui lòng nhập tên tệp có đuôi .txt hoặc .docx");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createFolderOnServer(String folderName) {
        try {
            boolean success = ftpClient.makeDirectory(folderName);
            if (success) {
                System.out.println("Tạo thư mục thành công.");
            } else {
                System.out.println("Tạo thư mục thất bại.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
