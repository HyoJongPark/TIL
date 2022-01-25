# MySQL 기초

> [생활코딩 Database2-MySQL](https://opentutorials.org/module/3300)
> 

## 데이터베이스의 목적

- 스프레드시트와 마찬가지로 데이터를 표의 형태로 표현해준다. 그러나 가장 큰 차이점은 데이터베이스는 코딩(프로그래밍 언어)를 통해서 제어할 수 있다.
- 관계형 데이터베이스(RDB)는 SQL(Structured Query Language) 언어를 통해서 데이터를 제어할 수 있다.
- 데이터 베이스를 구축하면, 저장된 데이터를 웹, 앱을 통해서 공유할 수 있고, 인공지능, 빅데이터를 이용해 분석할 수도 있다.
- 데이터베이스의 정보를 웹사이트를 통해 볼수 있게할 수도, 웹사이트를 통해 정보를 데이터 베이스에 저장할 수도 있다.

---

## MySQL 설치

설치와 초기 불러오는 과정은 opentutorials egoing 님의 강의를 첨부함

[https://opentutorials.org/course/3161/19532](https://opentutorials.org/course/3161/19532)

---

## MySQL의 구조

MySQL 혹은 관계형 데이터베이스들은 엑셀, 스프레드시트와 비슷한 구조를 가지며, 3개 정도의 구성요소가 있다.

- 표 (table)
    - 정보를 저장하는 곳
- 데이터베이스 (database = schema)
    - 연관된 표들을 grouping 해서 연관되어 있지 않은 표들과 구분, 일종의 폴더 역할
- 데이터베이스 서버(database server)
    - database(schema) 들을 저장
    - MySQL 을 설치한 것은 database 서버를 설치한 것

---

## 서버 접속

- 파일은 운영체제만 뚫리면 임의 수정이 가능해 보안에 취약하다. 데이터베이스는 자체 보안 체계를 갖추어 보다 안전하게 데이터를 보관할 수 있다.
- 권한 기능이 있어 MySQL 에 여러 사용자를 등록할 수 있다.
    - 각 사용자들에게 권한을 부여해 차등적으로 권한을 부여할 수 있다.
- `./mysql -uroot -p` 를 입력하면 `Enter password` 라고 나오며 비밀번호를 입력하면 접속할 수 있다.
    - `./mysql -uroot -p` 뒤에 바로 비밀번호를 입력할 수 있으나 보안상 좋지 않은 방법이다.
    - `root` 는 사용자 명, 위 구문의 의미는 `root` 사용자로 MySQL 에 접속 하겠다는 의미.

---

## 스키마(schema)의 사용

MySQL 공식 문서 : [https://dev.mysql.com/doc/refman/8.0/en/](https://dev.mysql.com/doc/refman/8.0/en/)

- `CREATE DATABASE databaseName;` : 해당 이름의 데이터베이스(스키마)를 생성
- `DROP DATABASE databaseName;` :  해당 이름의 데이터베이스(스키마) 삭제
- `SHOW DATABASES;` , `SHOW SCHEMAS`: 데이터베이스(스키마) 조회
- `USE databaseName;` : 사용할 데이터베이스(스키마) 선택

대문자로 작성되어있지만, 소문자도 상관없다.

---

## SQL 테이블과 구조

- SQL
    - Structured
        - 관계형 데이터베이스는 표의 형식으로 정보를 정리정돈하는데, 이것을 구조화 되었다고 한다.
    - Query
        - 데이터베이스에게 데이터 조회, 삭제등을 요청하는 것을 질의한다고 한다.
    - Language
        - 데이터베이스 서버와 유저가 공동으로 사용하는 언어다.
- table(표)
    - x 축
        - row(행), record
        - 데이터자체를 나타낸다.
    - y 축
        - column(열)
        - 데이터의 타입, 구조를 나타낸다.

---

## 테이블의 생성

엑셀과 데이터베이스의 큰 차이점은 데이터 형식이다. 엑셀은 어떤 형태의 데이터든 넣을 수 있다. 

데이터베이스는  해당 열(column)에 데이터 타입을 지정해 사용자가 입력할 수 있는 타입을 강제할 수 있다.

MySQL 의 데이터 타입 : [https://www.mysqltutorial.org/mysql-data-types.aspx](https://www.mysqltutorial.org/mysql-data-types.aspx)

```sql
create table topic(
  id INT(11) NOT NULL AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  description TEXT NULL,
  created DATETIME NOT NULL,
  author VARCHAR(30) NULL,
  profile VARCHAR(100) NULL
  PRIMARY KEY(id)
);
```

---

## MySQL의 CRUD

- CREATE
- READ
- UPDATE
- DELETE
- CREATE, READ 는 데이터베이스라면 반드시 가지고 있다.
- UPDATE, DELETE 는 분야에 따라 없을 수도 있다.
    - 예를들어 회계, 역사 기록

---

## INSERT 구문

- 해당 테이블의 구조 조회

```sql
DESC tableName
```

- 해당 테이블에 데이터를 추가

```sql
INSERT INTO topic (title,description,created,author,profile)
VALUES('MySQL','MySQL is ...',NOW(),'hyojong','developer');`
```

---

## SELECT 구문

- 해당 테이블의 모든 데이터(raw) 조회

```sql
SECLECT * FROM topic;
```

- 해당 테이블의 특정 데이터 조회

```sql
SECLECT id,author FROM topic;
```

- author = park 인 데이터 조회

```sql
SELECT * FROM topic WHERE author='park';
```

- 해당 column 에 대해 오름차순 정렬(DESC)

```sql
SELECT * FROM topic WHERE author='hyojong' ORDER BY id DESC;
```

- 해당 column 에 대해 내림차순 정렬(ASC)

```sql
SELECT * FROM topic WHERE author='hyojong' ORDER BY id ASC;
```

- 상위 n개의 행만 출력

```sql
SELECT * FROM topic WHERE author='hyojong' ORDER BY id DESC LIMIT 2;
```

---

## UPDATE 구문

- 특정 데이터의 값 변경

```sql
UPDATE topic SET discription='PostgreSQL is ...', title='ORACLE' WHERE id=2;
```

여기서 `SET` 뒤의 값은 변경할 데이터의 타입과 값을 입력하고, `WHERE` 뒤의 값은 변경할 데이터의 위치 혹은 특성을 나타낸다.

`WHERE` 을 입력하지 않으면 모든 데이터에 대해 변경 작업을 실시하고, 만약 동일한 특성을 가지는 데이터가 여러개면 그들 모두에 대해서 변경을 실시한다.

---

## DELETE 구문

- 특정 데이터 삭제

```sql
DELECT FROM topic WHERE id=5;
```

---

## 관계형 데이터베이스의 필요성

```sql
+----+------------+-------------------+---------------------+-----------+-----------+
| id | title      | description       | created             | author    | profile   |
+----+------------+-------------------+---------------------+-----------+-----------+
|  1 | MySQL      | MySQL is ...      | 2022-01-25 12:55:28 | hyojong   | developer |
|  2 | ORACLE     | PostgreSQL is ... | 2022-01-25 12:59:30 | hyojong   | developer |
|  3 | SQL Server | SQL Server is ... | 2022-01-25 12:59:54 | kean      | none      |
|  4 | PostgreSQL | PostgreSQL is ... | 2022-01-25 13:00:23 | lean      | scientist |
|  5 | MongoDB    | MongoDB is  ...   | 2022-01-25 14:08:04 | dele alli | student   |
+----+------------+-------------------+---------------------+-----------+-----------+
```

- 해당 테이블의 profile 을 보면 데이터의 중복이 발생한 것을 알 수 있다. 만약 이처럼 데이터가 중복되고 있다면, 개선이 필요하다는 강력한 증거이다.
- 중복이 발생한 데이터가 굉장히 많다면, 수정, 추가, 조회에서도 많은 비용이 발생하고, 데이터의 구분이 힘들어 질 수 있다.
- 중복을 해결하기 위해서 중복이 발생한 author 를 새로운 테이블로 만들고, 기존 테이블에는 식별자인 author_id 를 사용하도록 한다.
    - 이렇게 함으로써 중복된 데이터들을 수정할 때에도 모든 데이터를 수정하는 것이 아닌 author 테이블의 하나의 데이터만 수정하면 된다. - 유지보수가 편해짐
    - 다만 이제 topic 테이블에서 작성자에 대한 정보를 알아볼 수 없게되었다. JOIN 문으로 결합해 해결

---

## 데이터 분리하기

- 테이블 이름 변경(백업을 위해)

```sql
RENAME topic TO topic_backup
```

- 새로 생성된 author 테이블

```sql
+----+--------+---------------------------+
| id | name   | profile                   |
+----+--------+---------------------------+
|  1 | egoing | developer                 |
|  2 | duru   | database administrator    |
|  3 | taeho  | data scientist, developer |
+----+--------+---------------------------+
```

- 새로 생성된 topic 테이블

```sql
+----+------------+-------------------+---------------------+-----------+
| id | title      | description       | created             | author_id |
+----+------------+-------------------+---------------------+-----------+
|  1 | MySQL      | MySQL is...       | 2018-01-01 12:10:11 |         1 |
|  2 | Oracle     | Oracle is ...     | 2018-01-03 13:01:10 |         1 |
|  3 | SQL Server | SQL Server is ... | 2018-01-20 11:01:10 |         2 |
|  4 | PostgreSQL | PostgreSQL is ... | 2018-01-23 01:03:03 |         3 |
|  5 | MongoDB    | MongoDB is ...    | 2018-01-30 12:31:03 |         1 |
+----+------------+-------------------+---------------------+-----------+
```

---

## JOIN 구문

- 두개의 테이블을 해당되는 기준에 따라서 결합

```sql
SELECT * FROM topic LEFT JOIN author ON topic.author_id=author.id;
```

- 결합된 결과

```sql
+----+------------+-------------------+---------------------+-----------+------+--------+---------------------------+
| id | title      | description       | created             | author_id | id   | name   | profile                   |
+----+------------+-------------------+---------------------+-----------+------+--------+---------------------------+
|  1 | MySQL      | MySQL is...       | 2018-01-01 12:10:11 |         1 |    1 | egoing | developer                 |
|  2 | Oracle     | Oracle is ...     | 2018-01-03 13:01:10 |         1 |    1 | egoing | developer                 |
|  3 | SQL Server | SQL Server is ... | 2018-01-20 11:01:10 |         2 |    2 | duru   | database administrator    |
|  4 | PostgreSQL | PostgreSQL is ... | 2018-01-23 01:03:03 |         3 |    3 | taeho  | data scientist, developer |
|  5 | MongoDB    | MongoDB is ...    | 2018-01-30 12:31:03 |         1 |    1 | egoing | developer                 |
+----+------------+-------------------+---------------------+-----------+------+--------+---------------------------+
```

`LEFT JOIN author ON topic.author_id=author.id;` : topic 테이블에 author 테이블을 결합한다. 이때 결합 기준은 author_id 와 author.id 가 같은 데이터를 왼쪽에 추가한다.

- 중복된 데이터 타입이 있을 때 표현법

```sql
SELECT topic.id AS topic_id,title,description,created,name,profile FROM topic LEFT JOIN author ON topic.author_id=author.id;
```

- 출력 결과

```sql
+----------+------------+-------------------+---------------------+--------+---------------------------+
| topic_id | title      | description       | created             | name   | profile                   |
+----------+------------+-------------------+---------------------+--------+---------------------------+
|        1 | MySQL      | MySQL is...       | 2018-01-01 12:10:11 | egoing | developer                 |
|        2 | Oracle     | Oracle is ...     | 2018-01-03 13:01:10 | egoing | developer                 |
|        3 | SQL Server | SQL Server is ... | 2018-01-20 11:01:10 | duru   | database administrator    |
|        4 | PostgreSQL | PostgreSQL is ... | 2018-01-23 01:03:03 | taeho  | data scientist, developer |
|        5 | MongoDB    | MongoDB is ...    | 2018-01-30 12:31:03 | egoing | developer                 |
+----------+------------+-------------------+---------------------+--------+---------------------------+
```

두개의 테이블에는 `id` 라는 같은 데이터 타입이 존재하기 때문에 출력을 원할 때는 `topic.id` 와 같이 명시해주어야 한다.

출력되는 타입의 이름을 변경하고 싶은경우 `topic.id AS topic_id` 와 같이 작성하면 출력되는 이름만 변경된다.

---
