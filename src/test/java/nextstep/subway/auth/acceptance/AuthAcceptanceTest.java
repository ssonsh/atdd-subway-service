package nextstep.subway.auth.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.auth.dto.TokenRequest;
import nextstep.subway.auth.dto.TokenResponse;
import nextstep.subway.auth.infrastructure.JwtTokenProvider;
import nextstep.subway.member.dto.MemberRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthAcceptanceTest extends AcceptanceTest {

    @Autowired
    public JwtTokenProvider jwtTokenProvider;

    public static final String EMAIL = "wjdals300@gmail.com";
    public static final String PASSWORD = "1234";
    public static final int AGE = 32;

    @BeforeEach
    public void setUp() {
        super.setUp();
        // given 회원 등록되어 있음
        회원_등록되어_있음(EMAIL, PASSWORD, AGE);
    }

    @DisplayName("로그인을 확인한다.")
    @Test
    void loginTest() {
        // when
        ExtractableResponse<Response> response = 회원_로그인_요청(EMAIL, PASSWORD);

        // then
        회원_로그인_됨(response);
    }

    @DisplayName("Bearer Auth")
    @Test
    void myInfoWithBearerAuth() {
        // when
        ExtractableResponse<Response> loginResponse = 회원_로그인_요청(EMAIL, PASSWORD);
        TokenResponse tokenResponse = loginResponse.as(TokenResponse.class);
        String accessToken = tokenResponse.getAccessToken();

        // then
        회원_로그인_됨(loginResponse);
        assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
    }

    @DisplayName("Bearer Auth 로그인 실패")
    @Test
    void myInfoWithBadBearerAuth() {
        // when
        ExtractableResponse<Response> response = 회원_로그인_요청(EMAIL, "failed password");

        // then
        회원_로그인_실패됨(response);
    }

    @DisplayName("Bearer Auth 유효하지 않은 토큰")
    @Test
    void myInfoWithWrongBearerAuth() {
        // given
        String failedToken = "failed token";

        // when
        ExtractableResponse<Response> myInfoResponse = 나의_정보_조회(failedToken);

        //then
        나의_정보_조회_실패됨(myInfoResponse);
    }

    public static ExtractableResponse<Response> 회원_등록되어_있음(String email, String password, int age) {
        return 회원_생성_요청(email, password, age);
    }

    public static ExtractableResponse<Response> 회원_생성_요청(String email, String password, int age) {
        MemberRequest memberRequest = new MemberRequest(email, password, age);

        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(memberRequest)
                .when().post("/members")
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 회원_로그인_요청(String email, String password) {
        TokenRequest tokenRequest = new TokenRequest(email, password);

        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(tokenRequest)
                .when().post("/login/token")
                .then().log().all()
                .extract();
    }

    public static void 회원_로그인_됨(ExtractableResponse<Response> response) {
        assertThat(HttpStatus.OK.value()).isEqualTo(response.statusCode());

        TokenResponse tokenResponse = response.as(TokenResponse.class);
        String accessToken = tokenResponse.getAccessToken();

        assertThat(accessToken).isNotBlank();
    }

    public static ExtractableResponse<Response> 나의_정보_조회(String accessToken) {
        return RestAssured
                .given().log().all()
                .auth().oauth2(accessToken)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/members/me")
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 나의_정보_수정_요청(String accessToken, String newEmail, String newPassword, int newAge) {
        MemberRequest memberRequest = new MemberRequest(newEmail, newPassword, newAge);

        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(memberRequest)
                .auth().oauth2(accessToken)
                .when().put("/members/me")
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 나의_정보_삭제_요청(String accessToken) {
        return RestAssured
                .given().log().all()
                .auth().oauth2(accessToken)
                .when()
                .delete("/members/me")
                .then().log().all()
                .extract();
    }

    public static void 나의_정보_조회됨(ExtractableResponse<Response> myInfoResponse) {
        assertThat(HttpStatus.OK.value()).isEqualTo(myInfoResponse.statusCode());
    }

    public static void 회원_로그인_실패됨(ExtractableResponse<Response> response) {
        assertThat(HttpStatus.UNAUTHORIZED.value()).isEqualTo(response.statusCode());
    }

    public static void 나의_정보_조회_실패됨(ExtractableResponse<Response> myInfoResponse) {
        assertThat(HttpStatus.INTERNAL_SERVER_ERROR.value()).isEqualTo(myInfoResponse.statusCode());
    }

    public static void 나의_정보_수정됨(ExtractableResponse<Response> myInfoUpdateResponse) {
        assertThat(HttpStatus.OK.value()).isEqualTo(myInfoUpdateResponse.statusCode());
    }

    public static void 나의_정보_삭제됨(ExtractableResponse<Response> myInfoDeleteResponse) {
        assertThat(myInfoDeleteResponse.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }
}