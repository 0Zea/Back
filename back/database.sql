CREATE TABLE `Transaction` (
                               `transaction_id`	INT	NOT NULL	COMMENT '인덱스 자동 증가+1',
                               `account_id`	INT	NOT NULL	COMMENT '인덱스 자동 증가+1',
                               `ts_type`	VARCHAR(255)	NOT NULL	COMMENT '거래 타입(입금, 출금, 상환)',
                               `amount`	BIGINT	NOT NULL	COMMENT '거래 금액',
                               `ts_date`	DATETIME	NOT NULL	COMMENT '거래 일시',
                               `memo`	VARCHAR(255)	NULL	COMMENT '거래 내역'
);

CREATE TABLE `BankCode` (
                            `bank_code`	VARCHAR(10)	NOT NULL	COMMENT '해당 은행 코드',
                            `bank_icon`	VARCHAR(100)	NOT NULL	COMMENT '아이콘 URL',
                            `bank_name`	VARCHAR(10)	NOT NULL	COMMENT '은행 이름'
);

CREATE TABLE `Point` (
                         `point_id`	BINARY(16)	NOT NULL	COMMENT 'UUID(식별자)',
                         `user_id`	BINARY(16)	NOT NULL	COMMENT 'UUID(식별자)',
                         `point_amount`	INT	NOT NULL	COMMENT '적립 및 환급 포인트',
                         `typeDetail`	VARCHAR(40)	NOT NULL	COMMENT '상세내역',
                         `created_at`	TIMESTAMP	NOT NULL	COMMENT '적립한 시간',
                         `total_amount`	INT	NOT NULL	COMMENT '소유한 포인트',
                         `type`	INT	NOT NULL	COMMENT '적립/출금'
);

CREATE TABLE `Inquiry` (
                           `info_id`	BINARY(16)	NOT NULL	COMMENT 'UUID(식별자)',
                           `user_id`	BINARY(16)	NOT NULL	COMMENT 'UUID(식별자)',
                           `user_name`	VARCHAR(255)	NULL,
                           `content`	VARCHAR(1000)	NOT NULL	COMMENT '문의 내용',
                           `title`	VARCHAR(500)	NOT NULL	COMMENT '문의 제목',
                           `is_answered`	BOOLEAN	NOT NULL	COMMENT '답변 O, X(관리자가 수정 시 True)',
                           `created_at`	TIMESTAMP	NOT NULL	COMMENT '문의 등록일자',
                           `answered_content`	VARCHAR(1000)	NULL	COMMENT '답변 내용',
                           `view_count`	INT	NOT NULL	COMMENT '자주 물어보는 사항 필터'
);

CREATE TABLE `Notice` (
                          `Notice_id`	BINARY(16)	NOT NULL	COMMENT 'UUID(식별자)',
                          `admin_id`	BINARY(16)	NOT NULL	COMMENT 'UUID(식별자)/User.user(admin)',
                          `title`	VARCHAR(500)	NOT NULL	COMMENT '공지사항 제목',
                          `content`	VARCHAR(1000)	NOT NULL	COMMENT '공지사항 내용',
                          `created_at`	DATETIME	NOT NULL	COMMENT '공지사항 등록일자'
);

CREATE TABLE `Product` (
                           `fin_prdt_cd`	VARCHAR(50)	NOT NULL,
                           `dcls_month`	VARCHAR(6)	NOT NULL,
                           `fin_co_no`	VARCHAR(20)	NOT NULL,
                           `kor_co_nm`	VARCHAR(100)	NOT NULL,
                           `fin_prdt_nm`	VARCHAR(255)	NOT NULL,
                           `join_way`	VARCHAR(500)	NULL,
                           `mtrt_int`	VARCHAR(255)	NULL,
                           `spcl_cnd`	VARCHAR(500)	NULL,
                           `join_deny`	VARCHAR(10)	NULL,
                           `join_member`	VARCHAR(255)	NULL,
                           `etc_note`	VARCHAR(1000)	NULL,
                           `max_limit`	BIGINT	NULL,
                           `dcls_strt_day`	VARCHAR(10)	NULL,
                           `dcls_end_day`	VARCHAR(10)	NULL,
                           `fin_co_subm_day`	VARCHAR(14)	NULL
);

CREATE TABLE `Financial Term` (
                                  `id`	INT	NOT NULL,
                                  `description`	VARCHAR(1000)	NOT NULL	COMMENT '용어 설명',
                                  `categoty`	VARCHAR(10)	NOT NULL	COMMENT '용어 종류',
                                  `title`	VARCHAR(50)	NOT NULL	COMMENT '용어 이름'
);

CREATE TABLE `AnnuityOption` (
                                 `option_id`	INT	NULL,
                                 `fin_prdt_cd`	VARCHAR(50)	NOT NULL	COMMENT '금융상품 코드',
                                 `fin_co_no`	varchar(20)	NOT NULL	COMMENT '금융회사코드',
                                 `pnsn_entr_age`	INT	NULL	COMMENT '가입 나이',
                                 `mon_paym_atm`	INT	NULL	COMMENT '월 납입 금액(예: 10 -> 10만원)',
                                 `paym_prd`	INT	NULL	COMMENT '납입 기간 (예: 10 -> 10년)',
                                 `pnsn_strt_age`	INT	NULL	COMMENT '연금 수령 시작 나이 (예: 60 -> 60세)',
                                 `pnsn_recp_amt`	BIGINT	NULL	COMMENT '예상 연금 수령액 (원 단위)',
                                 `pnsn_recp_trm_nm`	VARCHAR(50)	NULL	COMMENT '연금 수령기간 명칭'
);

CREATE TABLE `BankAccount` (
                               `account_id`	INT	NOT NULL	COMMENT 'auto increment',
                               `user_id`	BINARY(16)	NOT NULL	COMMENT 'UUID(식별자)',
                               `goal_id`	BINARY(16)	NULL	COMMENT 'UUID(식별자)',
                               `bank_name`	VARCHAR(100)	NOT NULL	COMMENT '은행이름',
                               `account_num`	VARCHAR(255)	NOT NULL	COMMENT '계좌 번호',
                               `account_type`	VARCHAR(255)	NOT NULL	COMMENT '예/적금,대출',
                               `balance`	BIGINT	NOT NULL	COMMENT '계좌 잔액'
);

CREATE TABLE `ProductOption` (
                                 `option_id`	INT	NULL,
                                 `fin_prdt_cd`	VARCHAR(50)	NOT NULL,
                                 `fin_co_no`	varchar(20)	NOT NULL	COMMENT '금융회사코드',
                                 `pnsn_entr_age`	INT	NULL	COMMENT '가입 나이',
                                 `mon_paym_atm`	INT	NULL	COMMENT '월 납입 금액(예: 10 -> 10만원)',
                                 `paym_prd`	INT	NULL	COMMENT '납입 기간 (예: 10 -> 10년)',
                                 `pnsn_strt_age`	INT	NULL	COMMENT '연금 수령 시작 나이 (예: 60 -> 60세)',
                                 `pnsn_recp_amt`	BIGINT	NULL	COMMENT '예상 연금 수령액 (원 단위)',
                                 `pnsn_recp_trm_nm`	VARCHAR(50)	NULL	COMMENT '연금 수령기간 명칭'
);

CREATE TABLE `Quiz` (
                        `quiz_id`	INT	NOT NULL	COMMENT 'auto increment',
                        `question`	VARCHAR(255)	NOT NULL	COMMENT '문제',
                        `answer`	VARCHAR(50)	NOT NULL	COMMENT '정답(O,X /단답)',
                        `quiz_type`	VARCHAR(50)	NOT NULL	COMMENT 'OX/short'
);

CREATE TABLE `User` (
                        `user_id`	BINARY(16)	NOT NULL	COMMENT 'UUID(식별자)',
                        `name`	VARCHAR(255)	NOT NULL	COMMENT '사용자 이름',
                        `email`	VARCHAR(255)	NOT NULL	COMMENT '사용자 이메일',
                        `password`	VARCHAR(255)	NULL	COMMENT '사용자 비밀번호',
                        `phone_num`	VARCHAR(50)	NOT NULL	COMMENT '전화번호',
                        `mbti`	VARCHAR(10)	NOT NULL	COMMENT '금융성향',
                        `birth_date`	DATE	NOT NULL	COMMENT '사용자 생년월일',
                        `sex`	VARCHAR(10)	NOT NULL	COMMENT '사용자 성별',
                        `salary`	BIGINT	NOT NULL	COMMENT '사용자 연봉',
                        `pay_amount`	BIGINT	NOT NULL	COMMENT '사용자 고정 지출 비용',
                        `role`	VARCHAR(10)	NOT NULL	COMMENT '권한(디폴트 user), or admin',
                        `kakao_access_token`	VARCHAR(500)	NULL	COMMENT '카카오 액세스 토큰'
);

CREATE TABLE `UserQuiz` (
                            `user_quiz_id`	BINARY(16)	NOT NULL	COMMENT 'UUID(식별자)',
                            `user_id`	BINARY(16)	NOT NULL	COMMENT 'UUID(식별자)',
                            `quiz_id`	INT	NOT NULL	COMMENT 'auto increment',
                            `is_correct`	BOOLEAN	NOT NULL	COMMENT '정답 확인',
                            `answered_at`	TIMESTAMP	NOT NULL	COMMENT '하루에 횟수 한 번 제한'
);

CREATE TABLE `Annuity` (
                           `fin_prdt_cd`	VARCHAR(50)	NOT NULL	COMMENT '금융상품 코드',
                           `fin_co_no`	VARCHAR(255)	NOT NULL	COMMENT '금융회사코드',
                           `dcls_month`	varchar(10)	NOT NULL	COMMENT '공시제출원(YYYYMM)',
                           `kor_co_nm`	VARCHAR(255)	NOT NULL	COMMENT '회사명',
                           `fin_prdt_nm`	VARCHAR(255)	NOT NULL	COMMENT '상품명',
                           `join_way`	TEXT	NOT NULL	COMMENT '가입 방법',
                           `pnsn_kind`	VARCHAR(10)	NOT NULL	COMMENT '연금 유형 코드',
                           `pnsn_kind_nm`	VARCHAR(50)	NOT NULL	COMMENT '연금 유형 이름',
                           `mntn_cnt`	BIGINT	NULL	COMMENT '유지 계좌 수 또는 총액',
                           `prdt_type_nm`	VARCHAR(50)	NOT NULL	COMMENT '상품 유형',
                           `avg_prft_rate`	DECIMAL(5, 2)	NULL	COMMENT '평균 수익률 (%)',
                           `dcls_rate`	DECIMAL(5, 2)	NULL	COMMENT '공시 수익률 (%)',
                           `guar_rate`	DECIMAL(5, 2)	NULL	COMMENT '보장 수익률 (%)',
                           `btrm_prft_rate_1`	DECIMAL(5, 2)	NULL	COMMENT '수익률 항목 1',
                           `btrm_prft_rate_2`	DECIMAL(5, 2)	NULL	COMMENT '수익률 항목 2',
                           `btrm_prft_rate_3`	DECIMAL(5, 2)	NULL	COMMENT '수익률 항목 3',
                           `etc`	TEXT	NULL	COMMENT '기타',
                           `dcls_strt_day`	DATE	NULL	COMMENT '공시 시작일',
                           `dcls_end_day`	DATE	NULL	COMMENT '공시 종료일'
);

CREATE TABLE `Goal` (
                        `goal_id`	BINARY(16)	NOT NULL	COMMENT 'UUID(식별자)',
                        `user_id`	BINARY(16)	NOT NULL	COMMENT 'UUID(식별자)',
                        `goal_name`	VARCHAR(255)	NOT NULL	COMMENT '목표 이름',
                        `target_amount`	BIGINT	NOT NULL	COMMENT '목표 금액',
                        `save_amount`	BIGINT	NOT NULL	COMMENT '목표 까지 달성 금액',
                        `start_date`	DATE	NOT NULL	COMMENT '목표 시작일',
                        `end_date`	DATE	NOT NULL	COMMENT '목표 종료일',
                        `status`	VARCHAR(10)	NOT NULL	COMMENT '상태(성공, 진행, 실패)',
                        `deposit_date`	INT	NOT NULL	COMMENT '입금 날짜(프론트에서 처리)'
);

ALTER TABLE `Transaction` ADD CONSTRAINT `PK_TRANSACTION` PRIMARY KEY (
                                                                       `transaction_id`
    );

ALTER TABLE `BankCode` ADD CONSTRAINT `PK_BANKCODE` PRIMARY KEY (
                                                                 `bank_code`
    );

ALTER TABLE `Point` ADD CONSTRAINT `PK_POINT` PRIMARY KEY (
                                                           `point_id`
    );

ALTER TABLE `Inquiry` ADD CONSTRAINT `PK_INQUIRY` PRIMARY KEY (
                                                               `info_id`
    );

ALTER TABLE `Notice` ADD CONSTRAINT `PK_NOTICE` PRIMARY KEY (
                                                             `Notice_id`
    );

ALTER TABLE `Product` ADD CONSTRAINT `PK_PRODUCT` PRIMARY KEY (
                                                               `fin_prdt_cd`
    );

ALTER TABLE `Financial Term` ADD CONSTRAINT `PK_FINANCIAL TERM` PRIMARY KEY (
                                                                             `id`
    );

ALTER TABLE `AnnuityOption` ADD CONSTRAINT `PK_ANNUITYOPTION` PRIMARY KEY (
                                                                           `option_id`
    );

ALTER TABLE `BankAccount` ADD CONSTRAINT `PK_BANKACCOUNT` PRIMARY KEY (
                                                                       `account_id`
    );

ALTER TABLE `ProductOption` ADD CONSTRAINT `PK_PRODUCTOPTION` PRIMARY KEY (
                                                                           `option_id`
    );

ALTER TABLE `Quiz` ADD CONSTRAINT `PK_QUIZ` PRIMARY KEY (
                                                         `quiz_id`
    );

ALTER TABLE `User` ADD CONSTRAINT `PK_USER` PRIMARY KEY (
                                                         `user_id`
    );

ALTER TABLE `UserQuiz` ADD CONSTRAINT `PK_USERQUIZ` PRIMARY KEY (
                                                                 `user_quiz_id`
    );

ALTER TABLE `Annuity` ADD CONSTRAINT `PK_ANNUITY` PRIMARY KEY (
                                                               `fin_prdt_cd`
    );

ALTER TABLE `Goal` ADD CONSTRAINT `PK_GOAL` PRIMARY KEY (
                                                         `goal_id`
    );

