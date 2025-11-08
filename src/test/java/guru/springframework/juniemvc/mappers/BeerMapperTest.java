package guru.springframework.juniemvc.mappers;

import guru.springframework.juniemvc.entities.Beer;
import guru.springframework.juniemvc.models.BeerPatchRequest;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BeerMapperTest {

    private final BeerMapper mapper = Mappers.getMapper(BeerMapper.class);

    @Test
    void patchEntityFromRequest_ignoresNulls_and_updatesNonNulls() {
        Beer existing = Beer.builder()
                .id(1)
                .version(2)
                .beerName("Old Name")
                .beerStyle("IPA")
                .upc("0000000000001")
                .quantityOnHand(10)
                .price(new BigDecimal("9.99"))
                .description("old")
                .build();

        BeerPatchRequest req = BeerPatchRequest.builder()
                .beerName(null) // should be ignored
                .beerStyle("STOUT") // should be applied
                .upc(null) // ignored
                .quantityOnHand(25) // applied
                .price(new BigDecimal("12.50")) // applied
                .description(null) // ignored
                .build();

        mapper.patchEntityFromRequest(req, existing);

        // server-managed fields unchanged
        assertThat(existing.getId()).isEqualTo(1);
        assertThat(existing.getVersion()).isEqualTo(2);

        // unchanged due to null
        assertThat(existing.getBeerName()).isEqualTo("Old Name");
        assertThat(existing.getUpc()).isEqualTo("0000000000001");
        assertThat(existing.getDescription()).isEqualTo("old");

        // updated
        assertThat(existing.getBeerStyle()).isEqualTo("STOUT");
        assertThat(existing.getQuantityOnHand()).isEqualTo(25);
        assertThat(existing.getPrice()).isEqualByComparingTo("12.50");
    }
}
