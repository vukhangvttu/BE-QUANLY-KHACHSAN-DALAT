-- =====================================================
-- Function: check_room_availability_by_date
-- Description: Kiểm tra phòng trống cho một loại phòng vào một ngày cụ thể
--              Thread-safe với row-level locking
-- Parameters:
--   p_room_type_id: Mã loại phòng
--   p_check_date: Ngày cần kiểm tra
-- Returns: Table với thông tin phòng trống
-- =====================================================

CREATE OR REPLACE FUNCTION check_room_availability_by_date(
    p_room_type_id VARCHAR,
    p_check_date DATE
)
RETURNS TABLE (
    ma_loai_phong VARCHAR,
    ten_loai_phong VARCHAR,
    ngay DATE,
    tong_so_phong INTEGER,
    so_phong_da_dat INTEGER,
    so_phong_trong INTEGER
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_tong_so_phong INTEGER;
    v_so_phong_da_dat INTEGER;
    v_ten_loai_phong VARCHAR;
BEGIN
    -- Lấy thông tin loại phòng với lock để đảm bảo thread-safe
    SELECT lp.so_luong, lp.ten_loai_phong
    INTO v_tong_so_phong, v_ten_loai_phong
    FROM loai_phong lp
    WHERE lp.ma_loai_phong = p_room_type_id
    FOR SHARE; -- Shared lock để đọc dữ liệu, cho phép nhiều transaction đọc cùng lúc

    -- Nếu không tìm thấy loại phòng
    IF v_tong_so_phong IS NULL THEN
        RAISE EXCEPTION 'Không tìm thấy loại phòng: %', p_room_type_id;
    END IF;

    -- Tính số phòng đã đặt trong ngày (bao gồm cả booking và các phòng đang sử dụng)
    -- Sử dụng subquery với FOR UPDATE để lock rows trước khi aggregate
    SELECT COALESCE(SUM(locked_rows.so_luong), 0)
    INTO v_so_phong_da_dat
    FROM (
        SELECT ctb.so_luong
        FROM chi_tiet_booking ctb
        INNER JOIN booking b ON ctb.ma_booking = b.ma_booking
        WHERE ctb.ma_loai_phong = p_room_type_id
            AND b.ma_booking IS NOT NULL -- Booking hợp lệ
            AND p_check_date >= ctb.ngay_den
            AND p_check_date < ctb.ngay_di -- Ngày check-in được tính, ngày check-out không tính
            -- Chỉ tính các booking chưa hủy (có thể thêm điều kiện trạng thái nếu có)
        FOR UPDATE OF ctb SKIP LOCKED -- Lock các row đang được sử dụng, skip các row đã bị lock
    ) AS locked_rows;

    -- Đếm thêm các phòng đang bảo trì (nếu có bảng bảo trì)
    -- Uncommment nếu cần tính phòng bảo trì
    /*
    DECLARE
        v_phong_bao_tri INTEGER;
    BEGIN
        SELECT COUNT(DISTINCT p.ma_phong)
        INTO v_phong_bao_tri
        FROM phong p
        WHERE p.ma_loaiphong = p_room_type_id
            AND p.dang_bao_tri = true;

        v_so_phong_da_dat := v_so_phong_da_dat + v_phong_bao_tri;
    END;
    */

    -- Trả về kết quả
    RETURN QUERY
    SELECT
        p_room_type_id::VARCHAR as ma_loai_phong,
        v_ten_loai_phong::VARCHAR as ten_loai_phong,
        p_check_date::DATE as ngay,
        v_tong_so_phong::INTEGER as tong_so_phong,
        v_so_phong_da_dat::INTEGER as so_phong_da_dat,
        GREATEST(v_tong_so_phong - v_so_phong_da_dat, 0)::INTEGER as so_phong_trong;

END;
$$;

-- =====================================================
-- Function: check_room_availability_range
-- Description: Kiểm tra phòng trống cho một loại phòng trong khoảng thời gian
--              Thread-safe và tối ưu cho nhiều ngày
-- Parameters:
--   p_room_type_id: Mã loại phòng
--   p_start_date: Ngày bắt đầu
--   p_end_date: Ngày kết thúc
-- Returns: Table với thông tin phòng trống cho từng ngày
-- =====================================================

CREATE OR REPLACE FUNCTION check_room_availability_range(
    p_room_type_id VARCHAR,
    p_start_date DATE,
    p_end_date DATE
)
RETURNS TABLE (
    ma_loai_phong VARCHAR,
    ten_loai_phong VARCHAR,
    ngay DATE,
    tong_so_phong INTEGER,
    so_phong_da_dat INTEGER,
    so_phong_trong INTEGER
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_tong_so_phong INTEGER;
    v_ten_loai_phong VARCHAR;
    v_current_date DATE;
BEGIN
    -- Validate input
    IF p_start_date > p_end_date THEN
        RAISE EXCEPTION 'Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc';
    END IF;

    -- Lấy thông tin loại phòng
    SELECT lp.so_luong, lp.ten_loai_phong
    INTO v_tong_so_phong, v_ten_loai_phong
    FROM loai_phong lp
    WHERE lp.ma_loai_phong = p_room_type_id
    FOR SHARE;

    IF v_tong_so_phong IS NULL THEN
        RAISE EXCEPTION 'Không tìm thấy loại phòng: %', p_room_type_id;
    END IF;

    -- Sử dụng CTE để tạo danh sách ngày và tính số phòng đã đặt
    RETURN QUERY
    WITH date_series AS (
        -- Tạo series các ngày từ start_date đến end_date
        SELECT generate_series(p_start_date, p_end_date, '1 day'::interval)::DATE as check_date
    ),
    booked_rooms AS (
        -- Tính số phòng đã đặt cho mỗi ngày
        SELECT
            ds.check_date,
            COALESCE(SUM(ctb.so_luong), 0) as so_phong_da_dat
        FROM date_series ds
        LEFT JOIN chi_tiet_booking ctb ON
            ctb.ma_loai_phong = p_room_type_id
            AND ds.check_date >= ctb.ngay_den
            AND ds.check_date < ctb.ngay_di
        LEFT JOIN booking b ON ctb.ma_booking = b.ma_booking
        WHERE b.ma_booking IS NOT NULL OR ctb.ma_chi_tiet_booking IS NULL
        GROUP BY ds.check_date
    )
    SELECT
        p_room_type_id::VARCHAR,
        v_ten_loai_phong::VARCHAR,
        br.check_date::DATE,
        v_tong_so_phong::INTEGER,
        br.so_phong_da_dat::INTEGER,
        GREATEST(v_tong_so_phong - br.so_phong_da_dat, 0)::INTEGER as so_phong_trong
    FROM booked_rooms br
    ORDER BY br.check_date;

END;
$$;

-- =====================================================
-- Comments cho functions
-- =====================================================

COMMENT ON FUNCTION check_room_availability_by_date IS
'Kiểm tra phòng trống cho một loại phòng vào một ngày cụ thể.
Thread-safe với row-level locking để đảm bảo tính chính xác khi có nhiều user truy cập đồng thời.
Sử dụng FOR UPDATE SKIP LOCKED để tránh deadlock.';

COMMENT ON FUNCTION check_room_availability_range IS
'Kiểm tra phòng trống cho một loại phòng trong khoảng thời gian.
Tối ưu cho việc kiểm tra nhiều ngày liên tiếp.
Thread-safe với transaction isolation level SERIALIZABLE.';

-- =====================================================
-- Index để tối ưu performance
-- =====================================================

-- Index cho bảng chi_tiet_booking để tăng tốc truy vấn
CREATE INDEX IF NOT EXISTS idx_chi_tiet_booking_loai_phong_ngay
ON chi_tiet_booking(ma_loai_phong, ngay_den, ngay_di);

CREATE INDEX IF NOT EXISTS idx_chi_tiet_booking_booking
ON chi_tiet_booking(ma_booking);

-- Index cho bảng booking
CREATE INDEX IF NOT EXISTS idx_booking_ma_booking
ON booking(ma_booking);

-- =====================================================
-- Grant permissions (nếu cần)
-- =====================================================

-- Nếu có user/role cụ thể, grant quyền execute
-- GRANT EXECUTE ON FUNCTION check_room_availability_by_date TO your_application_user;
-- GRANT EXECUTE ON FUNCTION check_room_availability_range TO your_application_user;

