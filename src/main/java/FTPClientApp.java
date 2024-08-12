import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.*;
import java.util.Scanner;

// ... các import khác và lớp FTPClientApp

public class FTPClientApp {
    private static FTPClient ftpClient = new FTPClient();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            ftpClient.connect("localhost", 2121);
            ftpClient.login("Users", "12345");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            boolean quit = false;
            while (!quit) {
                System.out.println("Chọn chức năng:");
                System.out.println("1. Gửi tệp tin lên server");
                System.out.println("2. Tải tệp tin từ server xuống");
                System.out.println("3. Tải thư mục từ server xuống");
                System.out.println("4. Xem danh sách thư mục trên server");
                System.out.println("5. Tạo tệp tin mới trên server");
                System.out.println("6. Tạo thư mục mới trên server");
                System.out.println("7. Thoát");
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1:
                        System.out.print("Nhập đường dẫn tệp tin cần gửi: ");
                        String localFilePath = scanner.nextLine();
                        File uploadFile = new File(localFilePath);
                        if (uploadFile.exists() && !uploadFile.isDirectory()) {
                            System.out.print("Nhập đường dẫn thư mục đích trên server (ví dụ: /Subfolder): ");
                            String remoteDir = scanner.nextLine();
                            String remoteFileName = uploadFile.getName();
                            String remoteFilePath = remoteDir + "/" + remoteFileName;

                            try {
                                while (isFileExist(remoteFilePath)) {
                                    System.out.println("Tệp tin đã tồn tại trên server.");
                                    System.out.print("Nhập tên mới cho tệp tin: ");
                                    remoteFileName = scanner.nextLine();
                                    remoteFilePath = remoteDir + "/" + remoteFileName;
                                }
                                try (FileInputStream fis = new FileInputStream(uploadFile)) {
                                    boolean success = ftpClient.storeFile(remoteFilePath, fis);
                                    if (success) {
                                        System.out.println("Tải lên tệp thành công.");
                                    } else {
                                        System.out.println("Tải lên tệp thất bại.");
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("Tệp tin không tồn tại hoặc là thư mục.");
                        }
                        break;


                    case 2:
                        System.out.print("Nhập tên tệp tin trên server: ");
                        String remoteFileName = scanner.nextLine();
                        String downloadDir = "D:/Ltmang/DA_LTM/download";
                        File localFile = new File(downloadDir, "downloaded_" + remoteFileName);

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
                        try {
                            System.out.print("Nhập tên thư mục trên server: ");
                            String remoteFolderName = scanner.nextLine();
                            String localFolderPath = "D:/Ltmang/DA_LTM/download/" + remoteFolderName;
                            downloadFromServer(remoteFolderName, localFolderPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;

                    case 4:
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
                    case 5:
                        System.out.print("Nhập tên tệp tin mới trên server (bao gồm đuôi .txt hoặc .docx): ");
                        String fileName = scanner.nextLine();
                        System.out.print("Nhập nội dung tệp tin: ");
                        String fileContent = scanner.nextLine();
                        createFileOnServer(fileName, fileContent);
                        break;
                    case 6:
                        System.out.print("Nhập tên thư mục mới trên server: ");
                        String folderName = scanner.nextLine();
                        createFolderOnServer(folderName);
                        break;
                    case 7:
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
    private static void downloadFolder(Scanner scanner) {

    }
    private static void downloadFromServer(String remotePath, String localDir) throws IOException {
        if (ftpClient.listFiles(remotePath).length == 0) {
            // Nếu remotePath là một tệp
            File localFile = new File(localDir, new File(remotePath).getName());
            try (FileOutputStream fos = new FileOutputStream(localFile)) {
                boolean success = ftpClient.retrieveFile(remotePath, fos);
                if (success) {
                    System.out.println("Tải xuống tệp thành công. Lưu tại: " + localFile.getAbsolutePath());
                } else {
                    System.out.println("Tải xuống tệp thất bại.");
                }
            }
        } else {
            // Nếu remotePath là thư mục
            File localFolder = new File(localDir);
            if (!localFolder.exists()) {
                localFolder.mkdirs(); // Tạo thư mục nếu không tồn tại
            }

            FTPFile[] files = ftpClient.listFiles(remotePath);
            for (FTPFile file : files) {
                String remoteFilePath = remotePath + "/" + file.getName();
                String localFilePath = localFolder.getAbsolutePath() + "/" + file.getName();
                if (file.isDirectory()) {
                    downloadFromServer(remoteFilePath, localFilePath); // Đệ quy để tải thư mục con
                } else {
                    try (FileOutputStream fos = new FileOutputStream(localFilePath)) {
                        boolean success = ftpClient.retrieveFile(remoteFilePath, fos);
                        if (success) {
                            System.out.println("Tải xuống tệp thành công. Lưu tại: " + localFilePath);
                        } else {
                            System.out.println("Tải xuống tệp thất bại.");
                        }
                    }
                }
            }
        }
    }
}



