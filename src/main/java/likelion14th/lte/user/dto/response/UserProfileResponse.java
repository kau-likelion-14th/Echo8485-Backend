package likelion14th.lte.user.dto.response;

import likelion14th.lte.user.domain.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileResponse {
    private String userName;
    private String profileImageUrl;
    private String introduction;

    // [Q4. Controller가 DB에서 꺼낸 User Entity를 클라이언트 화면에 그대로 반환하지 않고,
    // 굳이 from() 메서드를 통해 DTO로 한번 변환해서 내보내는 핵심적인 이유 2가지는 무엇인가요?]
    // 답변:
    // 1) 유지보수: 화면에는 "이름#태그"처럼 가공된 형태가 필요하지만, DB 컬럼 구조는 그대로 둘 수 있음.
    //    Entity가 바뀌어도 DTO/from()만 손보면 API 응답 형태를 유지할 수 있어 프론트와 DB가 덜 엮임.
    // 2) 보안: Entity에는 s3ImageKey, id 등 클라이언트에 줄 필요 없는 값도 포함됨.
    //    DTO로 필요한 필드만 골라 보내면 내부 DB 정보·민감 데이터 노출을 줄일 수 있음.
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getUsername() + "#" + user.getUserTag(),
                user.getProfileImage(),
                user.getIntroduction()
        );
    }
}