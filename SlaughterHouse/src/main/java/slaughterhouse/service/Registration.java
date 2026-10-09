package slaughterhouse.service;

import slaughterhouse.domain.Animal;

// The registered animal, and whether this call created it or it was already registered
public record Registration(Animal animal, boolean created)
{
}
