package likelion14th.lte.user.service;

import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.user.domain.User;
import likelion14th.lte.user.dto.request.CreateTestUserRequest;
import likelion14th.lte.user.dto.response.UserProfileResponse;
import likelion14th.lte.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileService {

    // [Q5. Service 안에서 new UserRepository() 로 객체를 직접 생성하지 않고,
    // 외부에서 의존성 주입을 받는 이유는 무엇인가요?]
    // 답변: 테스트할 때 가짜 Repository를 끼워 넣어 DB 없이 Service만 검증할 수 있음.
    //       new로 직접 만들면 UserRepository 구현에 Service가 강하게 묶여 교체·수정이 어려움.
    //       생성자 주입은 Spring이 객체를 밖에서 넣어 주므로 결합도가 낮아지고 코드 교체가 수월함.
    private final UserRepository userRepository;

    // [Q6. 만약 클래스 위의 @RequiredArgsConstructor를 지운다면,
    // 우리가 직접 작성해야 할 의존성 주입용 자바 '생성자' 코드는 어떤 모습일까요? 아래에 직접 코딩해 보세요.]
    /*
       여기에 생성자 코드 작성:
       protected UserProfileService(UserRepository userRepository) {
           this.userRepository = userRepository;
       }
    */

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse createTestUser(CreateTestUserRequest request) {

        // [Q7. 일반적인 생성자 new User(name, intro, tag) 방식을 쓰지 않고,
        // User.builder()...build() 라는 '빌더 패턴'을 사용하여 객체를 조립했을 때 얻는 장점은 무엇인가요?]
        // 답변: builder는 .username(...).userTag(...)처럼 필드 이름을 적어 값을 넣으므로
        //       코드만 봐도 어디에 무엇이 들어가는지 바로 보여 가독성이 좋음.
        //       반면 new User("a", "b", "c")는 String끼리 순서를 바꿔 써도 컴파일이 통과되어
        //       논리적 실수를 잡기 어려움 → builder가 이런 실수를 줄여 줌.
        User newUser = User.builder()
                .username(request.getUsername())
                .introduction(request.getIntroduction())
                .userTag(request.getUserTag())
                .build();

        User savedUser;
        try {
            // [Q8. 데이터를 저장하는 이 메서드 위에 @Transactional이 반드시 붙어야 하는 이유는 무엇인가요?
            // 저장 도중 DB 서버가 끊겼을 때를 가정해서 설명하세요.]
            // 답변: save() 실행 중 DB 연결이 끊기면 저장이 중간에 멈출 수 있음.
            //       @Transactional이 있으면 이때까지의 작업을 롤백해 처음 상태로 되돌림.
            //       즉 "전부 성공하거나 전부 취소"되는 원자성이 보장되어, 반쪽만 저장된 데이터 오염을 막을 수 있음.
            savedUser = userRepository.save(newUser);
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.BAD_REQUEST);
        }
        return UserProfileResponse.from(savedUser);
    }
}