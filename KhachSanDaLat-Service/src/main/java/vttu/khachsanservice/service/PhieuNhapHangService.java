package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import vttu.khachsanservice.dto.request.NhapHangRequest;
import vttu.khachsanservice.dto.response.NhapHangReponse;
import vttu.khachsanservice.dto.response.TonKhoReponse;
import vttu.khachsanservice.entity.DichVuMienPhi;
import vttu.khachsanservice.entity.NhapHang;
import vttu.khachsanservice.entity.PhieuNhapHang;
import vttu.khachsanservice.entity.TonKho;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.mapper.NhapHangMapper;
import vttu.khachsanservice.mapper.TonKhoMapper;
import vttu.khachsanservice.repository.DichVuMienPhiRepository;
import vttu.khachsanservice.repository.NhapHangRepository;
import vttu.khachsanservice.repository.PhieuNhapHangRepository;
import vttu.khachsanservice.repository.TonKhoRepository;
import vttu.khachsanservice.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PhieuNhapHangService {

    PhieuNhapHangRepository phieuNhapHangRepository;
    NhapHangRepository nhapHangRepository;
    DichVuMienPhiRepository dichVuMienPhiRepository;
    TonKhoRepository tonKhoRepository;
    NhapHangMapper nhapHangMapper;
    TonKhoMapper tonKhoMapper;

    /**
     * Lưu phiếu nhập hàng và cập nhật tồn kho
     * @param requests Danh sách nhập hàng
     * @return PhieuNhapHang đã lưu
     */
    @Transactional
    public PhieuNhapHang savePhieuNhapHang(List<NhapHangRequest> requests) {
        // 1. Kiểm tra danh sách không rỗng
        if (requests == null || requests.isEmpty()) {
            throw new AppException(ErrorCode.NHAP_HANG_REQUEST_EMPTY);
        }

        // 2. Validate từng request và kiểm tra dịch vụ
        List<NhapHang> nhapHangList = new ArrayList<>();
        float tongSoLuong = 0;

        for (NhapHangRequest request : requests) {
            // Validate dữ liệu
            validateNhapHangRequest(request);

            // Kiểm tra dịch vụ miễn phí tồn tại và còn hoạt động
            DichVuMienPhi dichVuMienPhi = dichVuMienPhiRepository
                    .findById(request.getMaDichVu())
                    .orElseThrow(() -> new AppException(ErrorCode.DICH_VU_MIEN_PHI_KHONG_TON_TAI));

            if (dichVuMienPhi.getTrangThai() == null || !dichVuMienPhi.getTrangThai()) {
                throw new AppException(ErrorCode.DICH_VU_MIEN_PHI_BI_KHOA,
                        "Mã: " + request.getMaDichVu());
            }

            // Tạo NhapHang entity
            NhapHang nhapHang = new NhapHang();
            nhapHang.setDichVuMienPhi(dichVuMienPhi);
            nhapHang.setSoLuong(request.getSoLuong());
            nhapHang.setLoai(request.getLoai());
            nhapHang.setGhiChu(request.getGhiChu());

            nhapHangList.add(nhapHang);
            tongSoLuong += request.getSoLuong();
        }

        // 3. Tạo và lưu phiếu nhập hàng
        PhieuNhapHang phieuNhapHang = new PhieuNhapHang();
        phieuNhapHang.setTongSoLuong(tongSoLuong);
        phieuNhapHang.setNgayNhap(LocalDate.now());
        phieuNhapHang.setNguoiTao(SecurityUtil.getUsername());
        phieuNhapHang.setDaXoa(false);
        phieuNhapHang.setThoiGianTao(LocalDateTime.now());

        PhieuNhapHang savedPhieuNhapHang = phieuNhapHangRepository.save(phieuNhapHang);

        // 4. Liên kết và lưu các nhập hàng
        for (NhapHang nhapHang : nhapHangList) {
            nhapHang.setPhieuNhapHang(savedPhieuNhapHang);
        }
        nhapHangRepository.saveAll(nhapHangList);

        // 5. Cập nhật tồn kho
        updateTonKho(nhapHangList);

        log.info("✅ Lưu phiếu nhập hàng thành công - Mã: {}, Tổng SL: {}, Người tạo: {}",
                savedPhieuNhapHang.getMaPhieuNhapHang(), tongSoLuong, SecurityUtil.getUsername());

        return savedPhieuNhapHang;
    }

    /**
     * Validate dữ liệu nhập hàng
     */
    private void validateNhapHangRequest(NhapHangRequest request) {
        if (request.getMaDichVu() == null || request.getMaDichVu().trim().isEmpty()) {
            throw new AppException(ErrorCode.MA_DICH_VU_EMPTY);
        }

        if (request.getSoLuong() == null || request.getSoLuong() <= 0) {
            throw new AppException(ErrorCode.SO_LUONG_NHAP_HANG_INVALID);
        }

    }

    /**
     * Cập nhật số lượng tồn kho sau khi nhập hàng thành công
     */
    private void updateTonKho(List<NhapHang> nhapHangList) {
        for (NhapHang nhapHang : nhapHangList) {
            DichVuMienPhi dichVuMienPhi = nhapHang.getDichVuMienPhi();

            // Tìm tồn kho hiện tại
            TonKho tonKho = tonKhoRepository.findByDichVuMienPhi(dichVuMienPhi)
                    .orElseGet(() -> {
                        // Nếu chưa có tồn kho, tạo mới
                        TonKho newTonKho = new TonKho();
                        newTonKho.setDichVuMienPhi(dichVuMienPhi);
                        newTonKho.setSoLuong(0);
                        return newTonKho;
                    });

            // Cập nhật số lượng tồn kho
            int soLuongMoi = tonKho.getSoLuong() + nhapHang.getSoLuong();
            tonKho.setSoLuong(soLuongMoi);

            tonKhoRepository.save(tonKho);

            log.info("📦 Cập nhật tồn kho - Mã DV: {}, SL cũ: {}, SL nhập: {}, SL mới: {}",
                    dichVuMienPhi.getMaDichVuMienPhi(),
                    tonKho.getSoLuong() - nhapHang.getSoLuong(),
                    nhapHang.getSoLuong(),
                    soLuongMoi);
        }
    }

  public List<PhieuNhapHang> getAllPhieuNhapHang() {
        return phieuNhapHangRepository.findAllActivePhieuNhapHang();
  }

  public List<NhapHangReponse> getAllNhapHangByMaPhieuNhapHang(String maPhieuNhapHang) {

        return nhapHangRepository.findByMaPhieuNhapHang(maPhieuNhapHang)
                .stream().map(nhapHangMapper::toNhapHangReponse).toList();
  }

    /**
     * Xóa phiếu nhập hàng và cập nhật tồn kho
     * Logic: Trừ số lượng tồn kho theo số lượng đã nhập trước đó
     *
     * @param maPhieuNhapHang Mã phiếu nhập hàng cần xóa
     */
    @Transactional
    public void xoaPhieuNhapHang(String maPhieuNhapHang) {
        // 1. Kiểm tra phiếu nhập hàng tồn tại
        PhieuNhapHang phieuNhapHang = phieuNhapHangRepository.findById(maPhieuNhapHang)
                .orElseThrow(() -> new AppException(ErrorCode.PHIEU_NHAP_HANG_KHONG_TON_TAI));

        // 2. Kiểm tra phiếu đã xóa chưa
        if (phieuNhapHang.getDaXoa() != null && phieuNhapHang.getDaXoa()) {
            throw new AppException(ErrorCode.PHIEU_NHAP_HANG_DA_XOA,
                    "Mã: " + maPhieuNhapHang);
        }

        // 3. Lấy danh sách nhập hàng theo mã phiếu nhập hàng
        List<NhapHang> nhapHangList = nhapHangRepository.findByMaPhieuNhapHang(maPhieuNhapHang);

        if (nhapHangList.isEmpty()) {
            log.warn("⚠️ Phiếu nhập hàng {} không có chi tiết nhập hàng", maPhieuNhapHang);
        }

        // 4. Cập nhật tồn kho - trừ số lượng đã nhập
        for (NhapHang nhapHang : nhapHangList) {
            DichVuMienPhi dichVuMienPhi = nhapHang.getDichVuMienPhi();

            // Tìm tồn kho hiện tại
            TonKho tonKho = tonKhoRepository.findByDichVuMienPhi(dichVuMienPhi)
                    .orElseThrow(() -> new AppException(ErrorCode.DICH_VU_MIEN_PHI_KHONG_TON_TAI,
                            "Mã DV: " + dichVuMienPhi.getMaDichVuMienPhi()));

            // Kiểm tra số lượng tồn kho có đủ để trừ không
            int soLuongHienTai = tonKho.getSoLuong();
            int soLuongCanTru = nhapHang.getSoLuong();

            if (soLuongHienTai < soLuongCanTru) {
                throw new AppException(ErrorCode.TON_KHO_KHONG_DU,
                        String.format("Mã DV: %s - Tồn kho hiện tại: %d, Cần trừ: %d",
                                dichVuMienPhi.getMaDichVuMienPhi(),
                                soLuongHienTai,
                                soLuongCanTru));
            }

            // Trừ số lượng tồn kho
            int soLuongMoi = soLuongHienTai - soLuongCanTru;
            tonKho.setSoLuong(soLuongMoi);

            tonKhoRepository.save(tonKho);

            log.info("📦 Cập nhật tồn kho khi xóa - Mã DV: {}, SL cũ: {}, SL trừ: {}, SL mới: {}",
                    dichVuMienPhi.getMaDichVuMienPhi(),
                    soLuongHienTai,
                    soLuongCanTru,
                    soLuongMoi);
        }

        // 5. Đánh dấu phiếu nhập hàng đã xóa
        phieuNhapHang.setDaXoa(true);
        phieuNhapHangRepository.save(phieuNhapHang);

        log.info("🗑️ Xóa phiếu nhập hàng thành công - Mã: {}, Người thực hiện: {}",
                maPhieuNhapHang, SecurityUtil.getUsername());
    }

    /**
     * Update hoặc thêm mới nhập hàng
     * Logic:
     * - Nếu maNhapHang có giá trị → Update (điều chỉnh tồn kho)
     * - Nếu maNhapHang rỗng/null → Thêm mới (tăng tồn kho)
     * - Sau khi xử lý xong → Cập nhật lại tongSoLuong của PhieuNhapHang
     *
     * @param maPhieuNhapHang Mã phiếu nhập hàng
     * @param requests Danh sách NhapHangRequest
     * @return true nếu thành công, false nếu có lỗi
     */
    @Transactional
    public boolean updateOrCreateNhapHang(String maPhieuNhapHang, List<NhapHangRequest> requests) {
        try {
            // Kiểm tra danh sách không rỗng
            if (requests == null || requests.isEmpty()) {
                log.error("❌ Danh sách nhập hàng rỗng");
                return false;
            }

            // Tìm và kiểm tra phiếu nhập hàng
            PhieuNhapHang phieuNhapHang = phieuNhapHangRepository.findById(maPhieuNhapHang)
                    .orElseThrow(() -> new AppException(ErrorCode.PHIEU_NHAP_HANG_KHONG_TON_TAI));

            if (phieuNhapHang.getDaXoa() != null && phieuNhapHang.getDaXoa()) {
                throw new AppException(ErrorCode.PHIEU_NHAP_HANG_DA_XOA);
            }

            // Xử lý từng request
            for (NhapHangRequest request : requests) {
                // Validate dữ liệu đầu vào
                validateNhapHangRequest(request);

                // Kiểm tra maNhapHang để quyết định update hay create
                if (request.getMaNhapHang() == null || request.getMaNhapHang().trim().isEmpty()) {
                    // Thêm mới - liên kết với phiếu nhập hàng
                    createNhapHangWithPhieu(request, phieuNhapHang);
                } else {
                    // Update
                    updateNhapHang(request);
                }
            }

            // Cập nhật lại tổng số lượng của phiếu nhập hàng
            updateTongSoLuongPhieu(phieuNhapHang);

            // Cập nhật lại tồn kho cho từng dịch vụ đã xử lý
            recalculateTonKhoForAffectedServices(phieuNhapHang.getMaPhieuNhapHang());

            log.info("✅ Xử lý {} nhập hàng thành công - Người thực hiện: {}",
                    requests.size(), SecurityUtil.getUsername());

            return true;

        } catch (Exception e) {
            log.error("❌ Lỗi khi xử lý nhập hàng: {}", e.getMessage(), e);

            // Đánh dấu transaction cần rollback - đảm bảo tồn kho KHÔNG được cập nhật
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            return false;
        }
    }

    /**
     * Cập nhật tổng số lượng của phiếu nhập hàng
     */
    private void updateTongSoLuongPhieu(PhieuNhapHang phieuNhapHang) {
        // Lấy tất cả nhập hàng thuộc phiếu này
        List<NhapHang> nhapHangList = nhapHangRepository.findByMaPhieuNhapHang(
                phieuNhapHang.getMaPhieuNhapHang());

        // Tính tổng số lượng
        float tongSoLuong = 0;
        for (NhapHang nhapHang : nhapHangList) {
            tongSoLuong += nhapHang.getSoLuong();
        }

        // Cập nhật vào phiếu
        float soLuongCu = phieuNhapHang.getTongSoLuong();
        phieuNhapHang.setTongSoLuong(tongSoLuong);
        phieuNhapHangRepository.save(phieuNhapHang);

        log.info("📊 Cập nhật tổng số lượng phiếu {} - SL cũ: {}, SL mới: {}",
                phieuNhapHang.getMaPhieuNhapHang(), soLuongCu, tongSoLuong);
    }

    /**
     * Cập nhật lại tồn kho cho tất cả dịch vụ liên quan đến phiếu nhập hàng
     * Tính lại tổng số lượng từ tất cả các bản ghi nhập hàng theo từng dịch vụ
     */
    private void recalculateTonKhoForAffectedServices(String maPhieuNhapHang) {
        // Lấy tất cả nhập hàng thuộc phiếu này
        List<NhapHang> nhapHangList = nhapHangRepository.findByMaPhieuNhapHang(maPhieuNhapHang);

        // Nhóm theo dịch vụ và cập nhật tồn kho
        nhapHangList.stream()
                .map(NhapHang::getDichVuMienPhi)
                .distinct()
                .forEach(this::recalculateTonKhoForService);
    }

    /**
     * Tính lại tồn kho cho một dịch vụ cụ thể
     * Dựa trên tổng số lượng từ TẤT CẢ các bản ghi nhập hàng của dịch vụ đó
     */
    private void recalculateTonKhoForService(DichVuMienPhi dichVuMienPhi) {
        // Lấy tất cả nhập hàng của dịch vụ này từ TẤT CẢ các phiếu
        List<NhapHang> allNhapHangForService = nhapHangRepository.findAll().stream()
                .filter(nh -> nh.getDichVuMienPhi().getMaDichVuMienPhi()
                        .equals(dichVuMienPhi.getMaDichVuMienPhi()))
                .filter(nh -> nh.getPhieuNhapHang() != null)
                .filter(nh -> nh.getPhieuNhapHang().getDaXoa() == null
                        || !nh.getPhieuNhapHang().getDaXoa())
                .toList();

        // Tính tổng số lượng
        int tongSoLuongMoi = allNhapHangForService.stream()
                .mapToInt(NhapHang::getSoLuong)
                .sum();

        // Tìm hoặc tạo tồn kho
        TonKho tonKho = tonKhoRepository.findByDichVuMienPhi(dichVuMienPhi)
                .orElseGet(() -> {
                    TonKho newTonKho = new TonKho();
                    newTonKho.setDichVuMienPhi(dichVuMienPhi);
                    newTonKho.setSoLuong(0);
                    return newTonKho;
                });

        int soLuongCu = tonKho.getSoLuong();
        tonKho.setSoLuong(tongSoLuongMoi);
        tonKhoRepository.save(tonKho);

        log.info("🔄 Tính lại tồn kho - Mã DV: {}, SL cũ: {}, SL mới: {} (Tổng từ {} phiếu nhập)",
                dichVuMienPhi.getMaDichVuMienPhi(),
                soLuongCu,
                tongSoLuongMoi,
                allNhapHangForService.stream()
                        .map(nh -> nh.getPhieuNhapHang().getMaPhieuNhapHang())
                        .distinct()
                        .count());
    }

    /**
     * Thêm mới nhập hàng và liên kết với phiếu
     */
    private NhapHangReponse createNhapHangWithPhieu(NhapHangRequest request, PhieuNhapHang phieuNhapHang) {
        // Kiểm tra dịch vụ miễn phí
        DichVuMienPhi dichVuMienPhi = dichVuMienPhiRepository
                .findById(request.getMaDichVu())
                .orElseThrow(() -> new AppException(ErrorCode.DICH_VU_MIEN_PHI_KHONG_TON_TAI));

        if (dichVuMienPhi.getTrangThai() == null || !dichVuMienPhi.getTrangThai()) {
            throw new AppException(ErrorCode.DICH_VU_MIEN_PHI_BI_KHOA,
                    "Mã: " + request.getMaDichVu());
        }

        // Tạo NhapHang entity
        NhapHang nhapHang = new NhapHang();
        nhapHang.setDichVuMienPhi(dichVuMienPhi);
        nhapHang.setSoLuong(request.getSoLuong());
        nhapHang.setLoai(request.getLoai());
        nhapHang.setGhiChu(request.getGhiChu());
        nhapHang.setPhieuNhapHang(phieuNhapHang);  // Liên kết với phiếu

        NhapHang savedNhapHang = nhapHangRepository.save(nhapHang);

        // Cập nhật tồn kho - tăng số lượng
        updateTonKhoForCreate(dichVuMienPhi, request.getSoLuong());

        log.info("✅ Thêm mới nhập hàng - Mã: {}, Dịch vụ: {}, SL: {}, Phiếu: {}, Người tạo: {}",
                savedNhapHang.getMaNhapHang(),
                dichVuMienPhi.getMaDichVuMienPhi(),
                request.getSoLuong(),
                phieuNhapHang.getMaPhieuNhapHang(),
                SecurityUtil.getUsername());

        return nhapHangMapper.toNhapHangReponse(savedNhapHang);
    }

    /**
     * Thêm mới nhập hàng đơn lẻ
     */
    private NhapHangReponse createNhapHang(NhapHangRequest request) {
        // Kiểm tra dịch vụ miễn phí
        DichVuMienPhi dichVuMienPhi = dichVuMienPhiRepository
                .findById(request.getMaDichVu())
                .orElseThrow(() -> new AppException(ErrorCode.DICH_VU_MIEN_PHI_KHONG_TON_TAI));

        if (dichVuMienPhi.getTrangThai() == null || !dichVuMienPhi.getTrangThai()) {
            throw new AppException(ErrorCode.DICH_VU_MIEN_PHI_BI_KHOA,
                    "Mã: " + request.getMaDichVu());
        }

        // Tạo NhapHang entity
        NhapHang nhapHang = new NhapHang();
        nhapHang.setDichVuMienPhi(dichVuMienPhi);
        nhapHang.setSoLuong(request.getSoLuong());
        nhapHang.setLoai(request.getLoai());
        nhapHang.setGhiChu(request.getGhiChu());
        // phieuNhapHang sẽ được set sau nếu cần

        NhapHang savedNhapHang = nhapHangRepository.save(nhapHang);

        // Cập nhật tồn kho - tăng số lượng
        updateTonKhoForCreate(dichVuMienPhi, request.getSoLuong());

        log.info("✅ Thêm mới nhập hàng - Mã: {}, Dịch vụ: {}, SL: {}, Người tạo: {}",
                savedNhapHang.getMaNhapHang(),
                dichVuMienPhi.getMaDichVuMienPhi(),
                request.getSoLuong(),
                SecurityUtil.getUsername());

        return nhapHangMapper.toNhapHangReponse(savedNhapHang);
    }

    /**
     * Update nhập hàng đã tồn tại
     */
    private NhapHangReponse updateNhapHang(NhapHangRequest request) {
        // Parse maNhapHang
        long maNhapHang;
        try {
            maNhapHang = Long.parseLong(request.getMaNhapHang().trim());
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.NHAP_HANG_KHONG_TON_TAI,
                    "Mã nhập hàng không hợp lệ: " + request.getMaNhapHang());
        }

        // Tìm nhập hàng hiện tại
        NhapHang nhapHang = nhapHangRepository.findById(maNhapHang)
                .orElseThrow(() -> new AppException(ErrorCode.NHAP_HANG_KHONG_TON_TAI,
                        "Mã: " + maNhapHang));

        // Kiểm tra phiếu nhập hàng có bị xóa không (trước khi update tồn kho)
        if (nhapHang.getPhieuNhapHang() != null) {
            PhieuNhapHang phieuNhapHang = nhapHang.getPhieuNhapHang();
            if (phieuNhapHang.getDaXoa() != null && phieuNhapHang.getDaXoa()) {
                throw new AppException(ErrorCode.NHAP_HANG_DA_XOA,
                        String.format("Mã nhập hàng: %d - Phiếu nhập hàng %s đã bị xóa",
                                maNhapHang, phieuNhapHang.getMaPhieuNhapHang()));
            }
        }

        // Lưu số lượng cũ để điều chỉnh tồn kho
        int soLuongCu = nhapHang.getSoLuong();
        DichVuMienPhi dichVuMienPhiCu = nhapHang.getDichVuMienPhi();

        // Kiểm tra nếu thay đổi dịch vụ
        boolean thayDoiDichVu = !dichVuMienPhiCu.getMaDichVuMienPhi().equals(request.getMaDichVu());
        DichVuMienPhi dichVuMienPhiMoi = null;

        if (thayDoiDichVu) {
            // Tìm dịch vụ mới
            dichVuMienPhiMoi = dichVuMienPhiRepository
                    .findById(request.getMaDichVu())
                    .orElseThrow(() -> new AppException(ErrorCode.DICH_VU_MIEN_PHI_KHONG_TON_TAI));

            if (dichVuMienPhiMoi.getTrangThai() == null || !dichVuMienPhiMoi.getTrangThai()) {
                throw new AppException(ErrorCode.DICH_VU_MIEN_PHI_BI_KHOA,
                        "Mã: " + request.getMaDichVu());
            }
        }

        // Cập nhật tồn kho
        if (thayDoiDichVu) {
            // Trừ tồn kho dịch vụ cũ
            updateTonKhoForUpdate(dichVuMienPhiCu, soLuongCu, 0);
            // Cộng tồn kho dịch vụ mới
            updateTonKhoForUpdate(dichVuMienPhiMoi, 0, request.getSoLuong());

            nhapHang.setDichVuMienPhi(dichVuMienPhiMoi);
        } else {
            // Cùng dịch vụ, điều chỉnh số lượng
            updateTonKhoForUpdate(dichVuMienPhiCu, soLuongCu, request.getSoLuong());
        }

        // Update nhập hàng
        nhapHang.setSoLuong(request.getSoLuong());
        nhapHang.setLoai(request.getLoai());
        nhapHang.setGhiChu(request.getGhiChu());

        NhapHang updatedNhapHang = nhapHangRepository.save(nhapHang);

        log.info("🔄 Cập nhật nhập hàng - Mã: {}, SL cũ: {}, SL mới: {}, Người cập nhật: {}",
                maNhapHang, soLuongCu, request.getSoLuong(), SecurityUtil.getUsername());

        return nhapHangMapper.toNhapHangReponse(updatedNhapHang);
    }

    /**
     * Cập nhật tồn kho khi thêm mới (chỉ tăng)
     */
    private void updateTonKhoForCreate(DichVuMienPhi dichVuMienPhi, int soLuongThem) {
        TonKho tonKho = tonKhoRepository.findByDichVuMienPhi(dichVuMienPhi)
                .orElseGet(() -> {
                    TonKho newTonKho = new TonKho();
                    newTonKho.setDichVuMienPhi(dichVuMienPhi);
                    newTonKho.setSoLuong(0);
                    return newTonKho;
                });

        int soLuongMoi = tonKho.getSoLuong() + soLuongThem;
        tonKho.setSoLuong(soLuongMoi);
        tonKhoRepository.save(tonKho);

        log.info("📦 Cập nhật tồn kho (thêm mới) - Mã DV: {}, SL cũ: {}, SL thêm: {}, SL mới: {}",
                dichVuMienPhi.getMaDichVuMienPhi(),
                tonKho.getSoLuong() - soLuongThem,
                soLuongThem,
                soLuongMoi);
    }

    /**
     * Cập nhật tồn kho khi update (điều chỉnh delta)
     */
    private void updateTonKhoForUpdate(DichVuMienPhi dichVuMienPhi, int soLuongCu, int soLuongMoi) {
        TonKho tonKho = tonKhoRepository.findByDichVuMienPhi(dichVuMienPhi)
                .orElseThrow(() -> new AppException(ErrorCode.DICH_VU_MIEN_PHI_KHONG_TON_TAI,
                        "Mã DV: " + dichVuMienPhi.getMaDichVuMienPhi()));

        // Tính delta: nếu tăng số lượng → cộng, nếu giảm → trừ
        int delta = soLuongMoi - soLuongCu;
        int soLuongHienTai = tonKho.getSoLuong();

        // Nếu giảm số lượng, kiểm tra tồn kho đủ không
        if (delta < 0 && soLuongHienTai < Math.abs(delta)) {
            throw new AppException(ErrorCode.TON_KHO_KHONG_DU,
                    String.format("Mã DV: %s - Tồn kho: %d, Cần trừ: %d",
                            dichVuMienPhi.getMaDichVuMienPhi(),
                            soLuongHienTai,
                            Math.abs(delta)));
        }

        int soLuongKetQua = soLuongHienTai + delta;
        tonKho.setSoLuong(soLuongKetQua);
        tonKhoRepository.save(tonKho);

        log.info("📦 Cập nhật tồn kho (update) - Mã DV: {}, SL cũ: {}, Delta: {}, SL mới: {}",
                dichVuMienPhi.getMaDichVuMienPhi(),
                soLuongHienTai,
                delta > 0 ? "+" + delta : delta,
                soLuongKetQua);
    }


    public List<Map<String, Object>> getAllTonKho() {
        return tonKhoRepository.getTonKho();
    }
    public List<Map<String, Object>> getThongKeNhapXuat(LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        return tonKhoRepository.getThongKeNhapXuatPhong(ngayBatDau, ngayKetThuc);
    }
}
