package com.example.backend.service;

import com.example.backend.config.AppProperties;
import com.example.backend.dto.ShippingQuoteResponse;
import com.example.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShippingService {
    private final AppProperties properties;

    public ShippingQuoteResponse quote(Double destinationLatitude, Double destinationLongitude, User user) {
        double distance = distanceKm(destinationLatitude, destinationLongitude);
        String membership = user.getMembership() == null ? "MEMBER" : user.getMembership().getMemberName();
        boolean diamond = "DIAMOND".equalsIgnoreCase(membership);
        int fee = diamond ? 0 : (int) Math.round(properties.getShipping().getBaseFee() + distance * properties.getShipping().getPerKmFee());
        return new ShippingQuoteResponse(round(distance), fee, membership, membershipDiscountPercent(membership), diamond);
    }

    public int membershipDiscountPercent(String membership) {
        if ("DIAMOND".equalsIgnoreCase(membership)) {
            return 5;
        }
        if ("VIP".equalsIgnoreCase(membership)) {
            return 2;
        }
        return 0;
    }

    private double distanceKm(Double destinationLatitude, Double destinationLongitude) {
        if (destinationLatitude == null || destinationLongitude == null) {
            return 0;
        }
        double earthRadiusKm = 6371.0;
        double lat1 = Math.toRadians(properties.getShipping().getOriginLatitude());
        double lat2 = Math.toRadians(destinationLatitude);
        double deltaLat = Math.toRadians(destinationLatitude - properties.getShipping().getOriginLatitude());
        double deltaLng = Math.toRadians(destinationLongitude - properties.getShipping().getOriginLongitude());
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
