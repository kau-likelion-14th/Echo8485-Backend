package likelion14th.lte.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import likelion14th.lte.user.dto.request.CreateUserRequest;
import likelion14th.lte.user.dto.response.UserProfileResponse;
import likelion14th.lte.user.service.UserProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileController {

    private final UserProfileService userProfileService;

    // [Q9. Controller 내부에서 userRepository.findById()를 직접 호출해서 유저를 찾지 않고,
    // 반드시 userProfileService를 호출하여 작업을 위임해야 하는 이유는 무엇인가요?]
    // 답변: Controller가 Repository까지 직접 호출하면 조회·예외·DTO 변환·트랜잭션이 한곳에 몰려 스파게티 코드가 됨.
    //       HTTP 요청/응답 처리는 Controller, 비즈니스 처리는 Service로 나누는 것이 단일 책임 원칙에 맞음.
    //       역할을 분리해야 계층별 수정 범위가 줄고, Service 로직을 다른 API에서도 재사용하기 쉬움.
    @GetMapping
    @Operation(summary = "유저 프로필을 조회합니다", description = "유저프로필 조회")
    public ApiResponse<UserProfileResponse> getUserProfile(@RequestParam Long userId) {
        UserProfileResponse response = userProfileService.getUserProfile(userId);
        return ApiResponse.onSuccess(SuccessCode.USER_INFO_GET_SUCCESS, response);
    }

    @PostMapping
    @Operation(summary = "테스트 유저 생성", description = "테스트용 유저를 생성합니다.")
    public ApiResponse<UserProfileResponse> createTestUser(
            // [Q10. 클라이언트가 보낸 JSON 텍스트 데이터가 어떻게 자바 객체인 CreateTestUserRequest로
            // 변환 되는지앞의 어노테이션과 연관 지어 설명해 보세요.]
            // 답변: 프론트가 {"username":"...", "userTag":"..."} 형태의 JSON을 Body에 실어 보냄.
            //       @RequestBody가 "Body 내용을 이 매개변수 객체로 변환하라"고 Spring에 알려 줌.
            //       Spring은 JSON 키와 DTO 필드명을 매칭해 값을 채우는 역직렬화를 수행함.
            @RequestBody CreateUserRequest request
    ) {
        UserProfileResponse response = userProfileService.createTestUser(request);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }
}