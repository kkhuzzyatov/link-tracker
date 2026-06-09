package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StackOverflowCommentsDto {

    private List<Item> items;

    @AllArgsConstructor
    @Getter
    public static class Item {

        private Owner owner;

        @JsonProperty("creation_date")
        private Long creationDate;

        private String body;

        @AllArgsConstructor
        @Getter
        public static class Owner {
            @JsonProperty("display_name")
            private String name;
        }
    }
}
