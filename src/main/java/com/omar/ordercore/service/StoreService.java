package com.omar.ordercore.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StoreService {

    private static final Map<String, String> STORE_COUNTRY_MAP = Map.of(
            "STORE-LIS", "Portugal",
            "STORE-OPO", "Portugal",
            "STORE-MAD", "Spain",
            "STORE-BCN", "Spain",
            "STORE-CPH", "Denmark",
            "STORE-AAR", "Denmark"
    );

    @Cacheable(value = "stores", key = "#storeId")
    public String getCountry(String storeId) {
        return STORE_COUNTRY_MAP.getOrDefault(storeId, "Unknown");
    }
}
