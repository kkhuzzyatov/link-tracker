package backend.academy.linktracker.scrapper.api.client;

import backend.academy.linktracker.scrapper.link.model.Link;
import backend.academy.linktracker.scrapper.link.model.ResourceIdentifier;
import java.util.List;

public interface LinkUpdateClient {

    boolean supports(ResourceIdentifier identifier);

    List<String> getNewLinkChanges(Link link);
}
