package com.ecommerce.auctionplatform.identity.application.service;

import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import com.ecommerce.auctionplatform.identity.application.port.in.ReputationUseCase;
import com.ecommerce.auctionplatform.identity.domain.model.ReputationHistory;
import com.ecommerce.auctionplatform.identity.domain.model.User;
import com.ecommerce.auctionplatform.identity.domain.repository.ReputationHistoryRepository;
import com.ecommerce.auctionplatform.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReputationService implements ReputationUseCase {
    private final UserRepository userRepository;
    private final ReputationHistoryRepository reputationHistoryRepository;

    @Override
    @Transactional
    public void changeForOrder(UUID userId, int scoreChange, String reason, UUID orderId) {
        User user = getUser(userId);
        changeScore(user, scoreChange);
        userRepository.save(user);
        reputationHistoryRepository.save(ReputationHistory.builder()
                .userId(userId)
                .scoreChange(scoreChange)
                .reason(reason)
                .orderId(orderId)
                .build());
    }

    @Override
    @Transactional
    public void decreaseForDispute(UUID userId, int points, String reason, UUID disputeId) {
        User user = getUser(userId);
        user.decreaseReputationScore(points);
        userRepository.save(user);
        reputationHistoryRepository.save(ReputationHistory.builder()
                .userId(userId)
                .scoreChange(-points)
                .reason(reason)
                .disputeId(disputeId)
                .build());
    }

    @Override
    @Transactional
    public void decreaseForAuction(UUID userId, int points, String reason, UUID auctionId) {
        User user = getUser(userId);
        user.decreaseReputationScore(points);
        userRepository.save(user);
        reputationHistoryRepository.save(ReputationHistory.builder()
                .userId(userId)
                .scoreChange(-points)
                .reason(reason)
                .build());
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private void changeScore(User user, int scoreChange) {
        if (scoreChange < 0) {
            user.decreaseReputationScore(Math.abs(scoreChange));
        } else {
            user.increaseReputationScore(scoreChange);
        }
    }
}
