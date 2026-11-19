package slaughterhouse.station;

import slaughterhouse.messaging.ProductPacked;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

// Station 3: products are packed from closed trays and registered with the trays they came from.
// The tray ids come from the trays' labels
public class PackingStation
{
  // Product type codes from the catalogue (db/seed.sql). The server checks that the trays are closed
  // and hold exactly the part types the product type needs
  private static final List<Integer> PRODUCT_TYPES = List.of(1001, 1002, 1003, 1004);

  public static void main(String[] args) throws IOException, TimeoutException
  {
    ConsoleInput input = new ConsoleInput();

    try (StationConnection station = new StationConnection("localhost"))
    {
      System.out.println("Packing station ready.");

      while (true)
      {
        String productTypeInput = input.line("Product type " + PRODUCT_TYPES + " (q to quit): ");
        if (productTypeInput.equalsIgnoreCase("q"))
          break;

        int productType;
        try
        {
          productType = Integer.parseInt(productTypeInput);
        }
        catch (NumberFormatException e)
        {
          productType = -1;
        }
        if (!PRODUCT_TYPES.contains(productType))
        {
          System.out.println("Choose one of " + PRODUCT_TYPES + ". Try again.");
          continue;
        }
        Set<UUID> trayIds = input.uuids("Tray ids (comma separated): ");
        BigDecimal weight = input.positiveDecimal("Total weight (kg): ");

        ProductPacked product = new ProductPacked(UUID.randomUUID(), productType, trayIds, weight,
            LocalDateTime.now());
        station.publish(product);
        System.out.println("Sent product ID: " + product.productId());
      }
    }
  }
}
