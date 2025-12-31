package vttu.khachsanservice.utils;

import com.nlf.calendar.Lunar;
import com.nlf.calendar.Solar;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VietnamHolidayUtils {
    public static List<LocalDate> getVietnamHolidays(int year) {
        List<LocalDate> holidays = new ArrayList<>();

        // --- Các ngày lễ dương lịch ---
        holidays.add(LocalDate.of(year, 1, 1));   // Tết Dương lịch
        holidays.add(LocalDate.of(year, 4, 30));  // Giải phóng miền Nam
        holidays.add(LocalDate.of(year, 5, 1));   // Quốc tế Lao động
        holidays.add(LocalDate.of(year, 9, 2));   // Quốc khánh
        holidays.add(LocalDate.of(year,3,8)); // Quốc tế Phụ nữ
        holidays.add(LocalDate.of(year,2,14)); // Ngày lễ tình nhân

        // --- Tết Nguyên Đán: mùng 1 đến mùng 5 tháng Giêng âm lịch ---
        for (int i = 1; i <= 5; i++) {
            Lunar lunar = new Lunar(year, 1, i); // tháng Giêng âm
            Solar solar = lunar.getSolar();
            LocalDate date = LocalDate.of(solar.getYear(), solar.getMonth(), solar.getDay());
            holidays.add(date);
        }


        // --- Giỗ tổ Hùng Vương: 10 tháng 3 âm lịch ---
        Lunar hungKings = new Lunar(year, 3, 10);
        Solar solarHungKings = hungKings.getSolar();
        holidays.add(LocalDate.of(solarHungKings.getYear(), solarHungKings.getMonth(), solarHungKings.getDay()));


        return holidays;
    }

    public static class Main {
        public static void main(String[] args) {
            int year = LocalDate.now().getYear();

            List<LocalDate> holidays = VietnamHolidayUtils.getVietnamHolidays(year);
            System.out.println("Ngày lễ Việt Nam năm 2025:");
            for (LocalDate date : holidays) {
                System.out.println(date);
            }
        }
    }

}
