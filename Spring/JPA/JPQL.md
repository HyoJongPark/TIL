# JPQL

- JPQL 은 객체지향 쿼리 언어다. 따라서 테이블을 대상으로 쿼리하는 것이 아니라 엔티티 객체를 대상으로 쿼리한다.
- JPQL 은 SQL을 추상화해서 특정 데이터베이스 SQL에 의존하지 않는다.
- JPQL은 결국 SQL로 변환된다.

# 기본 문법과 쿼리 API

JPQL도 SQL과 비슷하게 SELECT, UPDATE, DELETE 문을 사용할 수 있다. 엔티티 저장 시에는 `EntityManager.persist()` 를 사용하기 때문에 INSERT 문은 없다.

### JPQL 문법

```java
select m from Member as m where m.username = 'Hello'
```

- 대소문자 구분
    - 엔티티와 속성은 대소문자를 구분한다. 반면에 `select, where` 같은 JPQL 키워드는 대소문자를 구분하지 않는다.
- 엔티티 이름
    - JPQL에서 사용한 `Member` 는 클래스 명이 아닌 엔티티 이름이다.
- 별칭은 필수
    - JPQL은 별칭을 필수로 사용해야 한다. (`m` )
    - 별칭을 지정할 때 `as` 는 생략 가능하다.

### JPQL 쿼리 생성

작성한 JPQL을 실행하려면 쿼리 객체를 만들어야 한다. `TypedQuery` , `Query` 가 쿼리 객체다.

반환 타입을 명확하게 지정할 수 있으면 `TypedQuery` 객체를 사용하고, 그렇지 않다면 `Query` 객체를 사용한다.

```java
//타입 지정
TypedQuery<Member> query = 
	em.createQuery("select m from Member m", Member.class);
//타입 미지정
Query query = 
	em.createQuery("select m.username, m.age from Member m");
```

- `em.createQuery` 의 두 번째 파라미터에 반환할 타입을 지정하면 `TypedQuery` 를 반환하고 지정하지 않으면 `Query` 를 반환한다.
- 조회 대상이 여러개라면 반환할 타입이 명확하지 않으므로 `Query` 객체를 사용해야 한다.
    - 조회 대상이 둘 이상일 때 쿼리 실행 결과는 `Object[]` 를 반환한다.

### 결과 조회

JPQL 쿼리를 특정 메서드를 사용해 호출하면 실제 쿼리를 실행해서 데이터베이스를 조회한다.

- `query.getResultList()` : 결과를 List로 반환. 결과가 없으면 빈 컬렉션을 반환
- `query.getSingleResult()` : 겨로가가 정확히 하나일 때 사용
    - 결과가 없으면 `NoResultException` 발생
    - 결과가 둘 이상이면 `NonUniqueResultException` 발생

---

# 파라미터 바인딩

JDBC는 위치 기준 파라미터 바인딩만 지원하지만 JPQL은 이름 기준 파라미터 바인딩도 지원한다.

위치 기준 파라미터 방식보다는 이름 기준 파라미터 바인딩 방식을 사용하는 것이 더 명확하다.

### 이름 기준 파라미터 바인딩

```java
List<Member> result = 
	em.createQuery("select m from Member m where m.username = :username", Member.class)
		.setParameter("username", "Park");
		.getResultList();
```

- `:username` 이라는 이름 기준 파라미터를 정의한다.
- `setParameter()` 에서 파라미터를 바인딩한다.

### 위치 기준 파라미터 바인딩

```java
List<Member> result = 
	em.createQuery("select m from Member m where m.username = ?1", Member.class)
		.setParameter(1, "Park");
		.getResultList();
```

- 위치 기준 파라미터 바인딩을 사용하려면 `?` 다음 위치 값을 부여한다.
    - 위치 값은 1부터 시작

> JPQL을 수정해서 직접 문자를 더해 만들어 넣으면 악의적인 사용자에 의해 SQL 인젝션 공격을 당할 수 있다.
> 
> 
> 또한 성능 이슈도 있는데 파라미터 바인딩 방식을 사용하면 파라미터의 값이 달라도 같은 쿼리로 인식해서 JPA는 JPQL을 SQL로 파싱한 결과를 재사용할 수 있다. 그리고 데이터베이스도 내부에서 실행한 SQL을 파싱해서 사용하는데 같은 쿼리는 파싱한 결과를 재사용할 수 있다. 결과적으로 애플리케이션과 데이터베이스 모두 해당 쿼리의 파싱 결과를 재사용할 수 있어 성능이 향상된다.
> 
> ```java
> //직접 JPQL 작성
> "select m from Member m where m.username = '" + usernameParam + "'"
> ```
> 

---

# 프로젝션

SELECT 절에 조회할 대상을 지정하는 것을 프로젝션이라 한다. 프로젝션 대상은 엔티티, 임베디드 타입, 스칼라 타입이 있다.

- 엔티티 프로젝션

```java
select m from Member m
select m.team from Member m
```

둘 다 엔티티를 프로젝션 대상으로 사용한다. 조회한 엔티티는 영속성 컨텍스트에서 관리된다.

- 임베디드 타입 프로젝션

```java
select o.address from Order o
```

임베디드 타입은 조회의 시작점이 될 수 없다. 따라서 여기서는 `Order` 엔티티를 시작점으로 임베디드 타입을 조회할 수 있다.

임베디드 타입은 엔티티 타입이 아닌 값 타입이다. 따라서 이렇게 조회한 임베디드 타입은 영속성 컨텍스트에서 관리되지 않는다.

- 스칼라 타입 프로젝션

```java
select username from Member m
//중복 제거
select distinct username from Member m
```

- 여러 값 조회

```java
List<Object[]> result = 
	em.createQuery("select m.username, m.age from Member m")
		.getResultList();
```

앞서 말했듯 프로젝션에 여러 값을 선택하면 `Query` 를 사용해야 한다.

- NEW 명령어

```java
List<MemberDTO> result = 
	em.createQuery("select new jpabook.jpql.MemberDTO(m.username, m.age) from Member m",
		MemberDTO.class)
		.getResultList();
```

실제 개발 시에는 여러 값을 조회할 때 `Object[]` 같은 타입이 아닌 DTO처럼 의미있는 객체로 변환해서 사용할 것이다. 

이때 SELECT 다음에 NEW 명령어를 사용하면 반환받을 클래스를 지정할 수 있는데, 이 클래스의 생성자에 JPQL 조회 결과를 넘겨 줄 수 있다.

---

# 페이징 API

JPA는 페이징을 두개의 API로 추상화한다.

- `setFirstResult(int startPosition)` : 조회 시작 위치
- `setMaxResults(int maxResult)` : 조회할 데이터 수

**페이징 사용**

```java
List<Member> result = 
	em.createQuery("select m from Member m order by m.username DESC", Member.class)
		.setFirstResult(10)
		.setMaxResult(20)
		.getResultList();
```

해당 코드는 11번째 값 부터 30번째 값 까지 조회한다. 

페이징 API의 큰 장점은 데이터베이스마다 다른 페이징 처리를 같은 API로 처리할 수 있는 것은 데이터베이스 방언 덕분이다. 

---

# 집합과 정렬

집합은 집합함수와 함께 통계 정보를 구할 때 사용한다. 

```java
select
	count(m),   //회원 수
	sum(m.age), //나이 합
	avg(m.age), //나이 평균
	max(m.age), //최대 나이
	min(m.age), //최소 나이
from Member m
```

### **집합함수**

| 함수 | 설명 | 반환 타입 |
| --- | --- | --- |
| COUNT | 결과 수를 구한다. | Long |
| MAX, MIN | 최대, 최소 값을 구한다. |  |
| AVG | 평균값을 구한다. | Double |
| SUM | 합을 구한다. 숫자 타입만 사용할 수 있다.  | 정수합: Long
소수합: Double
BigInteger 합 : BigInteger
BigDecimal합 : BigDecimal |
- NULL 값은 무시하므로 통계에 잡히지 않는다.
- 값이 없는데 집합 함수를 사용하면 NULL 값이 된다. 단, COUNT는 0 이다.
- DISTINCT를 집합 함수 안에 사용해서 사용 가능 하지만, COUNT 에서 사용할 때 임베디드 타입은 지원하지 않는다.

### 그룹화(**GROUP BY, HAVING)**

`GROUP BY` 는 통계 데이터를 구할 때 특정 데이터 그룹끼리 묶어준다.

`HAVING` 은 `GROUP BY` 로 그룹화한 통계 데이터를 기준으로 필터링 한다.

```sql
select t.name, COUNT(m.age), AVG(m.age), MAX(m.age), MIN(m.age)
	from Member m left join m.team t
	GROUP BY t.name
	HAVING AVG(m.age) >= 10
```

- 다음 코드는 팀 이름을 기준으로 그룹화 하고, 평균 나이가 10살 이상인 그룹을 조회한다.

### 정렬(ORDER BY)

`ORDER BY` 는 결과를 정렬할 때 사용한다. 

```sql
select m from Member m
	ORDER BY m.age DESC, m.username ASC
```

- 다음 코드는 나이를 기준으로 내림차순으로 정렬하고, 나이가 같으면 이름을 기준ㅇ로 오름차순 절렬한다.

---

# JPQL 조인

JPQL도 조인을 지원하는데 SQL 조인과 기능은 같고 문법만 다르다.

### 내부 조인

내부 조인은 `INNER JOIN` 을 사용한다. (`INNER` 생략 가능)

```sql
select m from Member m 
	INNER JOIN m.team where t.name = :teamName
```

- 회원과 팀을 내부 조인해서 `teamName` 에 소속된 회원을 조회하는 JPQL
- JPQL은 `JOIN` 명령어 다음에 조인할 객체의 연관 필드를 사용한다. SQL 조인 처럼 사용하면 문법 오류가 발생

### 외부 조인

외부 조인은 `LEFT OUTER JOIN` 으로 사용한다. (`OUTER` 생략 가능)

```sql
select m from Member m
	LEFT OUTER JOIN m.team t
```

- 외부 조인은 기능상 SQL의 외부 조인과 같다.

### 컬렉션 조인

일대다 관계나 다대다 관계처럼 컬렉션을 사용하는 곳에 조인하는 것을 컬렉션 조인이라고 한다.

- [회원→팀] 으로의 조인은 다대일 조인이면서 단일 값 연관 필드(`m.team` )를 사용한다.
- [팀→회원] 으로의 조인은 일대다 조인이면서 컬렉션 값 연관 필드(`m.members` )를 사용한다.

```sql
select t, m from Team t
	LEFT JOIN t.members m
```

> 컬렉션 조인 시 `JOIN` 대신 `IN` 을 사용할 수 있는데, 기능상 `JOIN` 과 같지만 컬렉션일 때만 사용할 수 있다.
> 

### 세타 조인

`WHERE` 절을 사용해서 세타 조인을 할 수 있다. 세타 조인은 내부 조인만 지원한다. 

세타 조인을 사용하면 연관 관계가 없는 엔티티도 조회할 수 있다.

```sql
select count(m) from Member m, Team t
	where m.username = t.name
```

### JOIN ON 절

JPA 2.1부터 조인 시에 `ON` 절을 지원한다. `ON` 절을 사용하면 조인 대상을 필터링하고 조인할 수 있다.

내부 조인의 `ON` 절은 `WHERE` 절을 사용할 때와 결과가 같아 대부분 외부 조인에서만 사용한다.

```sql
select m, t from Member m
	left join m.team on t.name = 'A'
```

---

# 페치 조인

페치 조인은 연관된 엔티티나 컬렉션을 한 번에 조회하는 기능인데 `JOIN FETCH` 명령어로 사용할 수 있다.

페치 조인은 SQL에서 이야기하는 조인의 종류가 아닌 JPQL에서 성능 최적화를 위해 제공하는 기능이다.

### 엔티티 페치 조인

```sql
select m from Member m
	join fetch m.team
```

- 페치 조인을 사용해서 회원 엔티티를 조회하면서 연관된 팀 엔티티도 함께 조회하는 JPQL이다.
- 일반 JPQL 조인과 달리 `m.team` 다음 별칭을 사용할 수 없다.
- 지연 로딩 전략에서도 팀과 회원을 함께 조회했기 때문에 프록시가 아닌 실제 엔티티 객체다.

> 하이버네이트에서는 페치 조인에도 별칭 사용을 허용한다.
> 

**문제점**

![Untitled](https://user-images.githubusercontent.com/75190035/168020265-25e5b8ba-1ade-4d0c-896f-a38be7a278d3.png)

- `TeamA` 는 하나지만 회원 테이블과 조인하면서 결과가 증가해 같은 팀이 2건 조회되었다.

### 컬렉션 페치 조인

```sql
select t from Team t
	join fetch t.members where t.name = 'TeamA'
```

- 팀을 조회하며, 팀 이름이 `TeamA` 인 회원들을 함께 조회하는 JPQL이다.

### 페치 조인과 DISTINCT

SQL의 `DISTINCT` 는 중복된 결과를 제거하는 명령이다. JPQL의 `DISTINCT` 명령어는 SQL에 `DISTINCT` 를 추가하는 것과 애플리케이션에서 한 번 더 중복을 제거한다.

```sql
select distinct t from Team t
	join fetch t.members where t.name = 'TeamA'
```

- 이전 엔티티 페치 조인과 달리 같은 팀 엔티티의 중복도 제거한다.

### 한계점

페치 조인을 사용하면 SQL 한 번으로 연관된 엔티티들을 함께 조회할 수 있어 SQL 호출 수를 줄여 성능을 최적화 할 수 있다. 

페치 조인은 글로벌 로딩 전략(엔티티의 `fetch` 속성)보다 우선한다. 따라서 필요한 경우에만 해당 엔티티를 마치 즉시 로딩 전략을 사용한 것 처럼 조회할 수 있다. 또한 연관된 엔티티를 쿼리 시점에 조회하므로 지연 로딩이 발생하지 않는다. 따라서 준영속 상태에서도 객체 그래프를 탐색할 수 있다.

**한계점**

- 페치 조인 대상에는 별칭을 줄 수 없다.
- 둘 이상의 컬렉션을 페치할 수 없다.
- 컬렉션을 페치 조인하면 페이징 API를 사용할 수 없다.
    - 컬렉션이 아닌 단일 값 연관 필드들은 페이징 API를 사용할 수 있다.

---

# 경로 표현식

경로 표현식은 `.` 을 찍어 객체 그래프를 탐색하는 것이다.

- 상태 필드 : 단순히 값을 저장하기 위한 필드(필드 or 프로퍼티)
    - 예) `t.username` , `t.age`
- 연관 필드 : 연관관계, 임베디드 타입을 위한 필드(필드 or 프로퍼티)
    - 단일 값 연관관계 필드 : `@ManyToOne` , `@OneToOne` , 대상이 엔티티
        - 예) `m.team`
    - 컬렉션 값 연관 필드 : `@OneToMany` , `@ManyToMany` , 대상이 컬렉션
        - 예) `m.orders`

## 특징

- 상태 필드 경로 : 경로 탐색의 끝, 더는 탐색할 수 없다.

```sql
select m.username, m.age from Member m
```

- 단일 값 연관 경로 : **묵시적으로 내부 조인**이 일어난다. 계속 탐색할 수 있다.

```sql
select o.member.team from Orders o
	where o.product.name = 'productA' and o.address.city = 'JINJU'
```

- 컬렉션 값 연관 경로 : **묵시적으로 내부 조인**이 일어난다. 더는 탐색할 수 없다.
    - `FROM` 절에서 조인을 통해 별칭을 얻으면 별칭으로 탐색할 수 있다.

```sql
select t.members from Team t //성공
select m.username from Team t join t.members m

select t.members.username from Team t //실패
```

> **경로 탐색을 사용한 묵시적 조인 시 주의사항**
> 
> - 항상 내부 조인이다.
> - 컬렉션은 경로 탐색의 끝이다. 컬렉션에서 경로 탐색을 하려면 명시적으로 조인해서 별칭을 얻어야 한다.
> - 경로 탐색은 주로 `SELECT` , `WHERE` 절에서 사용하지만 묵시적 조인으로 인해 SQL의 `FROM` 절에 영향을 준다.

---

# 서브 쿼리

JPQL도 SQL처럼 서브 쿼리를 지원한다. 

단, 제약이 있는데 서브 쿼리를 `WHERE` , `HAVING` 절에서만 사용할 수 있고 `SELECT` , `FROM` 절에서는 사용할 수 없다.

> 하이버네이트에서는 `SELECT` 절 까지의 서브 쿼리도 허용한다.
> 

### 서브 쿼리 함수

- `EXISTS` : 서브 쿼리에 결과가 존재하면 참이다.

```sql
select m from Member m
	where exists (select t from m.team t where t.name = 'TeamA')
```

- `{ALL | ANY | SOME}` : 비교 연산자와 같이 사용
    - `ALL` : 조건을 모두 만족하면 참
    - `ANY` , `SOME` : 둘은 같은 의미, 조건을 하나라도 만족하면 참

```sql
select o from Order o
	where o.orderAmount > ALL (select p.stockAmount form product p)
```

- `IN` : 서브 쿼리의 결과 중 하나라도 같은 것이 있으면 참

```sql
select t from Team t
	where t IN (select t2 from Team t2 JOIN t2.members m2 where m2.age >= 20)
```

---

# 다형성 쿼리

JPQL로 부모 엔티티를 조회하면 자식 엔티티도 함께 조회된다. 물론 조회 전략에 따라 실행되는 SQL도 다르다.

- 단일 테이블 전략(`SINGLE_TABLE` ) 일 때 SQL

```sql
select * from item
```

- 조인 전략(`JOINED` ) 일 때 SQL

```sql
select
	i.ITEM_ID, i.DTYPE, i.name, i.price, i.stockQuantity,
	b.author, b.isbn,
	a.artist, a.etc,
	m.actor, m.director
from
	Item i 
		left outer join Book b on i.ITEM_ID=b.ITEM_ID
		left outer join Album a on i.ITEM_ID=a.ITEM_ID
		left outer join Movie m on i.ITEM_ID=m.ITEM_ID
```

### TYPE

`TYPE` 은 엔티티의 상속 구조에서 조회 대상을 특정 자식 타입으로 한정할 때 주로 사용한다.

```sql
select i from Item i where type(i) IN (Book, Movie)
```

- Item 중에 Book, Movie 를 조회하는 JPQL

### TREAT

`TREAT` 는 JPA 2.1에 추가된 기능이며, 자바의 타입 캐스팅과 비슷하다. 상속 구조에서 부모 타입을 특정 자식 타입으로 다룰 때 사용한다.

JPA 표준은 `FROM` , `WHERE` 절에서 사용할 수 있지만, 하이버네이트는 `SELECT` 절에서도 사용하는 것을 허용한다.

```sql
select i from Item i where treate(i as Book).author = 'kim'
```

- 부모 타입인 Item 을 자식 타입인 Book으로 다룬다. 따라서 author 필드에도 접근 가능하다.

---

# 사용자 정의 함수

JPA 2.1 부터 사용자 정의 함수를 지원한다. 하이버네이트 구현체를 사용하면 방언 클래스를 상속해서 구현하고 사용할 데이터베이스 함수를 미리 등록해야 한다.

**방언 클래스 상속**

```java
public class MyH2Dialect extends H2Dialect {
	public MyH2Dialect() {
		registerFunction("group_concat", 
			new StandardSQLFuncion("group_concat", StandardBasicTypes.STRING));
	}
}
```

- 구현한 방언 클래스는 스프링 빈으로 등록해야 동작한다.

**사용**

```sql
select function('group_concat', i.name) from item i

//하이버네이트를 사용하면 축약 가능
select group_concat(i.name) from item i
```

---

# 기타 정리

- enum 은 `=` 비교 연산만 지원한다.
- 임베디드 타입은 비교를 지원하지 않는다.

### EMPTY STRING

JPA 표준은 ‘’을 길이 0 인 `Empty String` 으로 정했지만 데이터베이스에 따라 NULL로 사용하는 데이터도 있으므로 확인하고 사용해야 한다.

### NULL 정의

- 조건을 만족하는 데이터가 하나도 없으면 NULL 이다.
- NULL은 알 수 없는 값이다. NULL 과의 모든 수학적 계산 결과는 NULL이다.
    - `Null == Null` 은 알 수 없는 값
    - `Null is Null` 은 참

---

# 엔티티 직접 사용

### 기본 키 값

객체 인스턴스는 참조 값으로 식별하고 테이블 로우는 기본 키 값으로 식별한다.

따라서 JPQL에서 엔티티 객체를 직접 사용하면 SQL에서는 해당 엔티티의 기본 키 값을 사용한다.

- JPQL

```sql
select count(m.id) from Member m //엔티티의 아이디 사용
select count(m) from Member m //엔티티 직접 사용
```

- 실행된 SQL

```sql
select count(m.id) as cnt from Member m
```

JPQL 사용 시에 엔티티의 아이디를 사용하던, 직접 사용하던 결국 SQL에서는 엔티티의 기본 키를 사용한다. 따라서 실행된 SQL은 같다.

### 외래 키 값

- JPQL

```java
Team team = em.find(Team.class, 1L);

List result1 = 
	em.createQuery("select m from Member m where m.team = :team")
		.setParameter("team", team)
		.getResultList();

List result2 = 
	em.createQuery("select m from Member m where m.team.id = :teamId")
		.setParameter("teamId", 1L)
		.getResultList();
```

- 실행된 SQL

```sql
select m.* from Member m where m.team_id=?
```

예제에서 `m.team.id` 를 호출할 때 묵시적 조인이 일어날 것 같지만, `Member` 테이블이 이미 `team_id` 외래키를 가지고 있기 때문에 묵시적 조인은 일어나지 않는다.

---

# Named 쿼리: 정적 쿼리

JPQL 쿼리는 크게 동적 쿼리와 정적 쿼리로 나눌 수 있다.

- 동적 쿼리 : `em.createQuery("...")` 처럼 JPQL을 문자로 완성해서 직접 넘기는 것을 동적 쿼리라 한다. 런타임에 특정 조건에 따라 JPQL을 동적으로 구성할 수 있다.
- 정적 쿼리 : 미리 정의한 쿼리에 이름을 부여해서 필요할 때 사용할 수 있는데 이것을 **Named 쿼리**라 한다. Named 쿼리는 한 번 정의하면 변경할 수 없는 정적인 쿼리다.

Named 쿼리는 애플리케이션 로딩 시점에 JPQL 문법을 체크하고 미리 파싱해 둔다. 따라서 오류를 빨리 확인할 수 있고, 사용 시점에는 파싱된 결과를 재사용하므로 성능상 이점도 있다. 또한 정적인 SQL이 생성되므로 데이터베이스의 조회 성능 최적화에도 도움을 준다.

### Named 쿼리를 애노테이션에 정의

Named 쿼리는 `@NamedQuery` 애노테이션을 사용해 정의할 수 있다. 만약 둘 이상의 Named 쿼리를 정의하려면 `@NamedQueries` 애노테이션을 사용한다.

`@NamedQuery` 애노테이션

```java
@Target({TYPE})
public @interface NamedQuery {
	String name();  //Named 쿼리 이름(필수)
	String query(); //JPQL 정의 (필수)
	LockModeType lockMode default NONE; //쿼리 실행시 락 모드를 설정
	QueryHint[] hints() default {}; //JPA 구현체에 쿼리 힌트를 줄 수 있다.
```

**정의 - 하나의 Named 쿼리**

```java
@Entity
@NamedQuery(
	name = "Member.findByUsername",
	query= "select m from Member m where m.username = :username")
public class member {...}
```

- `name` 속성에 이름을 부여한다.
    - `Member.~~` 이라고 정의했는데 Named 쿼리는 영속성 유닛 단위로 관리되므로 충돌을 방지하기 위해 엔티티 이름을 준 것이다. 관리상 이점도 있다.

**정의 - 둘 이상의 Named 쿼리**

```java
@Entity
@NamedQueries({
	@NamedQuery(
		name = "Member.findByUsername",
		query= "select m from Member m where m.username = :username")
	@NamedQuery(
		name = "Member.findByAge",
		query= "select m from Member m where m.age = :age")
})
public class Member {...}
```

**사용**

```java
List<Member> result = 
	em.createQuery("Member.findByUsername", Member.class)
		.setParameter("username", "Member1")
		.getResultList();
```

### Named 쿼리를 XML에 정의

JPA 에서 애노테이션으로 작성할 수 있는 것은 XML로도 가능하다. 애노테이션이 직관적이고 편하지만 Named 쿼리 작성에서 XML이 편할 수 있다.

또한 XML과 애노테이션에 같은 설정이 있으면 **XML이 우선권을 가진다.**

**XML 정의**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-mappings xmlns="http://xmlns.jcp.org/xml/ns/persistence/orm"
	version="2.1">
	<named-query name="Member.findByUsername">
		<query><CDATA[
			select m from Member m
			where m.username = :username
		]></query>
	</named-query>
</entity-mappings>
```

XML을 정의한 후에도 해당 xml을 인식하도록 `META/persistence.xml` 에 다음 코드를 추가해야 한다.

**XML 등록**

```xml
<persistence-unit name="jpabook">
	<mapping-file>META-INF/namedQuery.xml</mapping-file>
...
```
