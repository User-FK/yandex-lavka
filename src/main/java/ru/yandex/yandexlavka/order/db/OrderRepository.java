package ru.yandex.yandexlavka.order.db;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends ListCrudRepository<OrderEntity, Long>, PagingAndSortingRepository<OrderEntity, Long> {
    Streamable<OrderEntity> findOrdersByStatus(OrderEntity.Status status);
    List<OrderEntity> findOrdersByCourierIdAndAssignmentDate(Long courierId, LocalDate date);

    Long countByCourierIdAndStatus(Long courierId, OrderEntity.Status status);

    List<OrderEntity> findByAssignmentDate(LocalDate date);
}
