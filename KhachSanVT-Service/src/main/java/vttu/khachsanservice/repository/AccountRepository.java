package vttu.khachsanservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vttu.khachsanservice.entity.Account;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {

    // check locked tài khoản xem tài khoản có bị khóa không

//    @Query(value = "select  * from account where  user_name=:tentaikhoan", nativeQuery = true)
//    Object checkLockedAccount(String tentaikhoan);

    @Query("SELECT u.locked FROM Account u WHERE u.userName = :username")
    Boolean checkLockedAccount(@Param("username") String username);

   Optional<Account> findByUserName(String username);
}
