package com.accor.wcp.sample.darkcanary;

import com.accor.wcp.sample.basket.api.model.PhoneNumber;
import com.accor.wcp.sample.kengine.advanced.model.Address;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder(toBuilder = true)
public class CanaryResponse {

    private String firsName;

    private String lastName;

    private String pmid;

    private String email;

    private Address address;

    private PhoneNumber phoneNumber;

    private Integer loyaltyPoints;

    private Map<String, Object> profile;

    static CanaryResponse ofJohnDoe() {
        Address address = new Address();
        address.setCity("Issy les moulineaux");
        address.setZipCode("92120");
        address.setStreet1("1 Rue de Lotre");
        address.setStreet2("Etage d'oeufs");
        PhoneNumber number = new PhoneNumber();
        number.setNumber("0666666666");
        number.setPrefix("+213");

        return CanaryResponse.builder()
                .firsName("John")
                .lastName("Doe")
                .pmid("12345")
                .address(address)
                .loyaltyPoints(100)
                .email("john@doe.com")
                .phoneNumber(number)
                .profile(new HashMap<>())
                .build();
    }
}
