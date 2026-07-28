package likelion14th.lte.todo.schedule;

import likelion14th.lte.todo.entity.Todo;
import likelion14th.lte.todo.generator.RoutineTodoDateGenerator;
import likelion14th.lte.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Component
@RequiredArgsConstructor
public class RoutineTodoScheduler {

    private final TodoRepository todoRepository;
    private final RoutineTodoDateGenerator routineTodoDateGenerator;

    /** 매일 새벽 3시에 루틴 TodoDate 를 미리 생성 */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void generateRoutineTodoDates() {
        LocalDate today = LocalDate.now();

        List<Todo> routineTodos = todoRepository.findAllByRoutineEnabledTrue();

        for (Todo todo : routineTodos) {
            routineTodoDateGenerator.generate(
                    todo,
                    todo.getStartDate(),
                    todo.getEndDate(),
                    today
            );
        }
    }
}