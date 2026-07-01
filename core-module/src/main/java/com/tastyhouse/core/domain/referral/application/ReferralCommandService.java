package com.tastyhouse.core.domain.referral.application;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.point.application.PointCommandService;
import com.tastyhouse.core.domain.point.application.dto.command.EarnPointCommand;
import com.tastyhouse.core.domain.referral.application.dto.command.RegisterReferralCommand;
import com.tastyhouse.core.domain.referral.domain.event.ReferralRegisteredEvent;
import com.tastyhouse.core.domain.referral.domain.model.MemberReferral;
import com.tastyhouse.core.domain.referral.domain.repository.MemberReferralRepository;
import com.tastyhouse.core.domain.referral.domain.vo.ReferralId;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class ReferralCommandService {

    private static final int REFERRAL_REWARD_POINT = 1000;
    private static final int REFEREE_REWARD_POINT = 1000;

    private final MemberReferralRepository memberReferralRepository;
    private final PointCommandService pointCommandService;
    private final ApplicationEventPublisher eventPublisher;

    public ReferralId register(RegisterReferralCommand command) {
        if (command.referrerId().equals(command.refereeId())) {
            throw new BusinessException(ErrorCode.REFERRAL_SELF_NOT_ALLOWED);
        }

        if (memberReferralRepository.existsByRefereeId(command.refereeId())) {
            throw new BusinessException(ErrorCode.REFERRAL_ALREADY_EXISTS);
        }

        MemberReferral referral = MemberReferral.register(command.referrerId(), command.refereeId());
        memberReferralRepository.save(referral);

        pointCommandService.getOrCreateMemberPoint(command.referrerId());
        pointCommandService.earnPoints(new EarnPointCommand(command.referrerId(), REFERRAL_REWARD_POINT, "추천인 보상"));

        pointCommandService.getOrCreateMemberPoint(command.refereeId());
        pointCommandService.earnPoints(new EarnPointCommand(command.refereeId(), REFEREE_REWARD_POINT, "추천받기 보상"));

        referral.reward();
        memberReferralRepository.save(referral);

        eventPublisher.publishEvent(new ReferralRegisteredEvent(
            referral.getReferralId(),
            referral.getReferrerId(),
            referral.getRefereeId(),
            LocalDateTime.now()
        ));

        return referral.getReferralId();
    }

    public void cancel(ReferralId referralId) {
        MemberReferral referral = memberReferralRepository.findById(referralId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REFERRAL_NOT_FOUND));
        referral.cancel();
        memberReferralRepository.save(referral);
    }
}
