package ru.yandex.yandexlavka.courier.db;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.Objects;

@Entity(name = "work_time")
@Getter
@Setter
@NoArgsConstructor
public class WorkTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "courier_id")
    private Long courierId;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    public WorkTime(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkTime workTime = (WorkTime) o;

        return Objects.equals(id, workTime.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("%s-%s", startTime.toString(), endTime.toString());
    }

    public static WorkTime fromString(String string) {
        var times = string.split("-");

        var workTime = new WorkTime();
        workTime.setStartTime(LocalTime.parse(times[0]));
        workTime.setEndTime(LocalTime.parse(times[1]));

        return workTime;
    }
}
