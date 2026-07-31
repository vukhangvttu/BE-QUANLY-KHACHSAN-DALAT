package vttu.khachsanservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity

public class Account {
    @Id
    @Column(nullable = false)
    String userName;
    String passWord;
    int failedAttempts;
    boolean locked;

    @OneToOne(mappedBy = "account")
     NhanVien nhanVien;

    @ManyToMany
    Set<Role> roles;


}
