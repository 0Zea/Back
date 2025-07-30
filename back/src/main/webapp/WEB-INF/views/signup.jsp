<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>회원가입</title>
</head>
<body>
<h1>회원가입</h1>

<form id="signupForm" action="/signup" method="post">
  <label for="name">이름:</label>
  <input type="text" id="name" name="name" required><br><br>

  <label for="email">이메일:</label>
  <input type="email" id="email" name="email" required><br><br>

  <label for="password">비밀번호:</label>
  <input type="password" id="password" name="password" required><br><br>

  <button type="submit">가입하기</button>
</form>

<p><a href="/">홈으로</a></p>

</body>
</html>
