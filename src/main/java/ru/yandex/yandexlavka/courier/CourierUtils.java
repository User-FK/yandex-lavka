package ru.yandex.yandexlavka.courier;

import ru.yandex.yandexlavka.courier.db.CourierEntity;
import ru.yandex.yandexlavka.order.db.OrderEntity;

public class CourierUtils {
    public static Double getMaxWeight(String courierType) {
        return switch (courierType) {
            case "FOOT" -> 10D;
            case "BIKE" -> 20D;
            case "AUTO" -> 40D;
            default -> 0D;
        };
    }

    public static int getDeliveryTime(String courierType, int orderNum) {
        return switch (courierType) {
            case "FOOT" -> orderNum == 1 ? 25 : 10;
            case "BIKE" -> orderNum == 1 ? 12 : 8;
            case "AUTO" -> orderNum == 1 ? 8 : 4;
            default -> 0;
        };
    }

    public static int getMaxOrdersNum(String courierType) {
        return switch (courierType) {
            case "FOOT" -> 2;
            case "BIKE" -> 4;
            case "AUTO" -> 7;
            default -> 0;
        };
    }

    public static boolean canAccept(CourierEntity courier, OrderEntity order) {
        if (courier.getStatus().equals(CourierEntity.Status.BUSY)) // just in case
            return false;

        if (getMaxWeight(courier.getCourierType()) < order.getWeight())
            return false;

        boolean hasTime = false;

        // consider that these lists are small
        outer: for (var workTime: courier.getWorkTimes()) {
            for (var deliveryTime: order.getDeliveryHours()) {
                if (workTime.getStartTime().isAfter(deliveryTime.getEndTime())
                    || workTime.getEndTime().isBefore(deliveryTime.getStartTime()))
                    continue;

                var common = deliveryTime.getStartTime().isAfter(workTime.getStartTime()) ? deliveryTime.getStartTime() : workTime.getStartTime();
                var end = common.plusMinutes(getDeliveryTime(courier.getCourierType(), 1));

                if (end.isBefore(workTime.getEndTime()) && end.isBefore(deliveryTime.getEndTime())) {
                    hasTime = true;
                    break outer;
                }
            }
        }

        return hasTime;
    }

    public static double getRatingCoeff(String courierType) {
        return switch (courierType) {
            case "FOOT" -> 3d;
            case "BIKE" -> 2d;
            case "AUTO" -> 1d;
            default -> 0d;
        };
    }

    public static double getEarningsCoeff(String courierType) {
        return switch (courierType) {
            case "FOOT" -> 2d;
            case "BIKE" -> 3d;
            case "AUTO" -> 4d;
            default -> 0d;
        };
    }
}
