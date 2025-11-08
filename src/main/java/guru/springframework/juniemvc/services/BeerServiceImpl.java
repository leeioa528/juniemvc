package guru.springframework.juniemvc.services;

import guru.springframework.juniemvc.entities.Beer;
import guru.springframework.juniemvc.mappers.BeerMapper;
import guru.springframework.juniemvc.models.BeerDto;
import guru.springframework.juniemvc.repositories.BeerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    public Page<BeerDto> getBeers(String beanName, String beerStyle, Pageable pageable) {
        boolean hasName = beanName != null && !beanName.isBlank();
        boolean hasStyle = beerStyle != null && !beerStyle.isBlank();

        Page<Beer> page;
        if (hasName && hasStyle) {
            page = beerRepository.findByBeerNameContainingIgnoreCaseAndBeerStyleContainingIgnoreCase(beanName, beerStyle, pageable);
        } else if (hasName) {
            page = beerRepository.findByBeerNameContainingIgnoreCase(beanName, pageable);
        } else if (hasStyle) {
            page = beerRepository.findByBeerStyleContainingIgnoreCase(beerStyle, pageable);
        } else {
            page = beerRepository.findAll(pageable);
        }

        List<BeerDto> content = page.getContent().stream().map(beerMapper::toDto).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
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
