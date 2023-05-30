package ru.yandex.yandexlavka.courier;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.yandexlavka.courier.db.CourierEntity;
import ru.yandex.yandexlavka.courier.db.CourierRepository;
import ru.yandex.yandexlavka.courier.db.Region;
import ru.yandex.yandexlavka.courier.db.WorkTime;
import ru.yandex.yandexlavka.courier.dto.CourierDto;
import ru.yandex.yandexlavka.courier.dto.CourierMetaInfoResponse;
import ru.yandex.yandexlavka.courier.dto.CreateCourierDto;
import ru.yandex.yandexlavka.repository.ChunkRequest;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CourierService {
    private final CourierRepository courierRepository;
    private final Function<CourierEntity, CourierDto> courierEntityDtoMapper;
    private final CourierMapper courierMapper = new CourierMapper();

    public CourierService(CourierRepository courierRepository,
                          @Qualifier("courierEntityDtoMapper") Function<CourierEntity, CourierDto> courierEntityDtoMapper) {
        this.courierRepository = courierRepository;
        this.courierEntityDtoMapper = courierEntityDtoMapper;
    }

    public List<CourierDto> addCouriers(List<CreateCourierDto> couriersDto) {
        List<CourierEntity> couriers = couriersDto.stream().map(courierMapper).collect(Collectors.toCollection(ArrayList<CourierEntity>::new)); // WARNING: should collect to modifiable list
        couriers = courierRepository.saveAll(couriers);

        return couriers.stream().map(courierEntityDtoMapper).toList();
    }

    public List<CourierDto> getCouriersDto(Long limit, Long offset) {
        //return courierRepository.findAll().stream().map(courierEntityDtoMapper).collect(Collectors.toList());
        return courierRepository.findAll(new ChunkRequest(offset.intValue(), limit.intValue(), Sort.by("id")))
                .stream().map(courierEntityDtoMapper).collect(Collectors.toList());
    }

    public Optional<CourierDto> getSingleCourier(Long courierId) {
        var courierEntityOptional = courierRepository.findById(courierId);
        return courierEntityOptional.map(courierEntityDtoMapper);
    }

    public Optional<CourierMetaInfoResponse> getCouriersMetaInfo(Long courierId, LocalDate startDate, LocalDate endDate) {
        var courierOptional = courierRepository.findById(courierId);
        if (courierOptional.isEmpty())
            return Optional.empty();

        var courier = courierOptional.get();
        long hoursBetween = Period.between(startDate, endDate).getDays() * 24L;

        Double rating = CourierUtils.getRatingCoeff(courier.getCourierType()) * courierRepository.countCompleted(courierId, startDate, endDate) / hoursBetween;
        Double earnings = CourierUtils.getEarningsCoeff(courier.getCourierType()) * courierRepository.sumEarnings(courierId, startDate, endDate).doubleValue();

        log.info("Courier id: " + courierId);
        log.info("Rating: " + rating);
        log.info("Earnings: " + earnings);

        return Optional.of(new CourierMetaInfoResponse(
                courier.getId(),
                courier.getCourierType(),
                courier.getRegions().stream().map(Region::getId).collect(Collectors.toCollection(ArrayList<Long>::new)),
                courier.getWorkTimes().stream().map(WorkTime::toString).collect(Collectors.toCollection(ArrayList<String>::new)),
                rating,
                earnings
        ));
    }

    private static class CourierMapper implements Function<CreateCourierDto, CourierEntity> {
        @Override
        public CourierEntity apply(CreateCourierDto createCourierDto) {
            var courier = new CourierEntity();

            courier.setCourierType(createCourierDto.courierType());
            courier.setStatus(CourierEntity.Status.FREE);
            courier.setRegions(createCourierDto.regions().stream().map(Region::new)
                    .collect(Collectors.toCollection(ArrayList<Region>::new)));

            courier.setWorkTimes(createCourierDto.workingHours().stream().map(WorkTime::fromString)
                    .collect(Collectors.toCollection(ArrayList<WorkTime>::new)));

            return courier;
        }
    }
}
