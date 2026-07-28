package likelion14th.lte.statistic.dto.response;

import likelion14th.lte.statistic.entity.Statistic;
import likelion14th.lte.todo.entity.WeekEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StatisticResponse {

    /** 연속 성공일 **/
    private int streak;

    /** 최근 30일 완료율 **/
    private int monthPercent;

    /** 가장 Todo 를 많이 완료한 요일 **/
    private WeekEnum mostTodoWeek;

    public static StatisticResponse from(Statistic statistic) {
        return new StatisticResponse(
                statistic.getStreak(),
                statistic.getMonthPercent(),
                statistic.getMostTodoWeek()
        );
    }
}
