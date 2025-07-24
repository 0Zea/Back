<html>
<head>
    <meta charset="UTF-8">
    <title>AllAccount 테스트</title>
    <script>
        function generateConnectedId() {
            // UUID-like 문자열 생성 (임시 예시, 실제 구현은 필요에 맞게)
            return 'conn-' + Date.now().toString(36) + '-' + Math.random().toString(36).substring(2, 10);
        }

        async function sendRequest() {
            const connectedId = generateConnectedId(); // 자동 생성
            const organization = document.getElementById("organization").value;

            const requestData = {
                connectedId: connectedId,
                organization: organization
            };

            console.log("전송할 데이터:", requestData);

            const response = await fetch("/AllAccount/accounts", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(requestData)
            });

            const result = await response.text();
            document.getElementById("result").textContent = result;
        }
    </script>
</head>
<body>
<h2>AllAccount 테스트</h2>

<!-- connectedId 입력 제거됨 -->
<label for="organization">Organization:</label>
<input type="text" id="organization" /><br/><br/>

<button onclick="sendRequest()">전송</button>

<h3>응답 결과</h3>
<pre id="result"></pre>
</body>
</html>