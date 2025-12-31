-- =====================================================
-- SCRIPT TẠO DATABASE FUNCTIONS CHO KIỂM TRA PHÒNG TRỐNG
-- File: create_room_availability_functions.sql
-- Description: Tạo các functions để kiểm tra phòng trống thread-safe
-- Author: System
-- Date: 2025-12-27
-- =====================================================

-- Kết nối database
\c QL-KHACH-SAN-VT;

BEGIN;

-- =====================================================
-- 1. DROP CÁC FUNCTIONS CŨ (NẾU TỒN TẠI)
-- =====================================================

DROP FUNCTION IF EXISTS check_room_availability_by_date(VARCHAR, DATE);
DROP FUNCTION IF EXISTS check_room_availability_range(VARCHAR, DATE, DATE);

-- =====================================================
-- 2. TẠO FUNCTION: check_room_availability_by_date
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
    FOR SHARE; -- Shared lock để đọc dữ liệu

    -- Nếu không tìm thấy loại phòng
    IF v_tong_so_phong IS NULL THEN
        RAISE EXCEPTION 'Không tìm thấy loại phòng: %', p_room_type_id;
    END IF;

    -- Tính số phòng đã đặt trong ngày
    -- Sử dụng subquery với FOR UPDATE để lock trước khi aggregate
    SELECT COALESCE(SUM(locked_rows.so_luong), 0)
    INTO v_so_phong_da_dat
    FROM (
        SELECT ctb.so_luong
        FROM chi_tiet_booking ctb
        INNER JOIN booking b ON ctb.ma_booking = b.ma_booking
        WHERE ctb.ma_loai_phong = p_room_type_id
            AND b.ma_booking IS NOT NULL
            AND p_check_date >= ctb.ngay_den
            AND p_check_date < ctb.ngay_di
        FOR UPDATE OF ctb SKIP LOCKED
    ) AS locked_rows;

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
-- 3. TẠO FUNCTION: check_room_availability_range
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

    -- Tính số phòng đã đặt cho từng ngày trong khoảng
    -- Không sử dụng FOR UPDATE với aggregate function
    RETURN QUERY
    WITH date_series AS (
        SELECT generate_series(p_start_date, p_end_date, '1 day'::interval)::DATE as check_date
    ),
    booked_rooms AS (
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
-- 4. TẠO INDEXES ĐỂ TỐI ƯU PERFORMANCE
-- =====================================================

-- Index cho chi_tiet_booking
CREATE INDEX IF NOT EXISTS idx_chi_tiet_booking_loai_phong_ngay
ON chi_tiet_booking(ma_loai_phong, ngay_den, ngay_di);

CREATE INDEX IF NOT EXISTS idx_chi_tiet_booking_booking
ON chi_tiet_booking(ma_booking);

-- Index cho booking
CREATE INDEX IF NOT EXISTS idx_booking_ma_booking
ON booking(ma_booking);

-- =====================================================
-- 5. THÊM COMMENTS
-- =====================================================

COMMENT ON FUNCTION check_room_availability_by_date IS
'Kiểm tra phòng trống cho một loại phòng vào một ngày cụ thể.
Thread-safe với row-level locking để đảm bảo tính chính xác khi có nhiều user truy cập đồng thời.';

COMMENT ON FUNCTION check_room_availability_range IS
'Kiểm tra phòng trống cho một loại phòng trong khoảng thời gian.
Tối ưu cho việc kiểm tra nhiều ngày liên tiếp.';

COMMIT;

-- =====================================================
-- 6. TEST FUNCTIONS
-- =====================================================

-- Test function check_room_availability_by_date
-- SELECT * FROM check_room_availability_by_date('CS-C-D', '2025-12-26');

-- Test function check_room_availability_range
-- SELECT * FROM check_room_availability_range('CS-C-D', '2025-12-26', '2025-12-28');

-- =====================================================
-- KẾT THÚC SCRIPT
-- =====================================================

\echo 'Đã tạo thành công các functions kiểm tra phòng trống!'
\echo 'Có thể test bằng các câu lệnh:'
\echo '  SELECT * FROM check_room_availability_by_date(''CS-C-D'', ''2025-12-26'');'
\echo '  SELECT * FROM check_room_availability_range(''CS-C-D'', ''2025-12-26'', ''2025-12-28'');'

