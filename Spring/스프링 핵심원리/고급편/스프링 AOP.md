# 스프링 AOP

>[김영한-스프링 핵심원리 - 고급편](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B3%A0%EA%B8%89%ED%8E%B8#)

- [스프링 AOP - 개념](https://github.com/HyoJongPark/TIL/new/main/Spring/%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%95%B5%EC%8B%AC%EC%9B%90%EB%A6%AC/%EA%B3%A0%EA%B8%89%ED%8E%B8#%EC%8A%A4%ED%94%84%EB%A7%81-aop---%EA%B0%9C%EB%85%90)
- [스프링 AOP - 구현](https://github.com/HyoJongPark/TIL/edit/main/Spring/%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%95%B5%EC%8B%AC%EC%9B%90%EB%A6%AC/%EA%B3%A0%EA%B8%89%ED%8E%B8/%EC%8A%A4%ED%94%84%EB%A7%81%20AOP.md#%EC%8A%A4%ED%94%84%EB%A7%81-aop---%EA%B5%AC%ED%98%84)

# 스프링 AOP - 개념

애플리케이션 로직은 크게 핵심 기능과 부가 기능으로 나눌 수 있다.

- 핵심 기능은 해당 객체가 제공하는 고유의 기능이다.
- 부가 기능은 핵심 기능을 보조하기 위해 제공되는 기능이다.

### 부가기능 적용 문제

- 부가 기능을 적용할 때 많은 반복이 필요하다.
- 부가 기능이 여러 곳에 퍼져서 중복 코드를 만들어낸다.
- 부가 기능을 변경할 때 중복으로 인해 많은 수정이 필요하다.
- 부가 기능의 적용 대상을 변경할 때 많은 수정이 필요하다.

결론적으로 부가 기능을 적용할 때 발생한 반복적인 코드들 때문에 수정시에도 동일한 문제가 발생한다.

소프트웨어 개발에서 변경 지점은 하나가 될 수 있도록 잘 모듈화 되어야 한다. 그런데 부가 기능처럼 특정 로직을 애플리케이션 전반에 적용하는 문제는 일반적인 OOP 방식으로는 해결이 어렵다.

---

## 애스팩트(Aspect)

**핵심 기능과 부가 기능을 분리**

부가 기능 적용 문제를 해결하기 위해서 부가 기능을 핵심 기능에서 분리하고 한 곳에서 관리하도록 했다. 그리고 해당 부가 기능을 어디에 적용할지 선택하는 기능도 만들었다.

이렇게 부가 기능과 부가 기능을 어디에 적용할지 선택하는 기능을 합해서 하나의 모듈로 만들었는데 이것이 애스팩트다. 

`@Aspect` 가 그것이고, 스프링이 제공하는 어드바이저도 어드바이스(부가 기능)과 포인트컷(적용 대상)을 가지고 있어서 개념상 하나의 애스펙트다.

애스팩트는 관점이라는 뜻으로, 애플리케이션을 바라보는 관점을 하나하나의 기능에서 횡단 관심사관점으로 달리 보는 것이다. 이렇게 **애스펙트를 사용한 프로그래밍 방식을 관점 지향 프로그래밍 AOP(Aspect-Oriented-Programming)이라 한다.**

AOP는 OOP를 대체하기 위한 것이 아니라 횡단 관심사를 깔끔하게 처리하기 어려운 OOP의 부족한 부분을 보조하는 목적으로 개발되었다.

### Aspect 프레임워크

AOP의 대표적인 구현으로 AspectJ 프레임워크가 있다. 스프링도 AOP를 지원하지만 대부분 AspectJ 문법을 차용하고, AspectJ가 제공하는 기능의 일부만 제공한다.

AspectJ 프레임워크는 스스로를 다음과 같이 설명한다.

- 자바 프로그래밍 언어에 대한 완벽한 관점 지향 확장
- 횡단 관심사의 깔끔한 모듈화
    - 오류 검사 및 처리
    - 동기화
    - 성능 최적화(캐싱)
    - 모니터링 및 로깅

---

## AOP 적용 방식

AOP 적용 방식에는 크게 3가지 방법이 있다.

- 컴파일 시점
- 클래스 로딩 시점
- 런타임 시점(프록시)

### 1. **컴파일 시점**

<img width="604" alt="Untitled" src="https://user-images.githubusercontent.com/75190035/161380260-e9431e43-04c6-4d40-bc17-a5d53dfa14c9.png">

`.java` 소스 코드를 컴파일러를 사용해서 `.class` 를 만드는 시점에 부가 기능 로직을 추가할 수 있다. 이때는 AspectJ가 제공하는 특별한 컴파일러를 사용해야 한다. 컴파일된 `.class` 파일을 디컴파일 해보면 애스펙트 관련 호출 코드가 들어간다. 

AspectJ 컴파일러는 Aspect를 확인해서 해당 클래스가 적용 대상인지 먼저 확인하고, 적용 대상인 경우에 부가 기능 로직을 적용한다. 이렇게 원본 로직에 부가 기능 로직이 추가되는 것을 위빙이라 한다.

**단점**

컴파일 시점에 부가 기능을 적용하려면 특별한 컴파일러도 필요하고 복잡하다.

 

### 2. 클래스 로딩 시점

<img width="608" alt="Untitled 1" src="https://user-images.githubusercontent.com/75190035/161380269-7eb7e99e-59ec-4c7d-951e-b738449121b2.png">

자바를 실행하면 자바 언어는 `.class` 파일을 JVM 내부의 클래스 로더에 보관한다.

중간에서 `.class` 파일을 조작한 다음 JVM에 올릴 수 있다. 자바 언어는 `.class` 를 JVM에 저장하기 전에 조작할 수 있는 기능을 제공한다.

이 시점에 애스펙트를 적용하는 것을 로드 타임 위빙이라 한다.

**단점**

로드 타임 위빙은 자바를 실행할 때 특별한 옵션(`java-javaagent` )을 통해 클래스 로더 조작기를 지정해야 하는데, 이 부분이 번거롭고 운영하기 어렵다.

### 3. 런타임 시점

<img width="607" alt="Untitled 2" src="https://user-images.githubusercontent.com/75190035/161380275-d27b84e8-9154-4b3e-ab7b-423a970af810.png">

런타임 시점은 컴파일도 다 끝나고, 클래스 로더에 클래스도 다 올라가서 이미 자바가 실행되고 난 다음이다.

자바의 메인(`main` )메서드가 이미 실행된 다음이다. 따라서 자바 언어가 제공하는 범위 안에서 부가 기능을 적용해야 한다.

스프링과 같은 컨테이너의 도움을 받고 프록시와 DI, 빈 포스트 프로세서 같은 개념들을 총 동원해야 한다. 이렇게 함으로써 프록시를 통해 스프링 빈에 부가 기능을 적용할 수 있다.

**단점**

프록시를 사용하기 때문에 AOP 기능에 일부 제약이 있다.

### AOP 적용 위치

- 적용 가능 지점(조인 포인트) : 생성자, 필드 값 접근, static 메서드 접근, 메서드 실행
- AspectJ를 사용해서 컴파일 시점과 클래스 로딩 시점에 적용하는 AOP는 바이트코드를 실제 조작하기 때문에 해당 기능을 모든 지점에 다 적용할 수 있다.
- 프록시 방식을 사용하는 스프링 AOP는 메서드 실행 지점에만 AOP를 적용할 수 있다.
    - 프록시는 메서드 오버라이딩 개념으로 동작한다. 따라서 생성자, static 메서드, 필드 값 접근에는 프록시 개념이 적용될 수 없다.
    - 프록시를 사용하는 **스프링 AOP의 조인 포인트는 메서드 실행**으로 제한된다.
- 프록시 방식을 사용하는 스프링 AOP는 스프링 컨테이너가 관리할 수 있는 스프링 빈에만 AOP를 적용할 수 있다.

> 스프링은 AspectJ 문법을 차용하고, 프록시 방식의 AOP를 적용한다. AspectJ를 직접 사용하는 것이 아니다.
> 

> **중요**
> 
> 
> 스프링이 제공하는 AOP는 프록시를 사용한다. 따라서 적용 시점이 제한되고, AspectJ를 사용했을 때 보다 기능이 제한된다.
> 
> 하지만 AspectJ를 사용하기 위해 필요한 기본 지식이 많고, 자바 관련 설정도 복잡하다. 반면에 스프링 AOP는 별도의 자바 설정 없이 스프링만 있으면 편리하게 AOP를 사용할 수 있다.
> 

---

## AOP 용어

- 조인 포인트(Join point)
    - 어드바이스가 적용될 수 있는 위치, 메소드 실행, 생성자 호출, 필드 값 접근, static 메서드 접근 같은 프로그램 실행 중 지점
    - 조인 포인트는 추상적인 개념, AOP를 적용할 수 있는 모든 지점이라 생각하면 된다.
- 포인트컷(Pointcut)
    - 어드바이스가 적용될 위치를 선별하는 기능
    - 주로 AspectJ 표현식을 사용해서 지정
    - 프록시를 사용하는 스프링 AOP는 메서드 실행 지점만 포인트컷으로 선별 가능
- 타겟(Target)
    - 어드바이스를 받는 객체
- 어드바이스(Advice)
    - 부가 기능
    - 특정 조인 포인트에서 Aspect에 의해 취해지는 조치
    - Around(주변), Before(전), After(후)와 같은 다양한 종류의 어드바이스가 있다.
- 애스팩트(Aspect)
    - 어드바이스 + 포인트컷을 모듈화 한 것(`@Aspect` 라 생각하면 된다.)
    - 여러 어드바이스와 포인트컷이 함께 존재
- 어드바이저(Advisor)
    - 하나의 어드바이스와 하나의 포인트컷으로 구성
    - 스프링 AOP에서만 사용되는 특별한 용어
- 위빙(Weaving)
    - 포인트컷으로 결정한 타겟의 조인 포인트에 어드바이스를 적용하는 것
    - 위빙을 통해 핵심 기능 코드에 영향을 주지 않고 부가 기능을 추가할 수 있다.
    - AOP 적용을 위해 애스펙트를 객체에 연결한 상태
        - 컴파일 타임(AspectJ compiler)
        - 로드 타임
        - 런타임(스프링 AOP)
- AOP 프록시
    - AOP 기능을 구현하기 위해 만든 프록시 객체, 스프링에서 AOP는 JDK 동적 프록시 또는 CGLIB 프록시이다.

---

# 스프링 AOP - 구현

스프링 AOP를 구현하는 일반적인 방법은 `@Aspect` 를 사용하는 방법이다.

```java
public class Pointcuts {
 //hello.springaop.app 패키지와 하위 패키지
 @Pointcut("execution(* hello.aop.order..*(..))")
 public void allOrder(){}
 //타입 패턴이 *Service
 @Pointcut("execution(* *..*Service.*(..))")
 public void allService(){}
 //allOrder && allService
 @Pointcut("allOrder() && allService()")
 public void orderAndService(){}
}
```

- `@Pointcut` :
    - `@Pointcut` 에 포인트컷 표현식을 사용한다.
    - 메서드 이름과 파라미터를 합쳐서 포인터컷 시그니처라 한다.
    - 메서드의 반환 타입은 `void`
    - 코드의 내용은 비워둔다.
    - `@Around` 어드바이스에서는 포인트컷을 직접 지정하거나, 포인트컷 시그니처를 사용해 포인트컷 기능을 사용할 수 있다.
    - `allOrder() && allService()` 처럼 포인트컷을 조합 할 수도 있다.
        - `&&` , `||` , `!` 3가지 조합이 가능하다.

```java
@Slf4j
@Aspect
public class AspectV6Advice {

 @Around("hello.aop.order.aop.Pointcuts.orderAndService()")
 public Object doTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
 try {
	 //@Before
	 log.info("[around][트랜잭션 시작] {}", joinPoint.getSignature());
	 Object result = joinPoint.proceed();
	 //@AfterReturning
	 log.info("[around][트랜잭션 커밋] {}", joinPoint.getSignature());
	 return result;
 } catch (Exception e) {
	 //@AfterThrowing
	 log.info("[around][트랜잭션 롤백] {}", joinPoint.getSignature());
	 throw e;
 } finally {
	 //@After
	 log.info("[around][리소스 릴리즈] {}", joinPoint.getSignature());
	 }
 }

 @Before("hello.aop.order.aop.Pointcuts.orderAndService()")
 public void doBefore(JoinPoint joinPoint) {
	 log.info("[before] {}", joinPoint.getSignature());
 }

 @AfterReturning(value = "hello.aop.order.aop.Pointcuts.orderAndService()", returning = "result")
 public void doReturn(JoinPoint joinPoint, Object result) {
	 log.info("[return] {} return={}", joinPoint.getSignature(), result);
 }

 @AfterThrowing(value = "hello.aop.order.aop.Pointcuts.orderAndService()", throwing = "ex")
 public void doThrowing(JoinPoint joinPoint, Exception ex) {
	 log.info("[ex] {} message={}", joinPoint.getSignature(), ex.getMessage());
 }

 @After(value = "hello.aop.order.aop.Pointcuts.orderAndService()")
 public void doAfter(JoinPoint joinPoint) {
	 log.info("[after] {}", joinPoint.getSignature());
 }
}
```

- `@Aspect` :
    - `@Aspect`는 애스팩트라는 표식이지 컴포넌트 스캔 대상이 되는 것은 아니다. 별도로 스프링 빈으로 등록해야한다.
- `@Around` :
    - 애노테이션의 값이 포인트 컷이 된다.
        - `execution(* hello.aop.order..*(..))` : `hello.aop.order` 패키지와 그 하위 패키지
        - `hello.aop.order.aop.Pointcuts.orderAndService()` : 포인트컷 시그니처를 사용
    - `@Around` 애노테이션이 붙은 `doLog` 메서드가 어드바이스가 된다.
- `@Order(n)` :
    - 어드바이스는 기본적으로 순서를 보장하지 않는다. 보장하고 싶다면 `@Aspect` 적용 단위로 `@Order` 애노테이션을 적용해야 한다.
    - 클래스 단위로 적용 가능하기 때문에 하나의 애스펙트에 여러 어드바이스가 있으면 애스펙트를 별도의 클래스로 분리해야한다.
    - 숫자가 작을 수록 먼저 실행된다.

> 스프링 AOP는 AspectJ 문법을 차용하고, 프록시 방식의 AOP를 제공한다. 스프링 AOP를 사용할 때 `@Aspect` 애노테이션도 AspectJ가 제공하는 애노테이션이다.
> 

**JoinPoint 인터페이스의 주요 기능**

- `getArgs()` : 메서드 인수를 반환
- `getThis()` : 프록시 객체를 반환
- `getTarget()` : 대상 객체를 반환
- `getSignature()` : 어드바이스 메서드에 대한 설명을 반환
- `toString()` :  어드바이스 방법에 대한 유용한 설명 인쇄

**ProceedingJoinPoint 인터페이스의 주요 기능**

- `proceed()` : 다음 어드바이스나 타겟 호출

### 어드바이스 종류

- `@Around` : 메서드 호출 전후에 수행, 가장 강력한 어드바이스, 조인 포인트 실행 여부 선택, 반환값 변환, 예외 변환 등이 가능
    - 어드바이스의 첫 번째 바라미터는 `ProceedingJoinPoint` 를 사용해야 한다.
    - `proceed()` 를 통해 대상을 실행하고, 여러번 실행할 수도 있다.
- `@Before` : 조인 포인트 실행 이전에 실행
    - `@Around` 와 달리 작업 흐름을 변경할 수 없다.
    - `ProceedingJoinPoint.proceed()` 를 호출하지 않아도 자동으로 다음 타겟이 호출된다.
        - 물론 예외 발생 시에는 호출되지 않는다.
- `@AfterReturning` : 조인 포인트가 정상 완료 후 실행
    - `returning` 속성에 사용된 이름은 어드바이스 메서드의 매개변수 이름과 일치해야 한다.
    - `returning` 절에 지정된 타입의 값을 반환하는 메서드만 대상으로 실행한다.(부모 자식관계도 사용 가능)
    - `@Around` 와 달리 반환되는 객체를 변경할 수 없다.
- `@AfterThrowing` : 메서드가 예외를 던지는 경우 실행
    - `throwing` 속성에 사용된 이름은 어드바이스 메서드의 매개변수 이름과 일치해야 한다.
    - `throwing` 절에 지정된 타입과 맞은 예외를 대상으로 실행한다. (부모 자식 관계도 사용 가능)
- `@After` : 조인 포인트가 정상 또는 예외에 관계없이 실행(finally)
    - 정상 및 예외 반환 조건을 모두 처리한다.
    - 일반적으로 리소스를 해제하는데 사용한다.

실행 **순서**

<img width="607" alt="Untitled" src="https://user-images.githubusercontent.com/75190035/161422666-17a1c818-56dc-4cc5-957b-b322cad107b8.png">

> 좋은 설계는 제약이 있는 것이다.
> 
> 
> `@Around` 만 있으면 될 것 같지만, 제약은 실수를 미연에 방지한다.
> 
> `@Around` 가 가장 넓은 기능을 제공하지만, `@Before, @After` 같은 어드바이스는 기능은 적지만 실수할 가능성이 낮고, 코드도 단순하다.
> 
> 큰 장점은 코드 작성 의도가 명확하게 들어난다는 점이다. `@Before` 애노테이션을 보는 순간 이 코드는 타겟 실행 전에 한정해서 어떤 일을 하는 코드라는 것을 인지 할 수 있게 될 것이다.
>
