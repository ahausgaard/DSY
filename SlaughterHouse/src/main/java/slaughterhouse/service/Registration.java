package slaughterhouse.service;

// The registered entity, and whether this call created it or it was already registered
public record Registration<T>(T registered, boolean created)
{
}
