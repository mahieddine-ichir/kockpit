package com.accor.wcp.console.services.me;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class MeInfo {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String token;
    private List<String> roles;
}
