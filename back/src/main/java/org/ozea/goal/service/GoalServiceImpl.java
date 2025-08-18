package org.ozea.goal.service;
import org.ozea.goal.dto.response.*;
import org.ozea.user.domain.User;
import org.ozea.goal.domain.Goal;
import org.ozea.goal.dto.request.GoalCreateRequestDto;
import org.ozea.goal.dto.request.GoalUpdateRequestDto;
import org.ozea.goal.mapper.GoalMapper;
import org.ozea.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class GoalServiceImpl implements GoalService {
    @Autowired
    private GoalMapper goalMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public RecommendNextGoalDto recommendNextGoal(UUID userId) {
        List<PastGoalResponseDto> pastGoals = getPastGoals(userId);

        if (pastGoals.isEmpty()) {
            Long monthlyNet = goalMapper.findPayAmountByUserId(userId);
            LocalDate now = LocalDate.now();
            LocalDate oneYearEnd = now.plusYears(1).minusDays(1);
            if (monthlyNet == null || monthlyNet <= 0) {
                return new RecommendNextGoalDto(
                        0L,
                        now,
                        oneYearEnd,
                        "지난 목표가 없고 월순수익이 미입력입니다. 입력하면 자동 계산해 드려요."
                );
            }
            long saveMonthly = monthlyNet / 2;
            long target = saveMonthly * 12;
            return new RecommendNextGoalDto(
                    target,
                    now,
                    oneYearEnd,
                    "당신에게 딱 맞는 목표를 추천했어요."
            );
        }
        PastGoalResponseDto recent = pastGoals.get(0);
        boolean isSuccess = recent.isSuccess();
        long originalAmount = recent.getTargetAmount();
        LocalDate now = LocalDate.now();
        LocalDate oldStart = recent.getStartDate();
        LocalDate oldEnd = recent.getEndDate();
        long originalDays = ChronoUnit.DAYS.between(oldStart, oldEnd);
        long newAmount = isSuccess
                ? Math.round(originalAmount * 1.2)
                : Math.round(originalAmount * 0.9);
        long newDays = isSuccess
                ? Math.round(originalDays * 0.9)
                : Math.round(originalDays * 1.2);
        LocalDate recommendedStart = now;
        LocalDate recommendedEnd = now.plusDays(newDays);
        String reason = isSuccess
                ? "이전 목표를 성공적으로 달성했어요! 금액을 조금 늘리고 기간을 줄여드렸어요."
                : "이전 목표를 달성하지 못했어요. 기간을 더 넉넉하게 잡고 금액을 줄여드렸어요.";
        return new RecommendNextGoalDto(newAmount, recommendedStart, recommendedEnd, reason);
    }
    @Override
    public void updateGoalStatuses() {
        goalMapper.updateSuccessGoals();
        goalMapper.updateFailedGoals();
    }
    @Override
    public List<PastGoalResponseDto> getPastGoals(UUID userId) {
        updateGoalStatuses();
        return goalMapper.findPastGoalsByUserId(userId);
    }
    @Override
    public void createGoal(UUID userId, GoalCreateRequestDto requestDto, UUID goalId) {
        LocalDate startDate = requestDto.getStart_date();
        LocalDate endDate = requestDto.getEnd_date();
        long totalMonths = ChronoUnit.MONTHS.between(startDate, endDate) + 1;
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("사용자가 존재하지 않습니다.");
        }
        long monthlyIncome = user.getSalary() - user.getPayAmount();
        long totalExpectedIncome = monthlyIncome * totalMonths;
        long existingTargetAmount = goalMapper.sumTargetAmountOverlappingGoals(userId, startDate, endDate);
        long newTargetAmount = requestDto.getTarget_amount();
        if (existingTargetAmount + newTargetAmount > totalExpectedIncome) {
            throw new IllegalArgumentException("해당 기간 순이익을 초과하여 목표를 생성할 수 없습니다.");
        }
        goalMapper.insertGoal(requestDto.toEntity(userId, goalId));
    }
    @Override
    public List<ProductRecommendResponseDto> recommendProducts(UUID goalId) {
        Goal goal = goalMapper.findByGoalId(goalId);
        if (goal == null) {
            throw new IllegalArgumentException("해당 목표가 존재하지 않습니다.");
        }
        int goalDuration = Period.between(goal.getStartDate(), goal.getEndDate()).getYears();
        long goalAmount = goal.getTargetAmount();
        long savePerMonth = goalAmount / (goalDuration * 12);
        List<ProductRecommendResponseDto> candidates = goalMapper.findProductsWithOptions();
        return candidates.stream()
                .sorted(Comparator.comparing(ProductRecommendResponseDto::getIntrRate2).reversed())
                .limit(5)
                .peek(dto -> {
                    StringBuilder reason = new StringBuilder();
                    reason.append("✨ 목표 기간과 비슷한 ").append(dto.getSaveTrm()).append("개월 상품입니다.");
                    if (dto.getIntrRate2().compareTo(new BigDecimal("3.0")) > 0) {
                        reason.append(" 우대금리가 높습니다.");
                    }
                    if (dto.getRsrvTypeNm().contains("자유")) reason.append(" 자유적립식입니다.");
                })
                .collect(Collectors.toList());
    }
    @Override
    public List<LinkedAccountDto> getAccountsByUserId(UUID userId) {
        return goalMapper.findAccountsByUserId(userId);
    }
    @Override
    public void linkAccountToGoal(UUID goalId, int accountId) {
        Integer count = goalMapper.isAccountAlreadyLinked(accountId);
        if (count != null && count > 0) {
            throw new IllegalStateException("이미 다른 목표에 연동된 계좌입니다.");
        }
        goalMapper.linkAccountToGoal(goalId, accountId);
    }
    @Override
    public List<GoalListResponseDto> getGoalsByUserId(UUID userId) {
        List<Goal> goals = goalMapper.findAllByUserId(userId); // 기존 그대로 사용
        return goals.stream()
                .map(g -> GoalListResponseDto.from(
                        g,
                        Optional.ofNullable(goalMapper.findLinkedAccountsByGoalId(g.getGoalId()))
                                .orElseGet(Collections::emptyList)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public GoalDetailResponseDto getGoalById(UUID goalId) {
        Goal goal = goalMapper.findByGoalId(goalId);
        if (goal == null) {
            throw new NoSuchElementException("해당 목표가 존재하지 않습니다.");
        }
        List<LinkedAccountDto> linkedAccounts = goalMapper.findLinkedAccountsByGoalId(goalId);
        return GoalDetailResponseDto.from(goal, linkedAccounts);
    }
    @Override
    public void unlinkAccount(int accountId) {
        goalMapper.unlinkAccount(accountId);
    }
    @Override
    public void deleteGoal(UUID goalId) {
        goalMapper.unlinkAllAccountsFromGoal(goalId);
        int deleted = goalMapper.deleteByGoalIdAndUserId(goalId);
        if (deleted == 0) {
            throw new IllegalArgumentException("해당 목표가 존재하지 않거나 권한이 없습니다.");
        }
    }
    @Transactional
    @Override
    public void updateGoal(UUID goalId, GoalUpdateRequestDto dto) {
        // 0) 대상 목표 확인 + userId 확보
        Goal existing = goalMapper.findByGoalId(goalId);
        if (existing == null) {
            throw new NoSuchElementException("해당 목표가 존재하지 않습니다.");
        }
        UUID userId = existing.getUserId();

        // 1) 입력값 검증
        LocalDate start = dto.getStart_date();
        LocalDate end   = dto.getEnd_date();
        if (start == null || end == null || end.isBefore(start)) {
            throw new IllegalArgumentException("기간이 올바르지 않습니다.");
        }
        Long targetAmount = dto.getTarget_amount();
        if (targetAmount == null || targetAmount <= 0) {
            throw new IllegalArgumentException("목표 금액이 올바르지 않습니다.");
        }

        // 2) 월 순수익(>=0)
        long monthlyNet = goalMapper.findPayAmountByUserId(userId);

        // 3) 포함 개월 수(같은 달이면 1개월)
        long months = ChronoUnit.MONTHS.between(YearMonth.from(start), YearMonth.from(end)) + 1;
        if (months <= 0) {
            throw new IllegalArgumentException("기간이 올바르지 않습니다.");
        }

        // 4) 자기 자신 제외, 겹치는 목표들의 target 합
        long reserved = goalMapper.sumTargetAmountOverlappingGoalsExceptGoal(userId, goalId, start, end);

        // 5) 상한 비교: (겹치는 합 + 이번 목표) ≤ 월순수익 × 개월수
        long maxAffordable = monthlyNet * months;
        long projected     = reserved + targetAmount;
        if (projected > maxAffordable) {
            long allowed = Math.max(0, maxAffordable - reserved);
            throw new IllegalArgumentException(
                    "기간 대비 목표 금액이 과도합니다. " +
                            "\n최대 설정 가능 금액: " + String.format("%,d", allowed) + "원"
            );
        }

        int updated = goalMapper.updateGoalByIdAndUserId(goalId, dto);
        if (updated == 0) {
            throw new IllegalArgumentException("해당 목표가 존재하지 않거나 권한이 없습니다.");
        }
    }
    @Override
    @Transactional
    public ClaimRewardResponseDto claimGoalReward(UUID userId, UUID goalId) {
        int inserted = goalMapper.claimGoalAchieveReward(userId, goalId);
        if (inserted == 1) {
            Integer pts = goalMapper.calcGoalRewardPoints(userId, goalId);
            return new ClaimRewardResponseDto(
                    ClaimRewardResponseDto.Status.REWARDED,
                    pts != null ? pts : 0
            );
        }
        boolean already = goalMapper.existsGoalRewardPoint(userId, goalId);
        return new ClaimRewardResponseDto(
                already ? ClaimRewardResponseDto.Status.ALREADY_REWARDED
                        : ClaimRewardResponseDto.Status.NOT_ELIGIBLE,
                null
        );
    }
    @Override
    public boolean existsGoalRewardPoint(UUID userId, UUID goalId) {
        return goalMapper.existsGoalRewardPoint(userId, goalId);
    }
}
