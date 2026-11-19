package slaughterhouse.messaging;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

// Everything a station can send. The JSON carries the record's name in "@type", so the server's
// listener can read any message as a StationMessage and switch on which record it is
@JsonTypeInfo(use = JsonTypeInfo.Id.SIMPLE_NAME)
@JsonSubTypes({
    @JsonSubTypes.Type(AnimalRegistered.class),
    @JsonSubTypes.Type(TrayOpened.class),
    @JsonSubTypes.Type(PartRegistered.class),
    @JsonSubTypes.Type(TrayClosed.class),
    @JsonSubTypes.Type(ProductPacked.class)})
public sealed interface StationMessage
    permits AnimalRegistered, TrayOpened, PartRegistered, TrayClosed, ProductPacked
{
}
