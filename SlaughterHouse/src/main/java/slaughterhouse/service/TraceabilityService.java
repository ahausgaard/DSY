package slaughterhouse.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import slaughterhouse.domain.Animal;
import slaughterhouse.domain.Product;
import slaughterhouse.persistence.AnimalRepository;
import slaughterhouse.persistence.ProductRepository;

import java.util.List;
import java.util.UUID;

// Answers which animals are in a product and which products an animal ended up in.
// A product counts as containing every animal with a part in any of its trays, since
// the parts are not tracked individually once they are packed.
@Service
@Transactional(readOnly = true)
public class TraceabilityService
{
  private final AnimalRepository animalRepository;
  private final ProductRepository productRepository;

  public TraceabilityService(AnimalRepository animalRepository, ProductRepository productRepository)
  {
    this.animalRepository = animalRepository;
    this.productRepository = productRepository;
  }

  public Animal getAnimal(UUID animalId)
  {
    return animalRepository.findById(animalId)
        .orElseThrow(() -> new NotFoundException("No animal with id " + animalId));
  }

  public Product getProduct(UUID productId)
  {
    return productRepository.findById(productId)
        .orElseThrow(() -> new NotFoundException("No product with id " + productId));
  }

  public List<UUID> getAnimalsInProduct(UUID productId)
  {
    if (!productRepository.existsById(productId))
      throw new NotFoundException("No product with id " + productId);

    return animalRepository.findIdsOfAnimalsInProduct(productId);
  }

  public List<UUID> getProductsForAnimal(UUID animalId)
  {
    if (!animalRepository.existsById(animalId))
      throw new NotFoundException("No animal with id " + animalId);

    return productRepository.findIdsOfProductsForAnimal(animalId);
  }
}
