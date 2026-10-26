package slaughterhouse.api.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import slaughterhouse.domain.Product;
import slaughterhouse.domain.ProductKind;
import slaughterhouse.domain.ProductType;
import slaughterhouse.domain.Tray;
import slaughterhouse.service.NotFoundException;
import slaughterhouse.service.PackingService;
import slaughterhouse.service.Registration;
import slaughterhouse.service.RuleViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Only the web layer: the service is a mock, so no database is needed
@WebMvcTest(PackingController.class)
class PackingControllerTest
{
  private final UUID productId = UUID.fromString("d0000000-0000-4000-8000-000000000001");
  private final UUID trayId = UUID.fromString("c0000000-0000-4000-8000-000000000001");

  @Autowired
  private MockMvc mvc;

  @MockitoBean
  private PackingService packingService;

  private Product product;

  @BeforeEach
  void setUp()
  {
    ProductType ham2Pack = mock(ProductType.class);
    when(ham2Pack.getCode()).thenReturn(1001);
    when(ham2Pack.getKind()).thenReturn(ProductKind.SAME_PART);
    Tray tray = mock(Tray.class);
    when(tray.getTrayId()).thenReturn(trayId);

    product = new Product(productId, ham2Pack, new BigDecimal("18.200"),
        LocalDateTime.of(2026, 9, 22, 7, 0), Set.of(tray));
  }

  @Test
  void packingANewProductReturnsCreated() throws Exception
  {
    when(packingService.packProduct(eq(productId), eq(1001), eq(Set.of(trayId)), any(), any()))
        .thenReturn(new Registration<>(product, true));

    mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(packProductBody()))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/products/" + productId))
        .andExpect(jsonPath("$.productTypeCode").value(1001))
        .andExpect(jsonPath("$.kind").value("SAME_PART"))
        .andExpect(jsonPath("$.trayIds[0]").value(trayId.toString()));
  }

  @Test
  void packingTheSameProductAgainReturnsOk() throws Exception
  {
    when(packingService.packProduct(any(), anyInt(), any(), any(), any()))
        .thenReturn(new Registration<>(product, false));

    mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(packProductBody()))
        .andExpect(status().isOk());
  }

  @Test
  void packingWithoutTraysIsABadRequest() throws Exception
  {
    mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content("""
            {"productId": "%s", "productTypeCode": 1001, "trayIds": [],
             "totalWeightKg": 18.2, "packedAt": "2026-09-22T07:00:00"}"""
            .formatted(productId)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void packingWithMissingFieldsIsABadRequest() throws Exception
  {
    mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void productBreakingARuleIsAConflict() throws Exception
  {
    when(packingService.packProduct(any(), anyInt(), any(), any(), any()))
        .thenThrow(new RuleViolationException("Tray is still open"));

    mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(packProductBody()))
        .andExpect(status().isConflict());
  }

  @Test
  void getsAProduct() throws Exception
  {
    when(packingService.getProduct(productId)).thenReturn(product);

    mvc.perform(get("/products/" + productId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.productId").value(productId.toString()))
        .andExpect(jsonPath("$.totalWeightKg").value(18.2));
  }

  @Test
  void unknownProductIsNotFound() throws Exception
  {
    when(packingService.getProduct(productId)).thenThrow(new NotFoundException("No product"));

    mvc.perform(get("/products/" + productId))
        .andExpect(status().isNotFound());
  }

  private String packProductBody()
  {
    return """
        {"productId": "%s", "productTypeCode": 1001, "trayIds": ["%s"],
         "totalWeightKg": 18.2, "packedAt": "2026-09-22T07:00:00"}"""
        .formatted(productId, trayId);
  }
}
