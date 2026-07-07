package vttu.khachsanservice.utils;

import vttu.khachsanservice.config.AES;

public class BaseCode {


    public static void main(String[] args) {

        System.out.println("user  "+ AES.encrypt("postgres","Aa@123456","vukhangvttu"));

        System.out.println("p  "+ AES.encrypt("Khangpostgresql2026@","Aa@123456","vukhangvttu"));

        System.out.println("mst  "+ AES.encrypt("2wsxCDE#","Aa@123456","khangkhongnoi"));

    }
}
