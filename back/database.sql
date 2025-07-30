use kongzea;
drop table if exists Inquiry;
create table Inquiry(
info_id BINARY(16) NOT NULL primary key,
user_id BINARY(16) NOT NULL,
user_name varchar(255) not null,
content varchar(1000) not null,
title varchar(500) not null,
is_answered boolean NOT null,
created_at TIMESTAMP NOT null,
answered_content VARcHAR(1000) null,
foreign key(user_id) references USER(user_id)
);

-- Point 테이블 추가
DROP TABLE IF EXISTS Point;
CREATE TABLE Point (
    point_id BINARY(16) NOT NULL PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    point_amount INT NOT NULL,
    type_detail VARCHAR(40) NOT NULL, -- 상세 내역
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount INT NOT NULL,
    type INT NOT NULL, -- 1: 적립, 2: 출금
    FOREIGN KEY(user_id) REFERENCES USER(user_id)
);

-- BankCode 테이블 추가
DROP TABLE IF EXISTS BankCode;
CREATE TABLE BankCode (
    bank_code VARCHAR(10) NOT NULL PRIMARY KEY,
    bank_name VARCHAR(100) NOT NULL,
    bank_icon VARCHAR(255)
);

-- 은행 코드 데이터 삽입
INSERT INTO BankCode (bank_code, bank_name, bank_icon) VALUES
('001', 'KB국민', '/images/banks/kb.png'),
('002', '신한', '/images/banks/shinhan.png'),
('003', '하나', '/images/banks/hana.png'),
('004', '우리', '/images/banks/woori.png'),
('005', 'NH농협', '/images/banks/nh.png'),
('006', 'IBK기업', '/images/banks/ibk.png'),
('007', 'KDB산업', '/images/banks/kdb.png'),
('008', '신협', '/images/banks/shinhyup.png'),
('009', 'SC제일', '/images/banks/sc.png'),
('010', '우정사업본부', '/images/banks/post.png'),
('011', '부산', '/images/banks/busan.png'),
('012', 'iM뱅크', '/images/banks/im.png');

-- 샘플 포인트 데이터 추가 (테스트용)
INSERT INTO Point (point_id, user_id, point_amount, type_detail, created_at, total_amount, type) VALUES
(UNHEX(REPLACE(UUID(), '-', '')), UNHEX(REPLACE('ac7b2a8c-6a65-4ccf-aa88-327c20c38f27', '-', '')), 10000, '챌린지 성공', '2024-07-14 15:32:00', 10000, 1),
(UNHEX(REPLACE(UUID(), '-', '')), UNHEX(REPLACE('ac7b2a8c-6a65-4ccf-aa88-327c20c38f27', '-', '')), 5000, '퀴즈 완료', '2024-07-12 12:00:00', 15000, 1),
(UNHEX(REPLACE(UUID(), '-', '')), UNHEX(REPLACE('ac7b2a8c-6a65-4ccf-aa88-327c20c38f27', '-', '')), 20000, '환급', '2024-07-10 10:30:00', -5000, 2),
(UNHEX(REPLACE(UUID(), '-', '')), UNHEX(REPLACE('ac7b2a8c-6a65-4ccf-aa88-327c20c38f27', '-', '')), 8000, '목표 달성', '2024-07-08 14:20:00', 15000, 1),
(UNHEX(REPLACE(UUID(), '-', '')), UNHEX(REPLACE('ac7b2a8c-6a65-4ccf-aa88-327c20c38f27', '-', '')), 15000, '출금', '2024-07-05 16:45:00', 0, 2);

# INSERT INTO user
# VALUES (
#            UNHEX(REPLACE(UUID(), '-', '')), -- 또는 UUID_TO_BIN(UUID())
#            '김콕재',
#            'jun@naver.com',
#            '12',
#            'intp',
#            '01000000000',
#            '2000-01-16',

#            'female',
#            10000000,
#            1000000,
#            'user'
#        );
# INSERT INTO user
#          VALUES (
#                     UNHEX(REPLACE(UUID(), '-', '')), -- 또는 UUID_TO_BIN(UUID())
#                     '운영자',
#                     'root@naver.com',
#                     '12',
#                     'intp',
#                     '01000000000',
#                     '2999-01-16',
#                     'female',
#                     10000000,
#                     1000000,
#                     'admin'
#                 );
select * from user;
# 24a0f5c7-66d7-11f0-8ab4-8cb0e9d84583 김콕재 user
# 3e7db2f4-66d7-11f0-8ab4-8cb0e9d84583 운영자 admin
select * from inquiry;
INSERT INTO inquiry
VALUES (
           UNHEX(REPLACE(UUID(), '-', '')),  -- UUID를 BINARY(16)으로 변환
           UNHEX(REPLACE('24a0f5c7-66d7-11f0-8ab4-8cb0e9d84583', '-', '')),  -- 예시 user_id (실제로는 존재하는 user_id 사용)
           '김콕재',
           '문의 내용입니다.',
           '문의 제목입니다.',
           false,
           '2024-07-01 15:30:00',  -- 원하는 TIMESTAMP 값
           null
       );
INSERT INTO inquiry
VALUES (
           UNHEX(REPLACE(UUID(), '-', '')),  -- UUID를 BINARY(16)으로 변환
           UNHEX(REPLACE('24a0f5c7-66d7-11f0-8ab4-8cb0e9d84583', '-', '')),  -- 예시 user_id (실제로는 존재하는 user_id 사용)
           '김콕재',
           '제 금융 정보가 업데이트가 안됩니다 ㅜㅜ',
           '금융 정보 업데이트 오류',
           false,
           '2024-07-01 15:50:00',  -- 원하는 TIMESTAMP 값
           null
       );
select *
    from inquiry
    where info_id = 'a94d8367-66e0-11f0-8ab4-8cb0e9d84583';
select * from inquiry order by created_at desc;
INSERT INTO inquiry
                      VALUES (
                                 UNHEX(REPLACE(UUID(), '-', '')),  -- UUID를 BINARY(16)으로 변환
                                 UNHEX(REPLACE('24a0f5c7-66d7-11f0-8ab4-8cb0e9d84583', '-', '')),  -- 예시 user_id (실제로는 존재하는 user_id 사용)
                                 '김콕재',
                                 '제 금융 정보가 업데이트가 안됩니다 ㅜㅜ제 금융 정보가 업데이트가 안됩니다 ㅜㅜ제 금융 정보가 업데이트가 안됩니다 ㅜㅜ제 금융 정보가 업데이트가 안됩니다 ㅜㅜ제 금융 정보가 업데이트가 안됩니다 ㅜㅜ제 금융 정보가 업데이트가 안됩니다 ㅜㅜ제 금융 정보가 업데이트가 안됩니다 ㅜㅜ제 금융 정보가 업데이트가 안됩니다 ㅜㅜ제 금융 정보가 업데이트가 안됩니다 ㅜㅜ',
                                 'content 최대 길이',
                                 false,
                                 '2024-07-01 15:50:00',  -- 원하는 TIMESTAMP 값
                                 null
                             );
SELECT *
FROM inquiry
WHERE title LIKE CONCAT('%문의%') ORDER BY created_at DESC;
drop table if exists Inquiry;
create table Inquiry(
                        info_id BINARY(16) NOT NULL primary key,
                        user_id BINARY(16) NOT NULL,
                        user_name varchar(255) not null,
                        content varchar(1000) not null,
                        title varchar(500) not null,
                        is_answered boolean NOT null,
                        created_at TIMESTAMP NOT null,
                        answered_content VARcHAR(1000) null,
                        foreign key(user_id) references USER(user_id)
);
select * from inquiry where info_id='1afe6ef6-67b0-11f0-b1df-8cb0e9d84583';
select * from inquiry;
drop table if exists Inquiry;
CREATE TABLE Inquiry (
                         info_id BINARY(16) NOT NULL PRIMARY KEY,
                         user_id BINARY(16) NOT NULL,
                         user_name VARCHAR(255) NOT NULL,
                         content VARCHAR(1000) NOT NULL,
                         title VARCHAR(500) NOT NULL,
                         is_answered BOOLEAN NOT NULL,
                         created_at DATETIME DEFAULT now(),
                         answered_content VARCHAR(1000) NULL,
                         view_count INT NOT NULL DEFAULT 0,
                         FOREIGN KEY(user_id) REFERENCES USER(user_id)
);
DROP TABLE IF EXISTS notice;
create table notice(
                       notice_id BINARY(16) NOT NULL primary key,
                       admin_id BINARY(16) NOT NULL,
                       title varchar(500) not null,
                       content varchar(1000) not null,
                       created_at DATETIME DEFAULT now(),
                       foreign key(admin_id) references USER(user_id)
);
select * from notice;

