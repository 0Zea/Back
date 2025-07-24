<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>회원가입</title>
</head>
<body>
<h1>회원가입</h1>

<form id="signupForm">
  <label for="name">이름:</label>
  <input type="text" id="name" required><br><br>

  <label for="email">이메일:</label>
  <input type="email" id="email" required><br><br>

  <label for="password">비밀번호:</label>
  <input type="password" id="password" required><br><br>

  <label for="mbti">MBTI:</label>
  <input type="text" id="mbti" required><br><br>

  <label for="phoneNum">전화번호:</label>
  <input type="text" id="phoneNum" placeholder="010-1234-5678" required><br><br>

  <label for="birthDate">생년월일:</label>
  <input type="date" id="birthDate" required><br><br>

  <label for="sex">성별:</label>
  <select id="sex" required>
    <option value="female">여성</option>
    <option value="male">남성</option>
  </select><br><br>

  <label for="salary">연봉:</label>
  <input type="number" id="salary" required><br><br>

  <label for="payAmount">월 지출금액:</label>
  <input type="number" id="payAmount" required><br><br>

  <input type="hidden" id="role" value="user">

  <button type="submit">가입하기</button>
</form>

<p><a href="/">홈으로</a></p>

<script>
  document.getElementById("signupForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const data = {
      name: document.getElementById("name").value,
      email: document.getElementById("email").value,
      password: document.getElementById("password").value,
      mbti: document.getElementById("mbti").value,
      phoneNum: document.getElementById("phoneNum").value,
      birthDate: document.getElementById("birthDate").value,
      sex: document.getElementById("sex").value,
      salary: parseInt(document.getElementById("salary").value),
      payAmount: parseInt(document.getElementById("payAmount").value),
      role: document.getElementById("role").value
    };

    try {
      const response = await fetch("/api/auth/signup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
      });

      if (!response.ok) {
        const error = await response.text();
        alert("회원가입 실패: " + error);
        return;
      }

      alert("회원가입 성공!");
      window.location.href = "/local-login"; // 회원가입 후 로그인 페이지로 이동
    } catch (error) {
      console.error("회원가입 오류:", error);
      alert("오류 발생: " + error.message);
    }
  });
</script>
</body>
</html>
