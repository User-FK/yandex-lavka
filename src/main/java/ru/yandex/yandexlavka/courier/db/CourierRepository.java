package ru.yandex.yandexlavka.courier.db;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface CourierRepository extends ListCrudRepository<CourierEntity, Long>, PagingAndSortingRepository<CourierEntity, Long> {
    @Query(nativeQuery = true, value = "select count(*) from order_table where (status = 'COMPLETED' " +
            "and courier_id = :courier_id " +
            "and completed_time between :start and :end)")
    Long countCompleted(@Param("courier_id") Long courierId,
                        @Param("start") LocalDate start,
                        @Param("end") LocalDate end);

    @Query(nativeQuery = true, value = "select sum(cost) from order_table where (status = 'COMPLETED' " +
            "and courier_id = :courier_id " +
            "and completed_time between :start and :end)")
    Long sumEarningsNullable(@Param("courier_id") Long courierId,
                        @Param("start") LocalDate start,
                        @Param("end") LocalDate end);

    default Long sumEarnings(Long courierId, LocalDate start, LocalDate end) {
        var res = sumEarningsNullable(courierId, start, end);
        return res == null ? 0 : res;
    }

    Streamable<CourierEntity> findCouriersByStatus(CourierEntity.Status status);
}
