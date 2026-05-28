package likelion14th.lte.user.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 1. JSON 데이터를 자바 객체로 변환할 때 기본 생성자가 필요해서 뚫어둡니다.
public class CreateTestUserRequest {
    // 2. 회원가입 시 사용자가 입력하는 딱 3가지 정보만 들어있는 가벼운 바구니입니다.
    private String username;
    private String userTag;
    private String introduction;
}