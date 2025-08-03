ALTER TABLE Goal ADD COLUMN save_amount BIGINT NOT NULL AFTER target_amount;

delete from user;

-- 테이블 생성
DROP TABLE IF EXISTS FinancialTerm;
CREATE TABLE FinancialTerm (
                               id INT NOT NULL PRIMARY KEY,
                               description VARCHAR(1000) NOT NULL,  -- 용어 설명
                               category VARCHAR(10) NOT NULL,       -- 용어 종류
                               title VARCHAR(50) NOT NULL           -- 용어 이름
);


-- 예시
INSERT INTO FinancialTerm (id, category, title, description)
VALUES
    (1, '예금', '정기예금이란?', '정기예금은 일정 기간 동안 돈을 은행에 맡기고, 만기 시 원금과 이자를 함께 받는 금융상품입니다.'),
    (2, '예금', '예금자보호란?', '예금자보호제도에 따라 1인당 5천만 원까지 보호됩니다.'),
    (3, '적금', '적금이란?', '적금은 일정 기간 동안 일정 금액을 정기적으로 저축하는 금융상품입니다.'),
    (4, '보험', '보험이란?', '보험은 위험에 대비해 일정 금액을 납입하고, 사고 발생 시 보장받는 금융상품입니다.'),
    (5, '세금', '소득세란?', '소득에 부과되는 세금입니다.'),
    (6, '예금', '입출금 예금이란?', '수시로 돈을 넣고 뺄 수 있는 예금 상품으로, 자유로운 입출금이 특징입니다.'),
    (7, '예금', 'MMDA란?', '수시 입출금이 가능하면서도 비교적 높은 금리를 제공하는 예금 상품입니다. 보통 예치 금액이 클수록 높은 금리가 적용됩니다.'),
    (8, '예금', 'CMA란?', '증권사에서 발행하는 종합자산관리계좌로, 하루만 맡겨도 이자가 붙고 수시 입출금이 가능합니다. RP형, MMF형 등 다양한 유형이 있습니다.'),
    (9, '보험', '실손보험이란?', '질병이나 상해로 인해 발생한 의료비(입원, 통원, 약제비 등)를 실제로 지출한 만큼 보상해주는 보험입니다.'),
    (10, '보험', '종신보험이란?', '피보험자가 사망할 때까지 평생 보장하며, 사망 시 유가족에게 보험금이 지급되는 보험입니다.'),
    (11, '보험', '연금보험이란?', '노후 대비를 위해 일정 기간 보험료를 납입하고, 노후에 연금 형태로 보험금을 지급받는 보험입니다.'),
    (12, '보험', '자동차보험이란?', '자동차 사고 발생 시 발생할 수 있는 경제적 손실을 보상해주는 의무 가입 보험입니다.');

ALTER TABLE Inquiry
    ADD COLUMN user_name VARCHAR(100),
    ADD COLUMN view_count INT DEFAULT 0;
