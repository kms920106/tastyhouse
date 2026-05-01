package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.rank.dto.RankDurationDto;
import com.tastyhouse.core.entity.rank.dto.RankPrizeDto;
import com.tastyhouse.core.repository.rank.RankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RankInfoCoreService {

    private final RankRepository rankRepository;

    @Transactional(readOnly = true)
    public Optional<RankDurationDto> findActiveDuration() {
        return rankRepository.findActiveDuration();
    }

    @Transactional(readOnly = true)
    public List<RankPrizeDto> findActivePrizes() {
        return rankRepository.findActivePrizes();
    }
}
