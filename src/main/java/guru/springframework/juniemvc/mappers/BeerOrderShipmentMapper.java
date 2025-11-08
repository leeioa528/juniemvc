package guru.springframework.juniemvc.mappers;

import guru.springframework.juniemvc.entities.BeerOrder;
import guru.springframework.juniemvc.entities.BeerOrderShipment;
import guru.springframework.juniemvc.models.BeerOrderShipmentRequest;
import guru.springframework.juniemvc.models.BeerOrderShipmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BeerOrderShipmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "beerOrder", source = "beerOrder")
    BeerOrderShipment toEntity(BeerOrderShipmentRequest request, BeerOrder beerOrder);

    @Mapping(target = "beerOrderId", source = "beerOrder.id")
    BeerOrderShipmentResponse toResponse(BeerOrderShipment entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "beerOrder", ignore = true)
    void updateEntity(@MappingTarget BeerOrderShipment entity, BeerOrderShipmentRequest request);
}
