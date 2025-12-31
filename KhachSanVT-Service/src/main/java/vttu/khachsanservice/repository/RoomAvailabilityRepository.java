package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vttu.khachsanservice.dto.RoomAvailabilityDTO;
import vttu.khachsanservice.entity.LoaiPhong;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomAvailabilityRepository extends JpaRepository<LoaiPhong, String> {

    /**
     * Kiểm tra phòng trống cho một loại phòng trong khoảng thời gian với thread-safe
     * Sử dụng PostgreSQL function để đảm bảo tính toán chính xác
     */
    @Query(value = "SELECT * FROM check_room_availability_by_date(:roomTypeId, :checkDate)", nativeQuery = true)
    List<RoomAvailabilityDTO> checkRoomAvailabilityByDate(
            @Param("roomTypeId") String roomTypeId,
            @Param("checkDate") LocalDate checkDate
    );

    /**
     * Kiểm tra phòng trống cho nhiều ngày - thread safe với FOR UPDATE
     */
    @Query(value = "SELECT * FROM check_room_availability_range(:roomTypeId, :startDate, :endDate)", nativeQuery = true)
    List<RoomAvailabilityDTO> checkRoomAvailabilityRange(
            @Param("roomTypeId") String roomTypeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Lock loại phòng để đảm bảo thread-safe khi booking
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT lp FROM LoaiPhong lp WHERE lp.maLoaiPhong = :roomTypeId")
    Optional<LoaiPhong> findByIdWithLock(@Param("roomTypeId") String roomTypeId);
}

