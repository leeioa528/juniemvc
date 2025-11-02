package guru.springframework.juniemvc.mappers;

import guru.springframework.juniemvc.entities.BeerOrder;
import guru.springframework.juniemvc.entities.BeerOrderLine;
import guru.springframework.juniemvc.models.BeerOrderDto;
import guru.springframework.juniemvc.models.BeerOrderLineDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BeerOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "orderLines", ignore = true) // lines are handled in service
    BeerOrder toEntity(BeerOrderDto dto);

    @Mapping(target = "orderLines", expression = "java(mapLines(entity.getOrderLines()))")
    BeerOrderDto toDto(BeerOrder entity);

    // Default helper to avoid needing nested mapper in unit tests
    default List<BeerOrderLineDto> mapLines(List<BeerOrderLine> lines) {
        if (lines == null) return null;
        List<BeerOrderLineDto> dtos = new ArrayList<>(lines.size());
        for (BeerOrderLine line : lines) {
            if (line == null) continue;
            BeerOrderLineDto dto = BeerOrderLineDto.builder()
                    .id(line.getId())
                    .version(line.getVersion())
                    .beerId(line.getBeer() != null ? line.getBeer().getId() : null)
                    .orderQuantity(line.getOrderQuantity())
                    .price(line.getPrice())
                    .createdDate(line.getCreatedDate())
                    .updateDate(line.getUpdateDate())
                    .build();
            dtos.add(dto);
        }
        return dtos;
    }
}
