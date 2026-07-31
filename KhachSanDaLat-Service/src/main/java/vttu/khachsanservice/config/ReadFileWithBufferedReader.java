package vttu.khachsanservice.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ReadFileWithBufferedReader {
    public static String readFile(String fileName) {
        // Tạo đối tượng Scanner để đọc file
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Lưu trữ nội dung file
        StringBuilder content = new StringBuilder();

        // Duyệt qua từng dòng trong file
        while (scanner.hasNextLine()) {
            // Lấy dòng hiện tại
            String line = scanner.nextLine();

            // Thêm dòng vào nội dung
            content.append(line).append("\n");
        }

        // Đóng file
        scanner.close();
        // Trả về nội dung file
        return content.toString().trim();
    }
}
