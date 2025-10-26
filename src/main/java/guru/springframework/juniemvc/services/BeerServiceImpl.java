package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.entities.Beer;
import guru.springframework.juniemvc.mappers.BeerMapper;
import guru.springframework.juniemvc.models.BeerDto;
import guru.springframework.juniemvc.repositories.BeerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of BeerService that uses BeerRepository for persistence
 */
@Service
class BeerServiceImpl implements BeerService {

    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;

    BeerServiceImpl(BeerRepository beerRepository, BeerMapper beerMapper) {
        this.beerRepository = beerRepository;
        this.beerMapper = beerMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeerDto> getAllBeers() {
        return beerRepository.findAll()
                .stream()
                .map(beerMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BeerDto> getBeerById(Integer id) {
        return beerRepository.findById(id).map(beerMapper::toDto);
    }

    @Override
    @Transactional
    public BeerDto createBeer(BeerDto beerDto) {
        Beer entity = beerMapper.toEntity(beerDto);
        Beer saved = beerRepository.save(entity);
        return beerMapper.toDto(saved);
    }

    @Override
    @Transactional
    public Optional<BeerDto> updateBeer(Integer id, BeerDto beerDto) {
        return beerRepository.findById(id)
                .map(existing -> {
                    beerMapper.updateEntityFromDto(beerDto, existing);
                    Beer saved = beerRepository.save(existing);
                    return beerMapper.toDto(saved);
                });
    }

    @Override
    @Transactional
    public boolean deleteBeerById(Integer id) {
        Optional<Beer> existing = beerRepository.findById(id);
        if (existing.isPresent()) {
            beerRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
