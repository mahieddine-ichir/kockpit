package org.kockpit.sample.api.audit;

import lombok.Data;

import java.util.List;

@Data
public class RandomUserResponse {
    private List<User> results;
    private Info info;
    
    @Data
    public static class User {
        private String gender;
        private Name name;
        private Location location;
        private String email;
        private String phone;
        private Picture picture;
        
        @Data
        public static class Name {
            private String title;
            private String first;
            private String last;
        }
        
        @Data
        public static class Location {
            private String city;
            private String state;
            private String country;
        }
        
        @Data
        public static class Picture {
            private String large;
            private String medium;
            private String thumbnail;
        }
    }
    
    @Data
    public static class Info {
        private String seed;
        private int results;
        private int page;
        private String version;
    }
}
