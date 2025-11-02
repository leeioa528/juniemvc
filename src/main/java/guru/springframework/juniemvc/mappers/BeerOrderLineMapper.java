package guru.springframework.juniemvc.mappers;

import guru.springframework.juniemvc.entities.BeerOrderLine;
import guru.springframework.juniemvc.models.BeerOrderLineDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BeerOrderLineMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "beer.id", source = "beerId")
    @Mapping(target = "beerOrder", ignore = true)
    BeerOrderLine toEntity(BeerOrderLineDto dto);

    @Mapping(target = "beerId", source = "beer.id")
    BeerOrderLineDto toDto(BeerOrderLine entity);
}
