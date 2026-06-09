package backend.academy.linktracker.scrapper.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class StackOverflowQuestionDto {
    private List<Item> items;

    @AllArgsConstructor
    @Getter
    public static class Item {
        private String title;
    }
}
