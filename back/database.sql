-- USER 테이블 생성
DROP TABLE IF EXISTS USER;
CREATE TABLE USER (
    user_id BINARY(16) NOT NULL PRIMARY KEY,  -- 사용자 ID (UUID)
    name VARCHAR(100) NOT NULL,               -- 사용자 이름
    email VARCHAR(255) NOT NULL UNIQUE,       -- 이메일 (고유)
    password VARCHAR(255),                    -- 비밀번호 (카카오 로그인 시 NULL)
    mbti VARCHAR(10) DEFAULT '미입력',        -- MBTI
    phone_num VARCHAR(20) DEFAULT '000-0000-0000',  -- 전화번호
    birth_date DATE,                          -- 생년월일
    sex VARCHAR(10) DEFAULT 'female',         -- 성별
    salary BIGINT DEFAULT 0,                  -- 연봉
    pay_amount BIGINT DEFAULT 0,              -- 월급
    role VARCHAR(20) DEFAULT 'USER',          -- 역할
    kakao_access_token TEXT,                  -- 카카오 액세스 토큰
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 생성일
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  -- 수정일
);

-- USER 테이블 인덱스 생성
CREATE INDEX idx_user_email ON USER(email);
CREATE INDEX idx_user_name ON USER(name);

-- Goal 테이블 생성
DROP TABLE IF EXISTS Goal;
CREATE TABLE Goal (
    goal_id BINARY(16) NOT NULL PRIMARY KEY,  -- 목표 ID (UUID)
    user_id BINARY(16) NOT NULL,              -- 사용자 ID (FK)
    goal_name VARCHAR(100) NOT NULL,          -- 목표 이름
    target_amount BIGINT NOT NULL,            -- 목표 금액
    save_amount BIGINT DEFAULT 0,             -- 현재 저축 금액
    start_date DATE NOT NULL,                 -- 시작일
    end_date DATE NOT NULL,                   -- 종료일
    status VARCHAR(20) DEFAULT 'ACTIVE',      -- 상태
    deposit_date INT DEFAULT 1,               -- 입금일 (1-28)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 생성일
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- 수정일
    
    FOREIGN KEY (user_id) REFERENCES USER(user_id) ON DELETE CASCADE
);

-- Goal 테이블 인덱스 생성
CREATE INDEX idx_goal_user_id ON Goal(user_id);
CREATE INDEX idx_goal_status ON Goal(status);

-- BankAccount 테이블 생성
DROP TABLE IF EXISTS BankAccount;
CREATE TABLE BankAccount (
    account_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,  -- 계좌 ID
    user_id BINARY(16) NOT NULL,                        -- 사용자 ID (FK)
    goal_id BINARY(16),                                 -- 목표 ID (FK, NULL 가능)
    bank_name VARCHAR(100) NOT NULL,                    -- 은행명
    account_num VARCHAR(50) NOT NULL,                   -- 계좌번호
    account_type VARCHAR(50) NOT NULL,                  -- 계좌 유형
    balance BIGINT DEFAULT 0,                           -- 잔액
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,     -- 생성일
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- 수정일
    
    FOREIGN KEY (user_id) REFERENCES USER(user_id) ON DELETE CASCADE,
    FOREIGN KEY (goal_id) REFERENCES Goal(goal_id) ON DELETE SET NULL
);

-- BankAccount 테이블 인덱스 생성
CREATE INDEX idx_bankaccount_user_id ON BankAccount(user_id);
CREATE INDEX idx_bankaccount_goal_id ON BankAccount(goal_id);
CREATE INDEX idx_bankaccount_bank_name ON BankAccount(bank_name);

-- Point 테이블 생성
DROP TABLE IF EXISTS Point;
CREATE TABLE Point (
    point_id BINARY(16) NOT NULL PRIMARY KEY,  -- 포인트 ID (UUID)
    user_id BINARY(16) NOT NULL,               -- 사용자 ID (FK)
    point_amount INT NOT NULL,                  -- 포인트 금액
    type_detail VARCHAR(255),                   -- 상세 내용
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 생성일
    total_amount INT DEFAULT 0,                 -- 누적 총액
    type INT DEFAULT 1,                         -- 타입 (1: 적립, 2: 출금)
    
    FOREIGN KEY (user_id) REFERENCES USER(user_id) ON DELETE CASCADE
);

-- Point 테이블 인덱스 생성
CREATE INDEX idx_point_user_id ON Point(user_id);
CREATE INDEX idx_point_type ON Point(type);
CREATE INDEX idx_point_created_at ON Point(created_at);

-- Term 테이블 생성
DROP TABLE IF EXISTS Term;
CREATE TABLE Term (
    term_id BINARY(16) NOT NULL PRIMARY KEY,  -- 약관 ID (UUID)
    title VARCHAR(200) NOT NULL,              -- 약관 제목
    content TEXT NOT NULL,                    -- 약관 내용
    version VARCHAR(20) NOT NULL,             -- 버전
    is_active BOOLEAN DEFAULT TRUE,           -- 활성화 여부
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 생성일
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  -- 수정일
);

-- Term 테이블 인덱스 생성
CREATE INDEX idx_term_version ON Term(version);
CREATE INDEX idx_term_is_active ON Term(is_active);

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

-- 금융상품 테이블 생성
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    fin_prdt_cd VARCHAR(50) NOT NULL PRIMARY KEY,  -- 금융상품 코드
    dcls_month VARCHAR(6),                         -- 공시 제출월
    fin_co_no VARCHAR(10),                         -- 금융회사 코드
    kor_co_nm VARCHAR(100),                        -- 금융회사 명
    fin_prdt_nm VARCHAR(200),                      -- 금융상품명
    join_way VARCHAR(500),                         -- 가입 방법
    mtrt_int TEXT,                                 -- 만기 후 이자율
    spcl_cnd TEXT,                                 -- 우대조건
    join_deny VARCHAR(10),                         -- 가입제한
    join_member VARCHAR(100),                      -- 가입대상
    etc_note TEXT,                                 -- 기타 유의사항
    max_limit BIGINT,                              -- 최고한도
    dcls_strt_day VARCHAR(8),                      -- 공시 시작일
    dcls_end_day VARCHAR(8),                       -- 공시 종료일
    fin_co_subm_day VARCHAR(14)                    -- 금융회사 제출일
);

-- 금융상품 옵션 테이블 생성
DROP TABLE IF EXISTS productoption;
CREATE TABLE productoption (
    option_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,  -- 옵션 ID
    fin_prdt_cd VARCHAR(50) NOT NULL,                  -- 금융상품 코드 (FK)
    intr_rate_type VARCHAR(10),                        -- 저축 금리 유형
    intr_rate_type_nm VARCHAR(50),                     -- 저축 금리 유형명
    rsrv_type VARCHAR(10),                             -- 적립 유형
    rsrv_type_nm VARCHAR(50),                          -- 적립 유형명
    save_trm INT,                                      -- 저축 기간 (개월)
    intr_rate DECIMAL(5,2),                            -- 저축 금리
    intr_rate2 DECIMAL(5,2),                           -- 최고 우대금리
    
    FOREIGN KEY (fin_prdt_cd) REFERENCES product(fin_prdt_cd) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX idx_product_fin_co_no ON product(fin_co_no);
CREATE INDEX idx_product_kor_co_nm ON product(kor_co_nm);
CREATE INDEX idx_productoption_fin_prdt_cd ON productoption(fin_prdt_cd);
CREATE INDEX idx_productoption_save_trm ON productoption(save_trm);
CREATE INDEX idx_productoption_intr_rate ON productoption(intr_rate);


ALTER TABLE product ADD COLUMN summary TEXT NULL;

DESC product;
SELECT summary
FROM product
WHERE fin_prdt_cd = '4';

UPDATE product
SET summary = '만기 1년, 자유적립 가능, 최고 우대금리 제공(조건 충족 시).'
WHERE fin_prdt_cd = '4';


SELECT fin_prdt_cd, summary
FROM product
WHERE fin_prdt_cd = '01012000200000000004';


ALTER TABLE Quiz
    ADD COLUMN explanation VARCHAR(1000) NULL AFTER quiz_type;

CREATE INDEX idx_userquiz_userid_date ON UserQuiz (user_id, answered_at);