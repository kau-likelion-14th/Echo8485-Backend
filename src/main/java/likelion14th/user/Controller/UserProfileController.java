package likelion14th.user.Controller;


import io.swagger.v3.oas.annotations.Operation;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import likelion14th.lte.user.dto.request.CreateTestUserRequest;
import likelion14th.lte.user.dto.response.UserProfileResponse;
import likelion14th.lte.user.service.UserProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
// 1. "나는 HTML 화면을 반환하는 게 아니라, JSON 데이터만 반환하는 컨트롤러야!"
@Slf4j
// 2. 콘솔 창에 로그(log.info 등)를 찍을 수 있게 해주는 롬복 기능입니다.
@RequestMapping("/api/profile")
// 3. 이 클래스 안의 모든 API 주소는 기본적으로 "/api/profile"로 시작합니다.
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
// 4. [의존성 주입(DI)] final이 붙은 변수의 생성자를 알아서 만들어줍니다. 스프링이 알아서 Service 객체를 이 안에 끼워 넣어줍니다!
public class UserProfileController {

    public final UserProfileService userProfileService;
    // 5. 비즈니스 로직을 처리할 Service 계층을 호출하기 위한 변수입니다. (직접 new를 쓰지 않았음에 주목하세요!)
    // [Q9. Controller 내부에서 userRepository.findById()를 직접 호출해서 유저를 찾지 않고,
    // 반드시 userProfileService를 호출하여 작업을 위임해야 하는 이유는 무엇인가요? (단일 책임 원칙 관점)]
    // 답변:controller 역할과 repoistory 역할이 하나의 클래스or파일에 묶이므로 단일 책임 원칙 관점에서 위배하게 된다

    @GetMapping
    // 6. 클라이언트가 GET 방식(조회)으로 "/api/profile"을 요청하면 이 메서드가 실행됩니다.
    @Operation(summary = "유저 프로필을 조회합니다", description = "유저프로필 조회")
    // 7. Swagger(API 명세서 자동 완성 도구)에 설명을 띄우기 위한 장식입니다. 기능엔 영향이 없습니다.
    public ApiResponse<UserProfileResponse> getUserProfile(
            @RequestParam Long userId
            // 8. 주소 뒤에 붙은 파라미터(?userId=1)에서 숫자 1을 빼와서 변수에 담습니다.
    ){
        // 9. 서비스에게 "이 ID 가진 유저 프로필 좀 가져와!" 라고 일을 시킵니다. (이 안에서 DTO 변환까지 다 끝나서 돌아옵니다.)
        UserProfileResponse response = userProfileService.getUserProfile(userId);

        // 10. 찾아온 결과를 ApiResponse라는 공통 규격 상자에 한 번 더 예쁘게 포장해서(성공 코드와 함께) 클라이언트에게 던져줍니다.
        return ApiResponse.onSuccess(SuccessCode.USER_INFO_GET_SUCCESS, response);
    }

    @PostMapping
    // 11. 클라이언트가 POST 방식(생성/저장)으로 "/api/profile"을 요청하면 실행됩니다.
    @Operation(summary = "테스트 유저 생성", description = "...")
    public ApiResponse<UserProfileResponse> createTestUser(
            @RequestBody CreateTestUserRequest request
            // 12. 클라이언트가 보낸 JSON 데이터를 스프링이 알아서 CreateTestUserRequest(DTO) 자바 객체로 변환해서 넣어줍니다.
    ){
        // 13. 서비스에게 "이 DTO 데이터대로 유저 좀 생성해 줘!"라고 지시합니다.
        UserProfileResponse response = userProfileService.createTestUser(request);

        // 14. 성공적으로 생성되었다는 코드(CREATED)와 함께 결과를 반환합니다.
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
        // [Q10. 클라이언트가 보낸 JSON 텍스트 데이터가 어떻게 자바 객체인 CreateTestUserRequest로
        // 변환 되는지앞의 어노테이션과 연관 지어 설명해 보세요.]
        // 답변:@PostMapping은 https를 불러오는 spring 축약어?? 이 클라이언트가 요청하 데이터를 담아 보내 @request가 감지하고 개체로 변환해서 넣어주고
        //이루 변환된 request 객체가 createTestUser() 매서드의 파라미터로 전달하여 사용 가능
    }
}
