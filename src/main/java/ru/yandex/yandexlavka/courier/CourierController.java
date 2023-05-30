package ru.yandex.yandexlavka.courier;

import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.yandexlavka.courier.dto.*;
import ru.yandex.yandexlavka.order.dto.OrderAssignResponse;
import ru.yandex.yandexlavka.service.AssignService;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping(value = "/couriers",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class CourierController {
    private final AssignService assignService;
    private final CourierService courierService;

    public CourierController(AssignService assignService, CourierService courierService) {
        this.assignService = assignService;
        this.courierService = courierService;
    }

    @PostMapping
    public ResponseEntity<CreateCouriersResponse> postCouriers(@RequestBody CreateCourierRequest createCourierRequest) {
        log.info("Post request to couriers");
        return new ResponseEntity<>(
                new CreateCouriersResponse(courierService.addCouriers(createCourierRequest.couriers())),
                HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<GetCouriersResponse> getCouriers(@RequestParam(required = false, defaultValue = "1") Long limit,
                                                           @RequestParam(required = false, defaultValue = "0") Long offset) {
        log.info(String.format("Get request to couriers, limit: %d, offset: %d", limit, offset));
        return new ResponseEntity<>(new GetCouriersResponse(courierService.getCouriersDto(limit, offset), limit, offset), HttpStatus.OK);
    }

    @GetMapping("/{courier_id}")
    public ResponseEntity<CourierDto> getCourier(@PathVariable("courier_id") Long courierId) {
        log.info("Get request for single courier id: " + courierId);
        Optional<CourierDto> courierOptional = courierService.getSingleCourier(courierId);
        if (courierOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<CourierDto>(courierOptional.get(), HttpStatus.OK);
        }
    }

    @GetMapping("/meta-info/{courierId}")
    public ResponseEntity<CourierMetaInfoResponse> getCourierMetaInfo(@PathVariable("courierId") Long courierId,
                                                                      @RequestParam("startDate")
                                                                      @DateTimeFormat(pattern = "yyyy-mm-dd") LocalDate startDate,
                                                                      @RequestParam("endDate")
                                                                      @DateTimeFormat(pattern = "yyyy-mm-dd") LocalDate endDate) {
        log.info("Get request to courier meta info");

        if (endDate.isBefore(startDate))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        var responseOptional = courierService.getCouriersMetaInfo(courierId, startDate, endDate);
        if (responseOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(responseOptional.get(), HttpStatus.OK);
        }
    }

    @GetMapping("/assignments")
    public ResponseEntity<OrderAssignResponse> getAssignments(@RequestParam(required = false, value = "courier_id", defaultValue = "-1")
                                                              Long courierId,
                                                              @RequestParam(required = false, value = "date")
                                                              LocalDate date) {
        if (date == null)
            date = LocalDate.now();
        return new ResponseEntity<>(new OrderAssignResponse(date, assignService.getAssignments(courierId, date)), HttpStatus.OK);
    }
}
