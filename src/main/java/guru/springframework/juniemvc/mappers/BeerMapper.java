package guru.springframework.juniemvc.mappers;

import guru.springframework.juniemvc.entities.Beer;
import guru.springframework.juniemvc.models.BeerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface BeerMapper {

    BeerDto toDto(Beer entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    Beer toEntity(BeerDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    void updateEntityFromDto(BeerDto dto, @MappingTarget Beer entity);
}
