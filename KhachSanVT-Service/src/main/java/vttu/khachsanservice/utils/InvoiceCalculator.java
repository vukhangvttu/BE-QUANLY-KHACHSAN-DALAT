package vttu.khachsanservice.utils;

import lombok.extern.slf4j.Slf4j;
import vttu.khachsanservice.dto.xuathoadon.ItemInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class InvoiceCalculator {
    // === Model ===
    public static class InvoiceItem {
        private String name;
        private BigDecimal quantity;
        private BigDecimal unitPriceWithVat;
        private BigDecimal totalWithVat;

        private BigDecimal unitPriceWithoutVat;
        private BigDecimal totalWithoutVat;
        private BigDecimal vatAmount;

        public InvoiceItem(String name, BigDecimal quantity, BigDecimal unitPriceWithVat) {
            this.name = name;
            this.quantity = quantity;
            this.unitPriceWithVat = unitPriceWithVat;
            this.totalWithVat = unitPriceWithVat.multiply(quantity);
        }

        public BigDecimal getTotalWithVat() {
            return totalWithVat;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setUnitPriceWithoutVat(BigDecimal unitPriceWithoutVat) {
            this.unitPriceWithoutVat = unitPriceWithoutVat;
        }

        public void setTotalWithoutVat(BigDecimal totalWithoutVat) {
            this.totalWithoutVat = totalWithoutVat;
        }

        public void setVatAmount(BigDecimal vatAmount) {
            this.vatAmount = vatAmount;
        }

        @Override
        public String toString() {
            return name + " | SL: " + quantity +
                    " | Giá có VAT: " + unitPriceWithVat +
                    " | Tổng có VAT: " + totalWithVat +
                    " | Giá chưa VAT: " + unitPriceWithoutVat +
                    " | Tổng chưa VAT: " + totalWithoutVat +
                    " | VAT: " + vatAmount;
        }
    }

    // === Logic ===
    public static List<ItemInfo> calculateWithoutVatFromItemInfo(List<ItemInfo> originalItems, BigDecimal vatRatePercent) {
        BigDecimal vatRate = vatRatePercent.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        BigDecimal onePlusVat = BigDecimal.ONE.add(vatRate);

        for (ItemInfo item : originalItems) {
            BigDecimal totalWithVat = item.getItemTotalAmountWithoutTax(); // hiện là có VAT
            BigDecimal totalWithoutVat = totalWithVat.divide(onePlusVat, 6, RoundingMode.HALF_UP)
                    .setScale(0, RoundingMode.HALF_UP);

            BigDecimal unitPriceWithoutVat = totalWithoutVat.divide(item.getQuantity(), 6, RoundingMode.HALF_UP)
                    .setScale(0, RoundingMode.HALF_UP);

            BigDecimal vatAmount = totalWithVat.subtract(totalWithoutVat)
                    .setScale(0, RoundingMode.HALF_UP);

            item.setItemTotalAmountWithoutTax(totalWithoutVat);
            item.setUnitPrice(unitPriceWithoutVat);
            item.setTaxPercentage(vatAmount);
        }
        return originalItems;
    }

    // === Main ===
    public static void main(String[] args) {
        List<ItemInfo> originalItems = new ArrayList<>();

        ItemInfo item1 = new ItemInfo();
        item1.setItemName("Dịch vụ cho thuê phòng");
        item1.setQuantity(new BigDecimal("2"));
        item1.setItemTotalAmountWithoutTax(new BigDecimal("5800000")); // Tổng CÓ VAT

        ItemInfo item2 = new ItemInfo();
        item2.setItemName("Dịch vụ giặt là");
        item2.setQuantity(new BigDecimal("1"));
        item2.setItemTotalAmountWithoutTax(new BigDecimal("50000")); // Tổng CÓ VAT

        originalItems.add(item1);
        originalItems.add(item2);

        BigDecimal vatRate = new BigDecimal("8");
        BigDecimal totalPayment = new BigDecimal("5850000");

        List<ItemInfo> updatedItems = calculateWithoutVatFromItemInfo(originalItems, vatRate);

        BigDecimal sumWithoutVat = BigDecimal.ZERO;
        BigDecimal sumVat = BigDecimal.ZERO;
        BigDecimal sumWithVat = BigDecimal.ZERO;

        System.out.println("== Chi tiết từng dòng sau khi tính lại ==");
        for (ItemInfo item : updatedItems) {
            System.out.println("Tên DV: " + item.getItemName());
            System.out.println("SL: " + item.getQuantity());
            System.out.println("Tổng có VAT: " + item.getItemTotalAmountWithoutTax().add(item.getTaxPercentage()));
            System.out.println("Tổng chưa VAT: " + item.getItemTotalAmountWithoutTax());
            System.out.println("Đơn giá chưa VAT: " + item.getUnitPrice());
            System.out.println("VAT: " + item.getTaxPercentage());
            System.out.println("------");

            sumWithVat = sumWithVat.add(item.getItemTotalAmountWithoutTax().add(item.getTaxPercentage()));
            sumWithoutVat = sumWithoutVat.add(item.getItemTotalAmountWithoutTax());
            sumVat = sumVat.add(item.getTaxPercentage());
        }

        System.out.println("== Tổng cộng ==");
        System.out.println("Tổng chưa VAT: " + sumWithoutVat);
        System.out.println("Tổng VAT: " + sumVat);
        System.out.println("Tổng có VAT (tính lại): " + sumWithVat);
        log.info("Tổng payment gốc: " + totalPayment);

        if (sumWithVat.compareTo(totalPayment) == 0) {
            log.info("✅ Tổng tiền KHỚP sau khi tính lại VAT.");
        } else {
            log.error("❌ Tổng tiền KHÔNG khớp! Chênh lệch: " + sumWithVat.subtract(totalPayment));
        }
    }


}
