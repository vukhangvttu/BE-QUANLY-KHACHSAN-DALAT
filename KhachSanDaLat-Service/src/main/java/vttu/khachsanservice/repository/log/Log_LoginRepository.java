package vttu.khachsanservice.repository.log;

import org.springframework.data.jpa.repository.JpaRepository;
import vttu.khachsanservice.entity.log.Log_Login;

public interface Log_LoginRepository extends JpaRepository<Log_Login, Integer> {
}
