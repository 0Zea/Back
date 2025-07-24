<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>추가 정보 입력</title>
</head>
<body>
<h2>추가 정보 입력</h2>
<form action="/additional-info" method="post">
    <input type="hidden" name="email" value="${email}" />
    <label>이름: <input type="text" name="name" required></label><br>
    <label>전화번호: <input type="text" name="phoneNum" required></label><br>
    <label>MBTI: <input type="text" name="mbti" required></label><br>
    <label>생년월일: <input type="date" name="birthDate" required></label><br>
    <label>성별: 
        <select name="sex" required>
            <option value="female">여성</option>
            <option value="male">남성</option>
        </select>
    </label><br>
    <label>월급: <input type="number" name="salary" required></label><br>
    <label>지출비: <input type="number" name="payAmount" required></label><br>
    <button type="submit">저장</button>
</form>
</body>
</html> 