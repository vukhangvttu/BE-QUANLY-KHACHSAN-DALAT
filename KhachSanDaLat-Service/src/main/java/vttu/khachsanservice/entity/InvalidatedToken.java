package vttu.khachsanservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class InvalidatedToken {
    @Id
    String id;
    Date expirytime;

    @Column(columnDefinition = "boolean default false")
    boolean loggedOut;

    @Column(columnDefinition = "boolean default false")
    boolean tokenExpired;
}
