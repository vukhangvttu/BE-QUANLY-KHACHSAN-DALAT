package vttu.khachsanservice.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateConverter {
    public static String converDate(LocalDate date){
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(outputFormatter);
    }
}
