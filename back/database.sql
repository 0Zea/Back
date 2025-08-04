-- 테이블 생성
DROP TABLE IF EXISTS Quiz;
CREATE TABLE Quiz (
                      quiz_id   INT NOT NULL PRIMARY KEY AUTO_INCREMENT,  -- 퀴즈 ID (자동 증가)
                      question  VARCHAR(255) NOT NULL,                    -- 문제
                      answer    VARCHAR(50) NOT NULL,                     -- 답 (O, X, 또는 단답)
                      quiz_type VARCHAR(50) NOT NULL                      -- 문제 유형 (OX, short)
);

-- 예시(OX)
INSERT INTO Quiz (question, answer, quiz_type)
VALUES
    ('소득이 많을수록 세금도 많아진다.', 'O', 'OX'),
    ('비과세 금융상품은 세금을 더 낸다.', 'X', 'OX'),
    ('청년 우대형 청약통장은 34세 이하만 가입 가능하다.', 'O', 'OX'),
    ('소득이 없으면 연말정산을 할 수 없다.', 'X', 'OX'),
    ('종합소득세 신고는 매년 5월이다.', 'O', 'OX');

-- 단답형 퀴즈 데이터
INSERT INTO Quiz (question, answer, quiz_type)
VALUES
    ('한국의 기준금리를 결정하는 기관은?', '한국은행', 'short'),
    ('예금자보호법에 의해 보호받는 예금액 한도는?', '5000만원', 'short'),
    ('P2P 대출의 정식 명칭은?', '온라인투자연계금융업', 'short'),
    ('연 복리 5%로 100만원을 10년 투자했을 때의 최종 금액은? (만원 단위)', '163', 'short'),
    ('코스닥의 정식 명칭은?', '코리아증권딜러자동호가', 'short');


-- 테이블 생성
DROP TABLE IF EXISTS UserQuiz;
CREATE TABLE UserQuiz (
                          user_quiz_id  BINARY(16) NOT NULL PRIMARY KEY,   -- 응답 고유 ID (UUID)
                          user_id       BINARY(16) NOT NULL,               -- 사용자 ID (FK)
                          quiz_id       INT NOT NULL,                      -- 퀴즈 번호 (FK)
                          is_correct    BOOLEAN NOT NULL,                  -- 정답 여부 (nullable)
                          answered_at   TIMESTAMP NOT NULL,                -- 응답 일시 (nullable)

                          FOREIGN KEY (user_id) REFERENCES USER(user_id),
                          FOREIGN KEY (quiz_id) REFERENCES Quiz(quiz_id)
);


-- 예시
INSERT INTO UserQuiz (
    user_quiz_id, user_id, quiz_id, is_correct, answered_at
) VALUES
      (
          UNHEX(REPLACE(UUID(), '-', '')),
          (SELECT user_id FROM USER WHERE email = 'soyeon@example.com'),
          1, 1, '2025-07-30 08:15:23'
      ),
      (
          UNHEX(REPLACE(UUID(), '-', '')),
          (SELECT user_id FROM USER WHERE email = 'soyeon@example.com'),
          2, 0, '2025-07-31 08:17:01'
      ),
      (
          UNHEX(REPLACE(UUID(), '-', '')),
          (SELECT user_id FROM USER WHERE email = 'testuser@example.com'),
          1, 1, '2025-06-29 09:00:40'
      ),
      (
          UNHEX(REPLACE(UUID(), '-', '')),
          (SELECT user_id FROM USER WHERE email = 'testuser@example.com'),
          3, 1, '2025-07-30 09:02:00'
      ),
      (
          UNHEX(REPLACE(UUID(), '-', '')),
          (SELECT user_id FROM USER WHERE email = 'testuser@example.com'),
          5, 1, '2025-07-31 10:30:59'
      );
