package com.phishware.dto.response;

public record OfficialSeal(boolean official, String brand, String officialDomain) {

    public static OfficialSeal none() {
        return new OfficialSeal(false, null, null);
    }

    public static OfficialSeal of(String brand, String officialDomain) {
        return new OfficialSeal(true, brand, officialDomain);
    }
}
