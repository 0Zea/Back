<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>금융 MBTI 설문</title>
    <style>
        table { border-collapse: collapse; margin-bottom: 20px; }
        th, td { border: 1px solid #aaa; padding: 8px 12px; text-align: center; }
    </style>
</head>
<body>
<h1>금융 MBTI 설문</h1>
<table>
    <tr><th>기준</th><th>빠름 (신속한)</th><th>느림 (신중한)</th></tr>
    <tr><th>하이 리스크 (승부사)</th><td>신속한 승부사</td><td>신중한 승부사</td></tr>
    <tr><th>로우 리스크 (분석가)</th><td>신속한 분석가</td><td>신중한 분석가</td></tr>
</table>
<form id="mbtiForm" method="post" action="/mbti-survey">
    <input type="hidden" name="email" value="${email}" />
    <label>1. 월급을 받았을 때 나는?</label><br>
    <input type="radio" name="speed1" value="fast" required> 사고 싶었던 걸 바로 지른다 (빠름 +4)<br>
    <input type="radio" name="speed1" value="slow"> 지출계획을 세우고 천천히 쓴다 (느림 +4)<br><br>

    <label>2. 앱에서 투자 상품을 볼 때 나는?</label><br>
    <input type="radio" name="speed2" value="fast" required> 직관적으로 빠르게 결정한다 (빠름 +4)<br>
    <input type="radio" name="speed2" value="slow"> 여러 번 비교하고 분석 후 결정한다 (느림 +4)<br><br>

    <label>3. 새 금융 서비스를 알게 되면?</label><br>
    <input type="radio" name="speed3" value="fast" required> 일단 써보면서 배운다 (빠름 +4)<br>
    <input type="radio" name="speed3" value="slow"> 리뷰와 후기 충분히 보고 결정한다 (느림 +4)<br><br>

    <label>4. 투자할 때 나는?</label><br>
    <input type="radio" name="risk1" value="high" required> 수익이 크면 리스크도 감수한다 (하이리스크 +4)<br>
    <input type="radio" name="risk1" value="low"> 안정적이고 꾸준한 수익을 선호한다 (로우리스크 +4)<br><br>

    <label>5. 재테크 수단을 고를 때 나는?</label><br>
    <input type="radio" name="risk2" value="high" required> 새로운 코인, 스타트업 투자도 도전 (하이리스크 +4)<br>
    <input type="radio" name="risk2" value="low"> 예금, 적금 등 확실한 수단을 선택 (로우리스크 +4)<br><br>

    <label>6. 손실 가능성이 있는 상황에서 나는?</label><br>
    <input type="radio" name="risk3" value="high" required> 손해 나더라도 기회라면 베팅 (하이리스크 +4)<br>
    <input type="radio" name="risk3" value="low"> 손해는 피하고 확실한 걸 고른다 (로우리스크 +4)<br><br>

    <button type="submit">제출</button>
</form>
<script>
    // 폼 제출 시 점수 계산 및 mbti 유형 결정(예시)
    document.getElementById('mbtiForm').addEventListener('submit', function(e) {
        // 실제 서버 저장은 그대로 POST, 클라이언트에서 미리 결과를 보여주고 싶으면 아래 코드 활용
        // e.preventDefault();
        // let fast = 0, slow = 0, high = 0, low = 0;
        // if (document.querySelector('input[name="speed1"]:checked').value === 'fast') fast += 4; else slow += 4;
        // ... (동일하게 각 문항별로 점수 합산)
        // 결과에 따라 유형 결정 로직 추가 가능
    });
</script>
</body>
</html>

