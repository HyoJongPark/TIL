# 스프링 AOP

>[김영한-스프링 핵심원리 - 고급편](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B3%A0%EA%B8%89%ED%8E%B8#)

- [스프링 AOP - 개념](https://github.com/HyoJongPark/TIL/blob/main/Spring/%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%95%B5%EC%8B%AC%EC%9B%90%EB%A6%AC/%EA%B3%A0%EA%B8%89%ED%8E%B8/%EC%8A%A4%ED%94%84%EB%A7%81%20AOP.md#%EC%8A%A4%ED%94%84%EB%A7%81-aop---%EA%B0%9C%EB%85%90)
- [스프링 AOP - 구현](https://github.com/HyoJongPark/TIL/blob/main/Spring/%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%95%B5%EC%8B%AC%EC%9B%90%EB%A6%AC/%EA%B3%A0%EA%B8%89%ED%8E%B8/%EC%8A%A4%ED%94%84%EB%A7%81%20AOP.md#%EC%8A%A4%ED%94%84%EB%A7%81-aop---%EA%B5%AC%ED%98%84)
- [스프링 AOP - 포인트컷](https://github.com/HyoJongPark/TIL/blob/main/Spring/%EC%8A%A4%ED%94%84%EB%A7%81%20%ED%95%B5%EC%8B%AC%EC%9B%90%EB%A6%AC/%EA%B3%A0%EA%B8%89%ED%8E%B8/%EC%8A%A4%ED%94%84%EB%A7%81%20AOP.md#%EC%8A%A4%ED%94%84%EB%A7%81aop---%ED%8F%AC%EC%9D%B8%ED%8A%B8%EC%BB%B7)

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

---

# 스프링AOP - 포인트컷

## 포인트컷 지시자

AspectJ는 포인트컷을 편라하게 표현하기 위한 특별한 표현식을 제공한다. 이때 표현식은 `execution` 같은 포인트컷 지시자로 시작한다.

- `execution` : 메소드 실행 조인 포인트를 매칭한다.
- `within` : 특정 타입 내의 조인 포인트를 매칭한다.
- `args` : 인자가 주어진 타입의 인스턴스인 조인 포인트
- `this` : 스프링 빈 객체를 대상으로 하는 조인 포인트
- `target` : Target 객체를 대상으로 하는 조인 포인트
- `@target` : 실행 객체의 클래스에 주어진 타입의 애노테이션이 있는 조인 포인트
- `@within` : 주어진 애노테이션이 있는 타입 내 조인 포인트
- `@annotation` : 메서드가 주어진 애노테이션을 가지고 있는 조인 포인트를 매칭
- `@args` : 전달된 실제 인수의 런타임 타입이 주어진 타입의 애노테이션을 갖는 조인 포인트
- `bean` : 스프링 전용 포인트컷 지시자. 빈의 이름으로 포인트컷을 매칭한다.

### execution 사용법

**execution 기본 문법**

```java
execution(접근제어자 반환타입 선언타입.메서드이름(파라미터) 예외)
```

**예제**

```java
@Test
void exactMatch() {
	pointcut.setExpression(
		"execution(public String hello.aop.member.MemberServiceImpl.hello(String))");
 assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}

@Test
void allMatch() {
 pointcut.setExpression("execution(* *(..))");
 assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}

@Test
void packageMatchSubPackage1() {
 pointcut.setExpression("execution(* hello.aop.member..*.*(..))");
 assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}
```

- `execution` 문법은 기본적으로 `접근제어자 반환타입 선언타입.메서드이름(파라미터) 예외` 의 형태로 이루어 진다.
    - 접근 제어자는 생략 가능하다.
- `*` 은 아무 값이나 들어와도 된다는 뜻이다. 파라미터에서는 `*` 과 `..` 을 사용한다.
    - `*` 은 메서드 이름 앞 뒤에도 사용 가능하다.
        - `*ello` , `hell*`
    - 파라미터에서 `*` , `..` 의 차이
        - `(*)` : 정확히 1개의 파라미터만 허용, 단 모든 타입에 대해서 허용한다.
        - `(..)` : 개수와 타입에 무관하게 모든 파라미터 허용
        - `(String, *)` : String 타입으로 시작하는 2개의 파라미터를 가지는 메서드 매칭
        - `(String, ..)` : String 타입으로 시작하는 모든 메서드 매칭
- 패키지에서 `.` 과 `..` 의 차이
    - `.` : 정확하게 해당 위치의 패키지
    - `..` : 해당 위치의 패키지와 그 하위 패키지도 포함
- `execution` 에서는 부모 타입으로 선언타입을 지정해도 자식 타입을 매칭할 수 있다.
    - 하지만, 부모 타입을 표현식에 선언한 경우 부모 타입에서 선언한 메서드가 자식 타입에 있어야 매칭에 선언한다.

---

### within 사용법

`within` 은 특정 타입 내의 조인 포인트에 대한 매칭을 제한한다. 해당 타입이 매칭되면 그 안의 메서드들이 자동으로 매칭된다. 문법은 단순한데 `execution` 에서 타입 부분만 사용한다고 보면 된다.

**예제**

```java
@Test
void withinExact() {
	pointcut.setExpression("within(hello.aop.member.MemberServiceImpl)");
	assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}
```

- `within` 은 대부분의 규칙은 `execution` 과 같다.
- `within` 의 경우 표현식에 부모 타입을 지정하면 안된다. 정확하게 타입이 맞아야 한다. 이 부분이 `execution` 과의 차이다.

---

### args 사용법

`args` 은 인자가 주어진 타입의 인스턴스인 조인 포인트로 매칭한다.

**예제**

```java
private AspectJExpressionPointcut pointcut(String expression) {
 AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
 pointcut.setExpression(expression);
 return pointcut;
}
@Test
void argsVsExecution() {
 //Args
 assertThat(pointcut("args(String)")
	 .matches(helloMethod, MemberServiceImpl.class)).isTrue();
 assertThat(pointcut("args(java.io.Serializable)")
	 .matches(helloMethod, MemberServiceImpl.class)).isTrue();
 assertThat(pointcut("args(Object)")
	 .matches(helloMethod, MemberServiceImpl.class)).isTrue();

 //Execution
 assertThat(pointcut("execution(* *(String))")
	 .matches(helloMethod, MemberServiceImpl.class)).isTrue();
 assertThat(pointcut("execution(* *(java.io.Serializable))")
	 .matches(helloMethod, MemberServiceImpl.class)).isFalse(); //매칭 실패
 assertThat(pointcut("execution(* *(Object))") 
	 .matches(helloMethod, MemberServiceImpl.class)).isFalse(); //매칭 실패
 }
```

- `execution` 과 `args` 의 차이점
    - `execution` 은 파라미터 타입이 정확하게 매칭되어야 한다. `execution` 은 클래스에 선언된 정보를 기반으로 판단한다.
    - `args` 는 부모 타입을 허용한다. `args` 는 실제 넘어온 파라미터 객체 인스턴스를 보고 판단한다.

> `args` 포인트컷 지시자는 단독으로 사용되기 보다는 파라미터 바인딩에서 주로 사용된다.
> 

---

### @target, @within

`@target` ,`@within` 은 타입에 있는 애노테이션으로 AOP 적용 여부를 판단한다.

- `@target` 은 인스턴스의 모든 메서드를 조인 포인트로 적용한다.
    - `@target(hello.aop.member.annotation.ClassAop)`
- `@within` 은 해당 타입 내에 있는 메서드만 조인 포인트로 적용한다.
    - `@within(hello.aop.member.annotation.ClassAop)`

간단히, `@target` 은 부모 클래스의 메서드까지 어드바이스를 적용하고, `@within` 은 자신의 클래스에 정의된 메서드에만 어드바이스를 적용한다.

<img width="608" alt="Untitled" src="https://user-images.githubusercontent.com/75190035/161720086-0e68ef4d-644f-4a8d-8b6b-e5e62c7468bd.png">

**예제**

```java
@Slf4j
@Aspect
static class AtTargetAtWithinAspect {
 //@target: 인스턴스 기준으로 모든 메서드의 조인 포인트를 선정, 부모 타입의 메서드도 적용
 @Around("execution(* hello.aop..*(..)) && @target(hello.aop.member.annotation.ClassAop)")
 public Object atTarget(ProceedingJoinPoint joinPoint) throws Throwable {
	 log.info("[@target] {}", joinPoint.getSignature());
	 return joinPoint.proceed();
 }

 //@within: 선택된 클래스 내부에 있는 메서드만 조인 포인트로 선정, 부모 타입의 메서드는 적용되지 않음
 @Around("execution(* hello.aop..*(..)) && @within(hello.aop.member.annotation.ClassAop)")
 public Object atWithin(ProceedingJoinPoint joinPoint) throws Throwable {
	 log.info("[@within] {}", joinPoint.getSignature());
	 return joinPoint.proceed();
 }
}
```

> `@target` , `@within` 포인트컷 지시자는 파라미터 바인딩에서 함께 사용된다.
> 

> **주의**
> 
> 
> `args, @args, @target` 포인트컷 지시자는 단독으로 사용해서는 안된다.
> 
> 위 예제에서도 `execution` 과 함께 사용해 적용 대상을 줄여줬다. 이 포인트컷 지시자들은 실제 객체 인스턴스가 생성되고 실행될 때 어드바이스 적용 여부를 확인할 수 있다.
> 
> 실행 지점에 일어나는 포인트컷 적용 여부도 결국 프록시가 있어야 실행 시점에 판단할 수 있다. 그런데 스프링 컨테이너가 프록시를 생성하는 시점은 스프링 컨테이너가 만들어지는 애플리케이션 로딩 시점이다. 
> 
> 따라서, 위 포인트컷 지시자가 있으면 스프링은 모든 빈에 AOP를 적용하려고 시도한다.
> 
> 문제는 모든 스프링 빈에 AOP 프록시를 지정하려고 하면, `final` 키워드로 지정된 빈들도 있기 때문에 오류가 발생할 수도 있다.
> 
> 따라서 이러한 표현식들은 최대한 프록시 적용 대상을 축소하는 표현식과 함께 사용해야 한다.
> 

---

### @annotation, @args

`@annotation` ,`@args` 은 애노테이션과 관련된 포인트컷 지시자들이다.

- `@annotation` : 해당 애노테이션을 갖는 조인 포인트를 매칭
- `@args` : 전달된 실제 인수의 런타임 타입이 주어진 타입의 애노테이션을 갖는 조인 포인트
    - `@args(test.Check)` : 전달된 인수의 런타임 타입에 `@Check` 애노테이션이 있는 경우 매칭

### bean

`bean` 스프링 전용 포인트컷 지시자로, 빈 이름으로 매칭한다.

---

## 매개변수 전달

특정 포인트컷 표현식을 사용해서 어드바이스에 매개변수를 전달할 수 있다.

- this, target, args, @target, @within, @annotation, @args

```java
@Before("allMember() && args(arg, ..)")
public void logArg(String arg) {
	log.info("[logArg] arg=", arg);
}
```

- 포인트컷 이름과 매개변수의 이름을 맞추어야 한다.
- 추가로 타입이 메서드에 지정한 타입으로 제한된다. 여기서는 메서드의 타입이 `String` 으로 되어있기 때문에 다음과 같다.
    - `args(arg, ..)`  → `args(String, ..)`

**예제**

```java
@Around("allMember()")
public Object logArgs1(ProceedingJoinPoint joinPoint) throws Throwable {
 Object arg1 = joinPoint.getArgs()[0];
 log.info("[logArgs1]{}, arg={}", joinPoint.getSignature(), arg1);
 return joinPoint.proceed();
}
@Around("allMember() && args(arg,..)")
public Object logArgs2(ProceedingJoinPoint joinPoint, Object arg) throws Throwable {
 log.info("[logArgs2]{}, arg={}", joinPoint.getSignature(), arg);
 return joinPoint.proceed();
}
@Before("allMember() && this(obj)")
public void thisArgs(JoinPoint joinPoint, MemberService obj) {
 log.info("[this]{}, obj={}", joinPoint.getSignature(), obj.getClass());
}
@Before("allMember() && target(obj)")
public void targetArgs(JoinPoint joinPoint, MemberService obj) {
 log.info("[target]{}, obj={}", joinPoint.getSignature(), obj.getClass());
}
@Before("allMember() && @target(annotation)")
public void atTarget(JoinPoint joinPoint, ClassAop annotation) {
 log.info("[@target]{}, obj={}", joinPoint.getSignature(), annotation);
}
@Before("allMember() && @within(annotation)")
public void atWithin(JoinPoint joinPoint, ClassAop annotation) {
 log.info("[@within]{}, obj={}", joinPoint.getSignature(), annotation);
}
@Before("allMember() && @annotation(annotation)")
public void atAnnotation(JoinPoint joinPoint, MethodAop annotation) {
 log.info("[@annotation]{}, annotationValue={}", joinPoint.getSignature(), annotation.value());
}
```

- `logArgs1` : `joinPoint.getArgs()[0]` 와 같이 매개변수를 전달 받는다.
- `logArgs2` : `args(arg,..)` 와 같이 매개변수를 전달 받는다.
- `this` : 프록시 객체를 전달 받는다.
- `target` : 실제 대상 객체를 전달 받는다.
- `@target, @within` : 타입의 애노테이션을 전달 받는다.
- `@annotation` : 메서드의 애노테이션을 전달 받는다.

---

### this, target

- `this` : 스프링 빈 객체를 대상(스프링 AOP 프록시)으로 하는 조인 포인트
- `target` : Target 객체(스프링 AOP 프록시가 가르키는 실제 대상)를 대상으로 하는 조인 포인트
- `this, target` 은 적용 타입 하나를 정확하게 지정해야 한다.
    - `*` 타입 사용 불가
    - 부모 타입을 허용한다.

```
this(hello.aop.member.MemberService)
target(hello.aop.member.MemberService)
```

**this와 target의 차이**

스프링에서 AOP를 적용하면 실제 target 객체 대신 프록시 객체가 스프링 빈으로 등록된다.

- `this` 는 스프링 빈으로 등록되어 있는 프록시 객체를 대상으로 포인트컷을 매칭
- `target` 은 실제 target 객체를 대상으로 포인트컷을 매칭

**프록시 생성 방식에 따른 차이**

1. JDK 동적 프록시

<img width="606" alt="Untitled 1" src="https://user-images.githubusercontent.com/75190035/161720170-0f0eb723-cc4e-43fa-9015-0534ab6e9637.png">

- MemberService 인터페이스 지정
    - `this(hello.aop.member.MemberService)`
        - proxy 객체를 보고 판단한다. `this` 는 부모 타입을 허용하기 때문에 AOP가 적용된다.
    - `target(hello.aop.member.MemberService)`
        - target 객체를 보고 판단한다. `target` 은 부모 타입을 허용하기 때문에 AOP가 적용된다.
- MemberService 구체 클래스 지정
    - `this(hello.aop.member.MemberServiceImpl)`
        - proxy 객체를 보고 판단한다. JDK 동적 프록시로 만들어진 proxy 객체는 `MemberSerivce` 인터페이스를 기반으로 구현된 새로운 클래스다. 따라서 `MemberServiceImpl` 을 전혀 알지 못하므로 AOP 적용 대상이 아니다.
    - `target(hello.aop.member.MemberServiceImpl)`
        - target 객체를 보고 판단한다. target 객체가 `MemberServiceImpl` 타입이므로 AOP 적용 대상이다.
2. CGLIB 프록시

<img width="608" alt="Untitled 2" src="https://user-images.githubusercontent.com/75190035/161720189-8ab3d7a5-a490-44c9-9418-5fb7c3d0ca88.png">

- MemberService 인터페이스 지정
    - `this(hello.aop.member.MemberService)`
        - proxy 객체를 보고 판단한다. this 는 부모 타입을 허용하기 때문에 AOP가 적용된다.
    - `target(hello.aop.member.MemberService)`
        - target 객체를 보고 판단한다. target 은 부모 타입을 허용하기 때문에 AOP가 적용된다.
- MemberServiceImpl 구체 클래스 지정
    - `this(hello.aop.member.MemberServiceImpl)`
        - proxy 객체를 보고 판단한다. CGLIB로 만들어진 proxy 객체는 MemberServiceImpl 를 상속 받아서 만들었기 때문에 AOP 적용가 적용된다. `this`가 부모 타입을 허용하기 때문에 포인트컷의 대상이 된다.
    - `target(hello.aop.member.MemberServiceImpl)`
        - target 객체를 보고 판단한다. target 객체가 MemberServiceImpl 타입이므로 AOP 적용 대상이다.

**정리**

프록시를 대상으로 하는 `this` 의 경우 구체 클래스를 지정하면, 프록시 생성 전략에 따라 다른 결과가 나올 수 있다.

> `this, target` 포인트컷 지시자는 단독으로 사용되기 보다는 파라미터 파인딩에서 주로 사용된다.
>
