package likelion14th.lte.statistic;

import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.api.GlobalSuccessHandler;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.global.exception.GlobalExceptionHandler;
import likelion14th.lte.statistic.controller.StatisticController;
import likelion14th.lte.statistic.dto.response.StatisticResponse;
import likelion14th.lte.statistic.entity.Statistic;
import likelion14th.lte.statistic.service.StatisticService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * standalone MockMvc 로 Controller 매핑과 공통 응답 형식을 검증한다.
 * (Spring Boot 4 의 @WebMvcTest 는 별도 모듈이라 새 의존성 없이 사용할 수 없으므로 standalone 방식을 쓴다)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StatisticControllerTest {

    @Mock
    private StatisticService statisticService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new StatisticController(statisticService))
                .setControllerAdvice(new GlobalExceptionHandler(), new GlobalSuccessHandler())
                .build();
    }

    private StatisticResponse sampleResponse() {
        Statistic statistic = Statistic.create();
        ReflectionTestUtils.setField(statistic, "streak", 5);
        ReflectionTestUtils.setField(statistic, "monthPercent", 73);
        return StatisticResponse.from(statistic);
    }

    @Test
    @DisplayName("GET /api/statistic 은 200 OK 와 STATISTIC_2001 성공 응답을 반환한다")
    void getStatisticSuccess() throws Exception {
        given(statisticService.getStatistic(1L)).willReturn(sampleResponse());

        mockMvc.perform(get("/api/statistic").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("STATISTIC_2001"))
                .andExpect(jsonPath("$.message").value("통계 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.result.streak").value(5))
                .andExpect(jsonPath("$.result.monthPercent").value(73))
                .andExpect(jsonPath("$.result.mostTodoWeek").value("MON"));
    }

    @Test
    @DisplayName("존재하지 않는 User 요청은 404 와 USER_4041 을 반환한다")
    void getStatisticUserNotFound() throws Exception {
        given(statisticService.getStatistic(anyLong()))
                .willThrow(new GeneralException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(get("/api/statistic").param("userId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("USER_4041"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."));
    }

    @Test
    @DisplayName("userId 파라미터가 없으면 400 Bad Request 를 반환한다")
    void getStatisticWithoutUserId() throws Exception {
        mockMvc.perform(get("/api/statistic"))
                .andExpect(status().isBadRequest());
    }
}
