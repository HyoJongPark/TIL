# 스프링 데이터 JPA

스프링 데이터 JPA는 스프링 프레임워크에서 JPA를 편리하게 사용할 수 있도록 지원하는 프로젝트이며, 스프링 데이터 프로젝트의 하위 프로젝트 중 하나다.

이 프로젝트로 CRUD를 처리하기 위한 공통 인터페이스를 제공하므로써 반복되는 CRUD 문제를 해결할 수 있다. 데이터 접근 계층을 개발할 때 구현 인터페이스만 작성해도 개발을 완료할 수 있다.

---

# 공통 인터페이스 기능

스프링 데이터 JPA는 간단한 CRUD 기능을 공통으로 처리하는 `JpaRepository` 를 제공한다. 이 인터페이스를 상속받아 간단하게 스프링 데이터 JPA를 사용할 수 있다.

**스프링 데이터 JPA 적용**

```java
public interface MemberRepository extends JpaRepository<Member, Long> {
	Member findByUsername(String username);
}
```

- `JpaRepository<엔티티 명, 식별자 타입>` 을 상속해 사용할 수 있다.
- 직접 작성한 메서드는 스프링 데이터 JPA가 메소드 이름을 분석해 JPQL을 실행한다.
    - `select m from Member m where username = :username`
- `JpaRepository` 계층 구조

<img width="503" alt="Untitled" src="https://user-images.githubusercontent.com/75190035/167563317-87413641-78ae-43b1-a482-571e25ad5b32.png">

---

# 쿼리 메서드 기능

쿼리 메서드 기능은 스프링 데이터 JPA가 제공하는 기능 중 하나다. 대표적으로 메서드 이름만으로 쿼리를 생성하는 기능이 있는데 인터페이스에 메서드만 선언하면 해당 메서드의 이름으로 적절한 JPQL 쿼리를 생성해서 실행한다.

- 스프링 데이터 JPA가 제공하는 쿼리 메서드 기능
    1. 메서드 이름으로 쿼리 생성
    2. 메서드 이름으로 JPA NamedQuery 호출
    3. `@Query` 애노테이션을 사용해서 리포지토리 인터페이스에 쿼리 직접 정의

## 1. 메서드 이름으로 쿼리 생성

```java
public interface MemberRepository extends Repository<Member, Long> {
	List<Member> findByEmailAndName(String email, String name);
}
```

- 이메일과 이름으로 회원을 조회하는 메서드다.
- 실행된 JPQL - `select m from Member m where m.email = ?1 and m.name = ?2`
- 엔티티의 필드명이 변경되면 인터페이스에 정의한 메서드 이름도 꼭 변경해야 한다.

> 메서드 이름으로 쿼리 생성 기능을 이용하려면 정해진 규칙에 따라 메서드 이름을 지어야 하다.
> 
> 
> 스프링 공식 문서에 표(Table 3) 참고.
> 
> [https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation)
> 

## 2. JPA NamedQuery

스프링 데이터 JPA는 메서드 이름으로 JPA Named 쿼리를 호출하는 기능을 제공한다. 

JPA Named 쿼리는 쿼리에 이름을 부여해서 사용하는 방법인데, 애노테이션 혹은 XML에 쿼리를 정의할 수 있다.

**애노테이션으로 쿼리 정의**

```java
@Entity
@NamedQuery(
	name="Member.findByUsername",
	query="select m from Member m where m.username = :username")
public class Member{...}
```

**XML로 쿼리 정의**

```java
<named-query name="Member.findByUsername">
	<query><CDATA[
		select m from Member m where m.username = :username]></query>
<named-query>
```

**스프링 데이터 JPA 로 Named 쿼리 호출**

```java
public interface MemberRepository extends JpaRepository<Member, Long> {
	List<Member> findByUsername(@Param("username") String username);
}
```

- 스프링 데이터 JPA 는 선언한 `도메인 클래스 + . + 메서드 이름` 으로 Named 쿼리를 찾아 실행한다.
- 실행할 Named 쿼리가 없으면 메서드 이름으로 쿼리 생성 전략을 사용한다.

## 3. @Query, 리포지토리에 메서드에 쿼리 정의

```java
public interface MemberRepository extends JpaRepository<Member, Long> {
	@Query("select m from Member m where m.username = ?1")
	Member findByUsername(String username);
}
```

- 리포지토리 메서드에 직접 쿼리를 정의하려면 `@Query` 애노테이션을 사용한다. 이 방법은 실행할 메서드에 정적 쿼리를 직접 작성하므로 이름 없는 Named 쿼리라 할 수 있다.
- JPA Named 쿼리처럼 애플리케이션 실행 시점에 문법 오류를 발견할 수 있다.
- 네이티브 SQL을 사용하려면 `@Query(nativeQuery=true)` 를 설정한다.

> **파라미터 바인딩**
> 
> 
> 스프링 데이터 JPA는 위치 기반, 이름 기반 파라미터 바인딩을 지원한다.
> 
> 기본값은 위치 기반이지만, 코드 가독성과 유지 보수를 위해 이름 기반 파라미터 바인딩을 권장한다.
> 
> 위치 기반 파라미터 바인딩을 사용하려면 `@Param` 애노테이션을 사용하면 된다.
> 

### 벌크성 수정 쿼리

스프링 데이터 JPA에서 벌크성 수정, 삭제 쿼리는 `@Modify` 애노테이션을 사용하면 된다.

벌크성 쿼리를 실행하고 나서 영속성 컨텍스트를 초기화하고 싶으면 `@Modifying(clearAutomatically = true)` 옵션을 설정하면 된다.

### 반환 타입

```java
List<Member> findByName(String name); //복수개
Member findByEmail(String email); //단건
```

스프링 데이터 JPA는 유연한 반환 타입을 지원하는데 결과가 한 건 이상이면 컬렉션 인터페이스, 단건이면 반환 타입을 지정한다.

만약 조회 결과가 없다면 컬렉션은 빈 컬렉션을 반환하고, 단건은 null을 반환한다. 단건 조회 시에 결과가 2건 이상 조회되면 `NonUniqueResultException` 예외가 발생한다.

단건으로 지정한 메서드를 호출하면 스프링 데이터 JPA는 내부에서 JPQL의 `Query.getSingleResult()` 메서드를 호출한다. 이 메서드를 호출했을 때 조회 결과가 없으면 `NoResultException` 예외가 발생하는데, 스프링 데이터 JPA는 단건 조회할 때 예외가 발생하면 예외를 무시하고 대신 null을 반환한다.

### 페이징과 정렬

스프링 데이터 JPA는 쿼리 메서드에 페이징과 정렬 기능을 사용할 수 있도록 특별한 파라미터를 제공한다.

- `org.springframework.data.domain.Sort` : 정렬 기능
- `org.springframework.data.domain.Pageable` : 페이징 기능(내부에 Sort 포함)

`Pageable` 을 사용하면 반환 타입으로 `List` 나 `Page` 를 사용할 수 있다.

반환 타입으로 `Page` 를 사용하면 스프링 데이터 JPA는 페이징 기능을 제공하기 위해 검색된 전체 데이터 건수를 조회하는 count 쿼리를 추가로 호출한다.

```java
//count 쿼리 사용
Page<Member> findByName(String name, Pageable pagealbe);

//count 쿼리 사용 X
List<Member> findByName(String name, Pageable pageable);
List<Member) findByName(String name, Sort sort);
```

## 힌트

JPA 쿼리 힌트를 사용하려면 `QueryHints` 애노테이션을 사용하면 된다. 이것은 SQL 힌트가 아닌 JPA 구현체에게 제공하는 힌트다.

```java
@QueryHints(
	value = { @QueryHint("org.hibernate.readOnly", value="true")},
	forCounting = true)
Page<Member> fingByName(String name, Pageable pageable);
```

`forCounting` 속성은 반환 타입으로 `Page` 인터페이스를 적용하면 추가로 호출하는 페이징을 위한 `count` 쿼리에도 쿼리 힌트를 적용할지를 설정하는 옵션이다.

## Lock

쿼리 시에 락을 걸려면 `Lock` 애노테이션을 사용하면 된다. 

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
List<Member> findByName(String name);
```

---

# 사용자 정의 리포지토리 구현

스프링 데이터 JPA로 리포지토리를 개발하면 인터페이스만 정의하고 구현체는 만들지 않는다. 

만약 리포지토리를 직접 구현하려면 공통 인터페이스까지 모두 구현해야 하는 불편함이 발생할 수 있다. 스프링 데이터 JPA에서는 이런 문제를 우회해서 필요한 메서드만 구현할 수 있는 방법을 제공한다.

**사용자 정의 인터페이스**

```java
public interface MemberRepositoryCustom {
	public List<Member> findMemberCustom();
}
```

- 사용자 정의 인터페이스를 사용하려면 구현 클래스를 작성해야 한다.
    - 구현 클래스 명명 규칙 : 리포지토리 인터페이스 이름 + Impl
        - ex) `MemberRepositoryCustomImpl`
- 사용자 정의 인터페이스를 사용하려면 JPA 리포지토리에 사용자 정의 인터페이스를 상속 받아야 한다.

---

# Web 확장

스프링 데이터 프로젝트는 스프링 MVC에서 사용할 수 있는 편리한 기능을 제공한다.

식별자로 도메인 클래스를 바로 바인딩해주는 도메인 클래스 컨버터 기능과, 페이징과 정렬 기능이 있다.

**사용 설정**

Web 확장 기능을 활성화 하려면 `SpringDataWebConfiguration` 을 스프링 빈으로 등록하거나, JavaConfig를 사용하면 `@EnableSpringDataWebSupport` 애노테이션을 사용하면 된다.

## 도메인 클래스 컨버터

도메인 클래스 컨버터는 HTTP 파라미터로 넘어온 엔티티의 아이디로 엔티티 객체를 찾아서 바인딩 해준다.

```java
@Controller
public class MemberController {
	@Autowired
	MemberRepository memberRepository;

	@RequestMapping("member/memberUpdateForm")
	public String memberUpdateForm(@RequestParam("id") Member member, Model model) {
		Member member = memberRepository.findOne(id);
		model.addAttribute("member", member);
		return "member/memberSaveForm";
	}
}
```

- `@RequestParam("id") Member member` 에서 HTTP 요청으로 id 를 받지만 도메인 클래스 컨버터가 동작해서 아이디를 회원 엔티티 객체로 변환해서 넘겨준다.
    - 도메인 클래스 컨버터는 해당 엔티티와 관련된 리포지토리를 사용해서 엔티티를 찾는다.

## 페이징과 정렬

- 페이징 기능 : `PageableHandlerMethodArgumentResolver`
- 정렬 기능 : `SortHandlerMethodArgumentResolver`

### 페이징

```java
@RequestMapping(vaule = "/members", method = RequestMethod.GET)
public String list(Pageable pageable, Model model) {
	Page<Member> page = memberService.findMembers(pageable);
	model.addAttribute("members", page.getContent());
	return "members/mebmerList";
}
```

- `Pageable` 은 다음 요청 파라미터 정보로 만들어 진다.
    - page : 현재 페이지(0 부터 시작)
    - size : 한 페이지에 노출할 데이터 건수
    - sort : 정렬 조건을 정의한다.
        - ex) DESC, ASC
- **접두사**
    - 사용해야 할 페이징 정보가 둘 이상이면 접두사를 사용해서 구분할 수 있다.
    - 스프링 프레임워크에서 제공하는 `@Qualifier` 애노테이션을 사용한다.
    
    ```java
    public String list (
    	@Qualifier("member") Pageable memberPageable,
    	@Qualifier("order") Pageable orderPageable, ...){...}
    ```
    
- **기본값**
    - `Pageable` 의 기본값은 page=0, size=20
    - 변경하고 싶다면, `@PageableDefault` 애노테이션을 사용해 옵션을 조정한다.

---
