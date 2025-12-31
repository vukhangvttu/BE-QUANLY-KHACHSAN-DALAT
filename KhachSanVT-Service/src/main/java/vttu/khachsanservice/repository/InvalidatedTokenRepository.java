package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vttu.khachsanservice.entity.InvalidatedToken;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {

//    @Query(value = "SELECT COUNT(*) > 0 FROM invalidated_token WHERE id = :id AND logged_out = true", nativeQuery = true)
    boolean existsByIdAndLoggedOut(String id, boolean loggedOut);

    boolean existsByIdAndTokenExpired(String id, boolean tokenExpired);
}
