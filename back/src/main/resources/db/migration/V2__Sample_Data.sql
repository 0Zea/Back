-- 샘플 데이터 삽입
-- V2__Sample_Data.sql

-- 퀴즈 샘플 데이터
INSERT INTO Quiz (question, answer, quiz_type) VALUES
    ('소득이 많을수록 세금도 많아진다.', 'O', 'OX'),
    ('비과세 금융상품은 세금을 더 낸다.', 'X', 'OX'),
    ('청년 우대형 청약통장은 34세 이하만 가입 가능하다.', 'O', 'OX'),
    ('소득이 없으면 연말정산을 할 수 없다.', 'X', 'OX'),
    ('종합소득세 신고는 매년 5월이다.', 'O', 'OX'),
    ('한국의 기준금리를 결정하는 기관은?', '한국은행', 'short'),
    ('예금자보호법에 의해 보호받는 예금액 한도는?', '5000만원', 'short'),
    ('P2P 대출의 정식 명칭은?', '온라인투자연계금융업', 'short'),
    ('연 복리 5%로 100만원을 10년 투자했을 때의 최종 금액은? (만원 단위)', '163', 'short'),
    ('코스닥의 정식 명칭은?', '코리아증권딜러자동호가', 'short');

-- 테스트 사용자 데이터
INSERT INTO USER (user_id, email, password, name, phone_num, birth_date, sex, mbti, salary, pay_amount, role) VALUES
    (UNHEX(REPLACE(UUID(), '-', '')), 'testuser@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '테스트 사용자', '010-1234-5678', '1990-01-01', 'male', 'INTJ', 30000000, 2500000, 'USER'),
    (UNHEX(REPLACE(UUID(), '-', '')), 'soyeon@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '소연', '010-9876-5432', '1995-05-15', 'female', 'ENFP', 35000000, 3000000, 'USER');

-- 공지사항 샘플 데이터
INSERT INTO Notice (notice_id, title, content, author, view_count) VALUES
    (UNHEX(REPLACE(UUID(), '-', '')), 'OZEA 서비스 오픈 안내', '안녕하세요! OZEA 서비스가 정식 오픈되었습니다. 많은 관심과 이용 부탁드립니다.', '관리자', 150),
    (UNHEX(REPLACE(UUID(), '-', '')), '시스템 점검 안내', '2024년 1월 15일 새벽 2시부터 4시까지 시스템 점검이 있을 예정입니다.', '시스템팀', 89),
    (UNHEX(REPLACE(UUID(), '-', '')), '새로운 기능 업데이트', '자산 관리 기능이 새롭게 업데이트되었습니다. 더욱 편리한 서비스를 이용해보세요.', '개발팀', 234);

-- 목표 샘플 데이터
INSERT INTO Goal (goal_id, user_id, title, description, target_amount, current_amount, target_date, goal_type, status) 
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    u.user_id,
    '집 구매 목표',
    '내년까지 집 구매를 위한 저축 목표입니다.',
    50000000,
    15000000,
    '2025-12-31',
    'SAVINGS',
    'ACTIVE'
FROM USER u WHERE u.email = 'testuser@example.com';

INSERT INTO Goal (goal_id, user_id, title, description, target_amount, current_amount, target_date, goal_type, status)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    u.user_id,
    '여행 자금',
    '일본 여행을 위한 자금 모으기',
    3000000,
    500000,
    '2024-06-30',
    'TRAVEL',
    'ACTIVE'
FROM USER u WHERE u.email = 'soyeon@example.com';

-- BankAccount 테이블에 테스트 데이터 추가 (API에서 사용하는 테이블)
INSERT INTO BankAccount (account_id, user_id, account_num, account_type, balance)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    u.user_id,
    '110-123456-789',
    '입출금통장',
    5000000
FROM USER u WHERE u.email = 'testuser@example.com';

INSERT INTO BankAccount (account_id, user_id, account_num, account_type, balance)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    u.user_id,
    '123-456789-012',
    '적금통장',
    10000000
FROM USER u WHERE u.email = 'testuser@example.com';

INSERT INTO BankAccount (account_id, user_id, account_num, account_type, balance)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    u.user_id,
    '456-789012-345',
    '예금통장',
    3000000
FROM USER u WHERE u.email = 'testuser@example.com';

-- soyeon 사용자용 BankAccount 데이터
INSERT INTO BankAccount (account_id, user_id, account_num, account_type, balance)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    u.user_id,
    '789-012345-678',
    '입출금통장',
    8000000
FROM USER u WHERE u.email = 'soyeon@example.com';

INSERT INTO BankAccount (account_id, user_id, account_num, account_type, balance)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    u.user_id,
    '321-654321-098',
    '청약통장',
    15000000
FROM USER u WHERE u.email = 'soyeon@example.com';

-- 자산 샘플 데이터 (기존 Asset 테이블)
INSERT INTO Asset (asset_id, user_id, bank_name, account_type, account_number, balance, asset_type)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    u.user_id,
    '신한은행',
    '입출금',
    '110-123456-789',
    5000000,
    'CHECKING'
FROM USER u WHERE u.email = 'testuser@example.com';

INSERT INTO Asset (asset_id, user_id, bank_name, account_type, account_number, balance, asset_type)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    u.user_id,
    '국민은행',
    '적금',
    '123-456789-012',
    10000000,
    'SAVINGS'
FROM USER u WHERE u.email = 'testuser@example.com';

-- 포인트 샘플 데이터
INSERT INTO Point (point_id, user_id, amount, point_type, description)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    u.user_id,
    100,
    'SIGNUP',
    '회원가입 보너스'
FROM USER u WHERE u.email = 'testuser@example.com';

INSERT INTO Point (point_id, user_id, amount, point_type, description)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    u.user_id,
    50,
    'QUIZ',
    '퀴즈 정답 보상'
FROM USER u WHERE u.email = 'testuser@example.com'; 