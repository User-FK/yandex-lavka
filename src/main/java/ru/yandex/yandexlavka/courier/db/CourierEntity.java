package ru.yandex.yandexlavka.courier.db;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// There won't be special class for service level, entities can also handle simple business logic
// For simplicity there is no polymorphic classes for each courier type

@Entity(name = "courier")
@Getter
@Setter
@NoArgsConstructor
public class CourierEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "courier_type")
    private String courierType;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status = Status.FREE;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER) // TODO: fix to make it lazy
    @JoinColumn(name = "courier_id")
    private List<Region> regions = new ArrayList<>();

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "courier_id")
    private List<WorkTime> workTimes = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CourierEntity that = (CourierEntity) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public enum Status {
        FREE, BUSY
    }
}
