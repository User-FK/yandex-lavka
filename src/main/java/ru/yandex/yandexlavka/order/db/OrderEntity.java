package ru.yandex.yandexlavka.order.db;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(name = "order_table")
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(name = "courier_id")
    private Long courierId; // can be null until the order is distributed

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_pos")
    private Long groupPos;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "region")
    private Long region;

    @Column(name = "cost")
    private Long cost;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "assignment_date")
    private LocalDate assignmentDate;

    @Column(name = "completed_time")
    private LocalDateTime completedTime; // can be null until the order is completed

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private List<DeliveryHoursEntity> deliveryHours = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderEntity that = (OrderEntity) o;

        return Objects.equals(Id, that.Id);
    }

    @Override
    public int hashCode() {
        return Id != null ? Id.hashCode() : 0;
    }

    public enum Status {
        NEW, IN_PROGRESS, COMPLETED
    }
}
