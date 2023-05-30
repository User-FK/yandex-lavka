package ru.yandex.yandexlavka.service;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.yandexlavka.courier.CourierUtils;
import ru.yandex.yandexlavka.courier.db.CourierEntity;
import ru.yandex.yandexlavka.courier.db.CourierRepository;
import ru.yandex.yandexlavka.courier.db.Region;
import ru.yandex.yandexlavka.courier.db.WorkTime;
import ru.yandex.yandexlavka.courier.dto.CourierDto;
import ru.yandex.yandexlavka.courier.dto.CreateCourierDto;
import ru.yandex.yandexlavka.order.db.DeliveryHoursEntity;
import ru.yandex.yandexlavka.order.db.OrderEntity;
import ru.yandex.yandexlavka.order.db.OrderRepository;
import ru.yandex.yandexlavka.order.dto.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// This class is not annotated with service to have different implementations in the Configuration class

@Slf4j
public class SimpleAssignService implements AssignService {
    private final Set<OrderEntity> newOrders = new HashSet<>(); // set of new orders
    private final Set<CourierEntity> freeCouriers = new HashSet<>(); // set of free couriers

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final Function<CourierEntity, CourierDto> courierEntityDtoMapper;
    private final Function<OrderEntity, OrderDto> orderEntityDtoMapper;

    public SimpleAssignService(OrderRepository orderRepository,
                               CourierRepository courierRepository,
                               Function<CourierEntity, CourierDto> courierEntityDtoMapper,
                               Function<OrderEntity, OrderDto> orderEntityDtoMapper) {
        this.orderRepository = orderRepository;
        this.courierRepository = courierRepository;
        this.courierEntityDtoMapper = courierEntityDtoMapper;
        this.orderEntityDtoMapper = orderEntityDtoMapper;
    }

    public void init() {
        // should work no matter of collection impl
        newOrders.addAll(orderRepository.findOrdersByStatus(OrderEntity.Status.NEW).stream().toList());
        log.info("New orders num after init: " + newOrders.size());

        freeCouriers.addAll(courierRepository.findCouriersByStatus(CourierEntity.Status.FREE).stream().toList());
        log.info("Free couriers num after init: " + freeCouriers.size());
    }

    @Override
    public List<CourierGroupOrder> assign() {
        init();

        var res = new ArrayList<CourierGroupOrder>();
        var currentDate = LocalDate.now();

        var couriers = freeCouriers.stream()
                .sorted(Comparator.comparingInt(courier -> CourierUtils.getMaxOrdersNum(courier.getCourierType())))
                .toList();

        for (var courier: couriers) {
            var freeTimes = courier.getWorkTimes().stream()
                    .map(workTime -> new FreeTime(workTime.getStartTime(), workTime.getEndTime()))
                    .collect(Collectors.toCollection(HashSet<FreeTime>::new));

            var orders = newOrders.stream()
                    .filter(order -> CourierUtils.canAccept(courier, order))
                    .sorted(Comparator.comparingDouble(OrderEntity::getCost))
                    .collect(Collectors.toCollection(HashSet<OrderEntity>::new));

            var group = new ArrayList<OrderEntity>();
            var maxSize = CourierUtils.getMaxOrdersNum(courier.getCourierType());

            var ordersIter = orders.iterator();
            while (ordersIter.hasNext()){
                var order = ordersIter.next();

                var deliveryIter = order.getDeliveryHours().iterator();
                timesLoop: while (deliveryIter.hasNext() && group.size() < maxSize) {
                    var deliveryTime = deliveryIter.next();

                    var freeTimeIter = freeTimes.iterator();
                    while (freeTimeIter.hasNext()) {
                        var freeTime = freeTimeIter.next();
                        var minutesToDeliver = CourierUtils.getDeliveryTime(courier.getCourierType(), group.size() + 1);
                        if (!deliveryTime.getStartTime().isBefore(freeTime.start())
                                && !deliveryTime.getStartTime().plusMinutes(minutesToDeliver).isAfter(freeTime.end())) {
                            freeTimeIter.remove();

                            var left = new FreeTime(freeTime.start(), deliveryTime.getStartTime());
                            var right = new FreeTime(deliveryTime.getStartTime().plusMinutes(minutesToDeliver), freeTime.end());

                            if (left.minutesDuration() >= minutesToDeliver)
                                freeTimes.add(left);
                            if (right.minutesDuration() >= minutesToDeliver)
                                freeTimes.add(right);

                            group.add(order);
                            ordersIter.remove();
                            break timesLoop;
                        } else if (!freeTime.start().isBefore(deliveryTime.getStartTime())
                                && !freeTime.start().plusMinutes(CourierUtils.getDeliveryTime(courier.getCourierType(), group.size())).isAfter(deliveryTime.getEndTime())) {
                            freeTimeIter.remove();

                            var right = new FreeTime(freeTime.start().plusMinutes(minutesToDeliver), freeTime.end());
                            if (right.minutesDuration() >= minutesToDeliver)
                                freeTimes.add(right);

                            group.add(order);
                            ordersIter.remove();
                            break timesLoop;
                        }
                    }
                }
            }

            if (group.isEmpty())
                continue;

            log.info(String.format("Found %d orders for courier %d", group.size(), courier.getId()));

            freeCouriers.remove(courier);
            courier.setStatus(CourierEntity.Status.BUSY);

            Long groupId = group.get(0).getId();
            for (int i = 0; i < group.size(); ++i) {
                var order = group.get(i);
                newOrders.remove(order);

                order.setCourierId(courier.getId());
                order.setStatus(OrderEntity.Status.IN_PROGRESS);
                order.setAssignmentDate(currentDate);
                order.setGroupId(groupId);
                order.setGroupPos((long) i + 1);

                orderRepository.save(order);
            }

            res.add(new CourierGroupOrder(courier.getId(), Collections.singletonList(new GroupOrders(groupId, group.stream().map(orderEntityDtoMapper).toList()))));
            courierRepository.save(courier);
        }

        return res;
    }

    @Override
    public List<CourierGroupOrder> getAssignments(Long courierId, LocalDate date) {
        if (courierId == -1) {
            var ordersByCouriers = orderRepository.findByAssignmentDate(date).stream()
                    .collect(Collectors.groupingBy(OrderEntity::getCourierId, Collectors.mapping(ord -> ord, Collectors.toList())));

            var res = new ArrayList<CourierGroupOrder>();

            for (var couriersEntry: ordersByCouriers.entrySet()) {
                var map = couriersEntry.getValue().stream()
                        .collect(Collectors.groupingBy(OrderEntity::getGroupId, Collectors.mapping(ord -> ord, Collectors.toList())));

                var groups = new ArrayList<GroupOrders>();
                for (var entry: map.entrySet())
                    groups.add(new GroupOrders(entry.getKey(), entry.getValue().stream().map(orderEntityDtoMapper).toList()));

                res.add(new CourierGroupOrder(couriersEntry.getKey(), groups));
            }

            return res;
        } else {
            var map = orderRepository.findOrdersByCourierIdAndAssignmentDate(courierId, date).stream()
                    .collect(Collectors.groupingBy(OrderEntity::getGroupId, Collectors.mapping(ord -> ord, Collectors.toList())));

            var groups = new ArrayList<GroupOrders>();
            for (var entry: map.entrySet())
                groups.add(new GroupOrders(entry.getKey(), entry.getValue().stream().map(orderEntityDtoMapper).toList()));

            return List.of(new CourierGroupOrder(courierId, groups));
        }
    }

    @Override
    public List<OrderDto> complete(List<CompleteOrderDto> toComplete) {
        var completed = new ArrayList<OrderDto>();

        for (var reqOrder: toComplete) {
            var orderOptional = orderRepository.findById(reqOrder.orderId());
            if (orderOptional.isEmpty())
                continue;

            var order = orderOptional.get();
            if (order.getStatus().equals(OrderEntity.Status.COMPLETED)) {
                completed.add(orderEntityDtoMapper.apply(order));
                continue;
            }

            if (!order.getStatus().equals(OrderEntity.Status.IN_PROGRESS))
                continue;

            var courierOptional = courierRepository.findById(reqOrder.courierId());
            if (courierOptional.isEmpty())
                continue;

            var courier = courierOptional.get();
            if (!courier.getStatus().equals(CourierEntity.Status.BUSY))
                continue;

            if (!courier.getId().equals(order.getCourierId()))
                continue;

            if (orderRepository.countByCourierIdAndStatus(courier.getId(), OrderEntity.Status.IN_PROGRESS) == 1)
                courier.setStatus(CourierEntity.Status.FREE);

            order.setCompletedTime(reqOrder.completeTime());
            order.setStatus(OrderEntity.Status.COMPLETED);

            completed.add(orderEntityDtoMapper.apply(order));

            orderRepository.save(order);
            courierRepository.save(courier);
        }

        return completed;
    }

    private record FreeTime(LocalTime start, LocalTime end) {
        public long minutesDuration() {
            if (start.isAfter(end))
                return 0;
            return Duration.between(start, end).toMinutes();
        }
    }
}
