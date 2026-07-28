package likelion14th.lte.todo.generator;

import likelion14th.lte.todo.entity.Todo;
import likelion14th.lte.todo.entity.TodoDate;
import likelion14th.lte.todo.repository.TodoDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoutineTodoDateGenerator {

    /**
     * 기준일로부터 미리 만들어 둘 기간.
     * 캘린더가 월 단위로 조회되므로, 월말에 다음 달을 열어도 비어 보이지 않도록 3개월치를 확보한다.
     */
    private static final int GENERATION_WINDOW_DAYS = 90;

    private final TodoDateRepository todoDateRepository;

    /**
     * 루틴 Todo 의 TodoDate 를 미리 생성한다.
     * 기준일 이전(과거)과 종료일 이후는 만들지 않으며, 이미 있는 날짜는 건너뛴다.
     *
     * @param todo      대상 루틴 Todo
     * @param startDate 루틴 시작일 (null 이면 기준일부터)
     * @param endDate   루틴 종료일
     * @param baseDate  생성 기준일 (스케줄러가 도는 날짜)
     */
    public void generate(Todo todo, LocalDate startDate, LocalDate endDate, LocalDate baseDate) {
        // 요일이나 종료일이 없으면 생성할 날짜를 특정할 수 없다
        if (todo.getWeek() == null || endDate == null) {
            return;
        }

        LocalDate base = (baseDate != null) ? baseDate : LocalDate.now();

        // 시작점: 기준일과 루틴 시작일 중 더 나중 (과거는 만들지 않는다)
        LocalDate from = (startDate != null && startDate.isAfter(base)) ? startDate : base;

        // 끝점: 기준일 + 생성 기간, 단 종료일을 넘지 않는다
        LocalDate to = base.plusDays(GENERATION_WINDOW_DAYS);
        if (to.isAfter(endDate)) {
            to = endDate;
        }

        if (from.isAfter(to)) {
            return;
        }

        // 이미 만들어진 날짜는 다시 만들지 않는다 (스케줄러가 매일 돌기 때문에 필수)
        Set<LocalDate> existingDates = todoDateRepository
                .findAllByTodo_IdAndDateBetween(todo.getId(), from, to)
                .stream()
                .map(TodoDate::getDate)
                .collect(Collectors.toSet());

        DayOfWeek targetDayOfWeek = todo.getWeek().toDayOfWeek();

        List<TodoDate> newTodoDates = new ArrayList<>();
        LocalDate cursor = from.with(TemporalAdjusters.nextOrSame(targetDayOfWeek));
        while (!cursor.isAfter(to)) {
            if (!existingDates.contains(cursor)) {
                newTodoDates.add(TodoDate.create(todo, cursor));
            }
            cursor = cursor.plusWeeks(1);
        }

        if (!newTodoDates.isEmpty()) {
            todoDateRepository.saveAll(newTodoDates);
        }
    }
}
