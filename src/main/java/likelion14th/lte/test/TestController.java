package likelion14th.lte.test;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="Test",description =  "테스트용 API 입니다")
@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/health")
    public String health() {return  "likelion 14th 화이팅";}

    @GetMapping("/item/{itemId}")
    public  String getItem(
            @Parameter(description = "조회할 ID",example = "1")
            @PathVariable Long itemID
    ){
        return  itemID + "빈";
    }
}