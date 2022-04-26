# 영속성 관리와 OSIV

# 트랜잭션 범위의 영속성 컨텍스트

순수하게 J2EE(Java SE)환경에서 JPA를 사용하면 개발자가 직접 엔티티 매니저를 생성하고 트랜잭션도 관리해야 한다. 하지만 스프링이나 J2EE 컨테이너 환경에서 JPA를 사용하면 컨테이너가 제공하는 전략을 사용해야 한다.

## 스프링 컨테이너의 기본 전략

<img width="606" alt="Untitled" src="https://user-images.githubusercontent.com/75190035/165265561-625478e3-0795-44ae-b4b3-cb64d3b7f070.png">

스프링 컨테이너는 **트랜잭션 범위의 영속성 컨텍스트 전략**을 기본으로 사용한다. 이 전략은 트랜잭션을 시작할 때 영속성 컨텍스트를 생성하고 트랜잭션이 끝날 때 영속성 컨텍스트를 종료한다. 그리고 같은 트랜잭션 안에서는 항상 같은 영속성 컨텍스트에 접근한다.

- **트랜잭션이 같으면 같은 영속성 컨텍스트를 사용한다.**
    
    다양한 위치에서 엔티티 매니저를 주입받아 사용해도 트랜잭션이 같다면 항상 같은 영속성 컨텍스트를 사용한다.
    
- **트랜잭션이 다르면 다른 영속성 컨텍스트를 사용한다.**
    
    같은 엔티티 매니저를 사용해도 트랜잭션에 따라 접근하는 영속성 컨텍스트가 다르다.
    
    - 스프링 컨테이너는 스레드마다 각각 다른 트랙잰션을 할당한다. 따라서 같은 엔티티 매니저를 호출해도 접근하는 영속성 컨테이너가 다르므로 멀티 쓰레드 상황에 안전하다.

---

# 준영속 상태와 지연 로딩

트랜잭션은 보통 서비스 계층에서 시작하므로 서비스 계층이 끝나는 시점에 영속성 컨텍스트와 함께 종료된다. 따라서 조회한 엔티티가 서비스와 리포지토리 계층에서는 영속성 컨테스트에 관리되며 영속 상태를 유지하지만 컨트롤러나 뷰 같은 프레젠테이션 계층에서는 준영속 상태가 된다.

```java
class OrderController {
	public String view(Long id) {
		Order order = orderService.findOne(orderId);
		Member member = order.getMember();
		member.getName(); //지연 로딩 시 예외 발생
		...
	}
}
		
```

- 준영속 상태와 변경 감지
    - 변경 감지 기능은 영속성 컨텍스트가 살아있는 서비스 계층 까지만 동작한다.
- 준영속 상태에서 지연 로딩
    - 준영속 상태에서는 지연 로딩이 동작하지 않는다. 이 문제는 뷰를 렌더링 할 때 연관된 엔티티를 함께 사용해야 하는데 이 때 `LazyInitializationException` 예외를 발생시킨다.
    - 해결책
        - 뷰가 필요한 엔티티를 미리 로딩
            - 글로벌 페치 전략 수정
            - JPQL 페치 조인
            - 강제 초기화
        - OSIV를 사용해서 엔티티를 항상 영속 상태로 유지

## 글로벌 페치 전략 수정

```java
@ManyToOne(fetch = FetchType.EAGER)//페치 전략 수정
private Team team;
```

- 엔티티의 `fetch` 타입을 변경해서 글로벌 페치 전략을 수정할 수 있다. 해당 전략을 `EAGER` 로 수정하면 엔티티를 조회할 때 연관된 엔티티도 모두 로딩해서 가지기 때문에 준영속 상태에서도 문제를 일으키지 않는다.
- 문제점
    - 사용하지 않는 엔티티를 로딩한다.
        - `EAGER` 는 엔티티 조회 시에 모든 연관된 엔티티를 조회하므로 사용하지 않을 엔티티도 조회하게 된다.
    - N+1 문제가 발생한다.

> **N+1 문제**
> 
> 
> `em.createQuery("select o from Order o", Order.class).getResultList`과 같이 JPQL로 조회하면 실행된 SQL은 다음과 같다.
> 
> ```sql
> select * from Order
> select * from Member where id=?
> select * from Member where id=?
> select * from Member where id=?
> select * from Member where id=?
> ```
> 
> JPA가 JPQL을 분석해서 SQL을 생성할 때는 글로벌 페치 전략을 참고하지 않고 오직 JPQL자체만 사용한다.
> 
> 1. JPQL을 분석해서 `select * from Order` SQL 을 생성한다.
> 2. 데이터베이스에서 결과를 받아 order 엔티티 인스턴스를 생성한다.
> 3. order를 로딩하는 즉시 연관된 member도 로딩한다.
> 4. 먼저 영속성 컨텍스트에서 찾고, 없다면  `select * from Member where id=?` SQL을 조회한 order 엔티티 수만큼 실행한다.
> 
> 4번 문제 처럼 처음 조회한 데이터 수만큼 다시 SQL을 사용해서 조회하는 것을 N+1 문제라 한다. N+1이 발생하면 SQL이 상당히 많이 호출되므로 조회 성능에 치명적이다.
> 

## JPQL 페치 조인

글로벌 페치 전략을 즉시 로딩으로 설정하면 애플리케이션 전체에 영향을 주므로 비효율적이다.

```sql
//페치 조인 사용 전
JPQL: select o from Order o
SQL : select * from Order

//페치 조인 사용 후
JPQL: select o from Order o join fetch o.member
SQL : select o.*, m.* from Order o
			join Member m on o.MEMBER_ID=m.MEMBER_ID
```

페치 조인은 조인 명령에 뒤에 `fetch` 를 넣어 사용한다. 실행된 SQL을 보면 페치 조인을 사용하면 SQL JOIN을 사용해서 페치 조인 대상까지 함께 조회한다. 따라서 N+1 문제가 발생하지 않는다.

- 단점
  - 무분별하게 사용하면 화면에 맞춘 리포지토리 메서드가 증가할 수 있다. 결국 프리젠테이션 계층이 데이터 접근 계층을 침범하는 것이다.

## 강제로 초기화

강제로 초기화하는 방식은 영속성 컨텍스트가 살아있을 때 프리젠테이션 계층이 필요한 엔티티를 강제로 초기화 해서 반환하는 방법이다.

```java
@Transcation
public Order findOrder(id) {
	Order order = orderRepository.findOrder(id);
	order.getMember().getName();//강제 초기화
	return order;
}
```

글로벌 페치 전략을 `LAZY` 로 한 경우 연관된 엔티티를 프록시 객체로 조회한다. 프록시 객체는 실제 사용하는 시점에 초기화 됨으로 영속성 컨텍스트가 살아있을 때 강제로 초기화해서 반환하면, 준영속 상태에서 사용할 수 있다.

하이버네이트를 사용하면 `initialize()` 메서드를 사용해서 강제로 초기화할 수 있다.

- 초기화 여부 확인

JPA 표준에는 초기화 메서드가 없지만 초기화 여부를 확인할 수는 있다.

```java
PersistenceUnitUtil persistenceUnitUtil = 
	em.getEntityManagerFactory().getPersistenceUnitUtil();
boolean isLoaded = persistenceUnitUtil.isLoaded(order.getMember());
```

## FACADE 계층 추가

만약 서비스 계층에서 강제 초기화 같은 작업을 수행한다면 이것은 프리젠테이션 계층이 서비스 계층을 침범하는 상황이다. 따라서 두 계층 사이에 별도의 계층을 마련해서 서비스 계층에서 초기화 작업을 분리해야 한다. `FACADE` 계층이 위와 같은 역할을 수행한다.

```java
class OrderFacade {
	@Autowired
	OrderService orderService;
	
	public Order findOrder(Long id) {
		Order order = orderService.findOrder(id);
		order.getMember().getName();//강제 초기화
		return order;
	}
}

class OrderSerivce {
	public Order findOrder(Long id) {
		return orderRepository.findOrder(id);
	}
```

**FACADE 계층의 역할과 특징**

- 프리젠테이션 계층과 도메인 모델 계층 간의 논리적 의존성을 분리해준다.
- 프리젠테이션 계층에서 필요한 프록시 객체를 초기화한다.
- 서비스 계층을 호출해서 비지니스 로직을 실행한다.
- 리포지토리를 직접 호출해서 뷰가 요구하는 엔티티를 찾는다.

## 준영속 상태와 지연 로딩의 문제점

뷰를 개발할 때 엔티티를 미리 초기화하는 방법은 오류가 발생할 확률이 높다. 이유는 보통 뷰를 개발할 때는 엔티티 클래스를 보고 개발을 하지 이것이 초기화되어 있는지 아닌지 확인하기 위해 FACADE나 서비스 클래스까지 열어보는 것은 상당히 번거롭고 놓치기 쉽기 때문이다. 

또 화면별로 최적화된 엔티티를 딱 맞게 떨어지게 초기화해서 조회하려면 FACADE 계층에 여러 종류의 조회 메서드가 필요하다.

결국 모든 문제는 엔티티가 프리젠테이션 계층에서 준영속 상태이기 때문에 발생한다.

---

# OSIV

OSIV(Open Session In View)는 영속성 컨텍스트를 뷰까지 열어둔다는 뜻이다. 영속성 컨텍스트가 살아있으면 엔티티는 영속 상태로 유지된다. 따라서 뷰에서도 지연 로딩을 사용할 수 있다.

## 과거 OSIV: 요청 당 트랜잭션

OSIV의 핵심은 뷰에서도 지연 로딩이 가능하도록 하는 것이다. 가장 단순한 구현 방법은 클라이언트의 요청이 들어오자마자 서블릿 필터나 스프링 인터셉터에서 트랜잭션을 시작하고 요청이 끝날 때 트랜잭션도 끝내는 것이다.

이것을 요청 당 트랜잭션 방식의 OSIV라 한다.

**요청 당 트랜잭션 방식의 문제**

요청 당 트랜잭션 방식의 OSIV가 가지는 문제점은 컨트롤러나 뷰 같은 프리젠테이션 계층이 엔티티를 변경할 수 있다는 점이다.

서비스 계층처럼 비지니스 로직을 실행하는 곳에서 데이터를 변경하는 것은 당연하지만, 프리젠테이션 계층에서 데이터를 잠시 변경했다고 실제 데이터베이스까지 변경 내용이 반영되면 애플리케이션을 유지보수하기 상당히 힘들어진다.

이런 문제를 해결하려면 프리젠테이션 계층에서 엔티티를 수정하지 못하게 막아야 한다. 프리젠테이션 계층에서 엔티티를 수정하지 못하게 하는 방법은 다음과 같다.

- 엔티티를 읽기 전용 인터페이스를 제공

```java
@Entity
class Member implements MemberView {
	...
}

interface MemberView {
	public String getName();
}
```

프리젠테이션 계층에는 읽기 전용 메서드만 있는 `MemberView` 인터페이스를 제공한다. 따라서 프리젠테이션 계층에서는 수정이 불가능 하다.

- 엔티티 매핑

```java
class MemberWrapper {
	private Member member;
	
	public MemberWrapper(member) {
		this.member = member;
	}
	//읽기 전용 메서드만 제공
	public String getName() {
		member.getName();
	}
}
```

읽기 전용 메서드만 가지고 있는 엔티티를 감싼 객체를 만들고 이것을 프리젠테이션 계층에 반환하는 방법이다.

따라서 프리젠테이션 계층에서는 수정이 불가능 하다.

- DTO만 반환

```java
class MemberDto {
	private String name;
	
	//getter, setter
}
```

가장 전통적인 방법인데 프리젠테이션 계층에 엔티티 대신 단순히 데이터만 전달하는 객체인 DTO(Data Transfer Object)를 생성해서 반환하는 것이다.

하지만 이 방법은 OSIV를 사용하는 장점을 살릴 수 없고 엔티티를 거의 복사한 듯한 DTO 클래스도 하나 더 만들어야 한다.

위의 방법들 모두 코드량이 상당히 증가한다는 단점이 있다. 차라리 프리젠테이션 계층에서는 값을 수정하지 못하는 규칙을 만들거나, 적절한 도구를 사용해서 프리젠테이션 계층에서 엔티티를 호출하는 코드를 잡아내는 것도 하나의 방법이지만 모두 쉽지 않다.

이런 문제들 때문에 요청 당 트랜잭션 방식의 OSIV는 거의 사용하지 않는다. 최근에는 이런 문제점을 어느정도 보완해서 비지니스 계층에서만 트랜잭션을 유지하는 방식의 OSIV를 사용한다.

## 스프링 OSIV: 비지니스 계층 트랜잭션

**스프링 프레임워크가 제공하는 OSIV 라이브러리**

스프링 프레임워크의 `spring-orm.jar` 는 다양한 OSIV 클래스를 제공한다. OSIV를 서블릿 필터에서 적용할지 스프링 인터셉터에서 적용 할지에 따라 원하는 클래스를 선택해 사용하면 된다.

- 하이버네이트 OSIV 서블릿 필터 : `org.springframework.orm.hibernate4.support.OpenSessionInViewFilter`
- 하이버네이트 OSIV 스프링 인터셉터 : `org.springframework.orm.hibernate4.support.OpenSessionInViewInterceptor`
- JPA OEIV 서블릿 필터 : `org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter`
- JPA OEIV 스프링 인터셉터 : `org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor`

**스프링 OSIV 분석**

스프링 프레임워크가 제공하는 OSIV는 요청 당 트랜잭션 방식의 문제를 어느정도 해결할 수 있다.

스프링 프레임워크가 제공하는 OSIV는 비지니스 계층에서만 트랜젹션을 사용한다. 

<img width="610" alt="Untitled 1" src="https://user-images.githubusercontent.com/75190035/165265578-9b1a8db1-b2fd-4343-83de-ab38dfed9a9d.png">

동작 원리는 다음과 같다.

1. 클라이언트의 요청이 들어오면 서블릿 필터나, 스프링 인터셉터에서 영속성 컨텍스트를 생성한다. 단, 이때 트랜잭션은 시작하지 않는다.
2. 서비스 계층에서 `@Transactional` 로 트랜잭셕을 시작할 때 1번에서 미리 생성해둔 영속성 컨텍스트를 찾아와서 트랜잭션을 시작한다.
3. 서비스 계층이 끝나면 트랜잭션을 커밋하고 영속성 컨텍스트를 플러시한다. 이때 트랜잭션은 끝내지만 영속성 컨텍스트는 종료하지 않는다.
4. 컨트롤러와 뷰까지 영속성 컨텍스트가 유지되므로 조회한 엔티티는 영속 상태를 유지한다.
5. 서블릿 필터나, 스프링 인터셉터로 요청이 돌아오면 영속성 컨텍스트를 종료한다. 이때 플러시를 호출하지 않고 바로 종료한다.

**트랜잭션 없이 읽기**

영속성 컨텍스트를 통한 모든 변경은 트랜잭션 안에서 이루어져야 한다. 만약 트랜잭션 없이 엔티티를 변경하고 영속성 컨텍스트를 플러시하면 `javax.persistence.TransactionRequiredException` 예외가 발생한다.

단, 엔티티를 변경하지 않고 단순히 조회할 때는 트랜잭션이 없어도 되는데 이것을 트랜잭션 없이 읽기라 한다. 프록시를 초기화하는 지연 로딩도 조회 기능이므로 트랜잭션 없이 읽기가 가능하다.

만약 서비스 계층 밖에서 엔티티의 값을 변경하면 2가지 이유로 플러시가 동작하지 않는다.

- 트랜잭션을 사용하는 서비스 계층이 끝날때 트랜잭션이 커밋되면서 이미 영속성 컨텍스트를 플러시했다. 그리고 스프링이 제공하는 OSIV 서블릿 필터나 OSIV 스프링 인터셉터는 요청이 끝나면 플러시를 호출하지 않고 `em.close()` 로 영속성 컨텍스트만 종료한다.
- 프리젠테이션 계층에서 `em.flush()` 를 호출해서 강제로 플러시해도 트랜잭션 범위 밖이므로 `TransactionRequiredException` 예외가 발생한다.

**스프링 OSIV 주의사항**

스프링 OSIV를 사용하면 프리젠테이션 계층에서 엔티티를 수정해도 수정 내용을 데이터베이스에 반영하지 않는다. 하지만 수정 직후에 트랜잭션을 시작하는 서비스 계층을 호출하면 문제가 발생한다.

```java
class MemberController {
	public String viewMember(Long id) {
		Member member = memberService.getMember(id);
		member.setName("xxx");

		memberService.biz();//비지니스 로직
		return "view";
	}
}
```

`biz` 메소드가 끝나면 트랜잭션 AOP가 끝나면서 영속성 컨택스트도 플러시한다. 이때 변경 감지가 동작하면서 회원 엔티티의 수정 사항을 데이터베이스에 반영한다. 스프링 OSIV는 같은 영속성 컨텍스트를 여러 트랜잭션이 공유할 수 있으므로 이런 문제가 발생한다.

이런 문제를 피하기 위해서는 비지니스 로직을 호출한 뒤에 엔티티를 변경하면 된다.

**스프링 OSIV 정리**

- 특징
    - OSIV는 클라이언트 요청이 들어올 때 영속성 컨텍스트를 생성해서 요청이 끝날 때까지 같은 영속성 컨텍스트를 유지한다. 따라서 한 번 조회한 엔티티는 요청이 끝날 때까지 영속 상태를 유지한다.
    - 엔티티 수정은 트랜잭션이 있는 계층에서만 동작한다.
- 단점
    - OSIV를 적용하면 같은 영속성 컨텍스트를 여러 트랜잭션이 공유할 수 있다는 점을 주의해야 한다.
    - 프리젠테이션 계층에서 엔티티를 수정하고나서 비지니스 로직을 수행하면 엔티티가 수정될 수 있다.
    - 프리젠테이션 계층에서 지연 로딩에 의한 SQL이 실행된다. 따라서 성능 튜닝 시에 확인해야 할 부분이 넓다.
- OSIV를 사용하는 방법은 만능이 아니다.
    - OSIV를 사용하면 화면을 출력할 때 엔티티를 유지하면서 객체 그래프를 마음껏 탐색할 수 있다. 하지만 복잡한 화면을 구성할 때는 처음부터 관련 JPQL을 작성해서 DTO로 조회하는 것이 효과적일 수 있다.
- OSIV는 같은 JVM을 벗어난 원격 상황에서는 사용할 수 없다.
