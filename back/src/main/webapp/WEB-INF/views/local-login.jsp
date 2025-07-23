<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>Local Login</title>
</head>
<body>
<h1>로컬 로그인</h1>

<!-- 로그인 폼 -->
<form id="localLoginForm">
    <label for="email">이메일:</label>
    <input type="email" id="email" name="email" required /><br><br>

    <label for="password">비밀번호:</label>
    <input type="password" id="password" name="password" required /><br><br>

    <button type="submit">로그인</button>
</form>

<p><a href="/">홈으로 돌아가기</a></p>

<script>
    document.getElementById("localLoginForm").addEventListener("submit", async function (e) {
        e.preventDefault();

        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;

        try {
            const response = await fetch("/api/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ email, password })
            });

            if (!response.ok) {
                const errorText = await response.text();
                alert("로그인 실패: " + errorText);
                return;
            }

            const result = await response.json();
            console.log("로그인 성공:", result);

            // JWT 토큰 저장 (필요하다면)
            localStorage.setItem("jwtToken", result.token);

            alert("로그인 성공!");
            window.location.href = "/main"; // 로그인 성공 후 메인 페이지로 이동
        } catch (error) {
            console.error("로그인 에러:", error);
            alert("로그인 중 오류 발생: " + error.message);
        }
    });
</script>
</body>
</html>
