package com.oneday.dto;

import javax.validation.constraints.NotBlank;

/**
 * Request DTO for address and temperature lookup.
 */

public class AddressRequest {
    @NotBlank(message = "${error.address-postal-code-required}")
    private String address;

    @NotBlank(message = "${error.address-postal-code-required}")
    private String postalCode;

    public AddressRequest() {}

    public AddressRequest(String address, String postalCode) {
        this.address = address;
        this.postalCode = postalCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
