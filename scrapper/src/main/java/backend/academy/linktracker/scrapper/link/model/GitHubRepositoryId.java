package backend.academy.linktracker.scrapper.link.model;

public record GitHubRepositoryId(String owner, String repository) implements ResourceIdentifier {}
