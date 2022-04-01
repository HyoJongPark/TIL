# @Aspect AOP

>[김영한-스프링 핵심원리 - 고급편](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B3%A0%EA%B8%89%ED%8E%B8#)

# @Aspect 프록시

스프링 애플리케이션에 프록시를 적용하기 위해서는 어드바이저를 만들어서 스프링 빈으로 등록해야한다.

스프링은 `@Aspect` 애노테이션으로 매우 편리하게 포인트컷과 어드바이스로 구성되어있는 어드바이저 생성 기능을 지원한다.

> `@Aspect` 는 관점 지향 프로그래밍(AOP)을 가능하게 하는 AspectJ 프로젝트에서 제공하는 애노테이션이다. 스프링은 이것을 차용해 프록시를 통한 AOP를 가능하게 한다.
> 

### @Aspect 적용

```java
@Slf4j
@Aspect
public class LogTraceAspect {
 private final LogTrace logTrace;

 public LogTraceAspect(LogTrace logTrace) {
	 this.logTrace = logTrace;
 }

 @Around("execution(* hello.proxy.app..*(..))")
 public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
	 TraceStatus status = null;
	 try {
		 String message = joinPoint.getSignature().toShortString();
		 status = logTrace.begin(message);
		 //로직 호출
		 Object result = joinPoint.proceed();
		 logTrace.end(status);
		 return result;
	 } catch (Exception e) {
		 logTrace.exception(status, e);
		 throw e;
	 }
 }
}
```

- `@Aspect` : 애노테이션 기반 프록시를 적용할 때 필요하다.
- `@Around("execution(* hello.proxy.app..*(..))")`
    - `@Around` 에 표현식을 넣는다. 표현식은 AspectJ 표현식을 사용한다.
    - `@Around` 가 붙은 메서드는 어드바이스가 된다.
- `ProceedingJoinPoint joinPoint` : 어드바이스에서 살펴본 `MethodInvocation invocation` 과 유사한 기능이다. 내부에 실제 호출 대상, 전달 인자, 어떤 객체와 어떤 메서드가 호출되었는지의 정보가 포함되어있다.
- `joinPoint.proceed()` : 실제 호출 대상(target)을 호출한다.

### @Aspect - 정리

**자동 프록시 생성기능 2가지 일을 한다.**

1. `@Aspect` 를 보고 어드바이저로 변환해서 저장

<img width="608" alt="Untitled" src="https://user-images.githubusercontent.com/75190035/161196958-b58c2050-2650-44da-94e6-bde7dc689035.png">

**@Aspect를 어드바이저로 변환해서 저장하는 과정**

1. 실행 : 스프링 애플리케이션 로딩 시점에 자동 프록시 생성기를 호출한다.
2.  모든 @Aspect 빈 조회 : 자동 프록시 생성기능 스프링 컨테이너에서 `@Aspect` 애노테이션이 붙은 스프링 빈을 모두 조회한다.
3. 어드바이저 생성 : `@Aspect` 어드바이저 빌더를 통해 `@Aspect` 애노테이션 정보를 기반으로 어드바이저를 생성한다.
4. @Aspect 기반 어드바이저 저장 : 생성한 어드바이저를 `@Aspect` 어드바이저 빌더 내부에 저장한다.

> @Aspect 어드바이저 빌더
> 
> 
> `BeanFactoryAspectJAdvisorsBuilder` 클래스다. `@Aspect` 정보를 기반으로 포인트컷, 어드바이스, 어드바이저를 생성하고 보관하는 것을 담당한다.
> 
> `@Aspect` 정보를 기반으로 어드바이저를 만들고, @Aspect 어드바이저 빌더 내부 저장소에 캐시한다. 캐시가 이미 만들어져 있다면, 캐시에 저장된 어드바이저를 반환한다.
> 

1. 어드바이저를 기반으로 프록시를 생성

<img width="604" alt="Untitled 1" src="https://user-images.githubusercontent.com/75190035/161196972-95ba12f1-deb4-48d1-aa39-800c4d739558.png">

**자동 프록시 생성기의 동작 과정**

1. 생성 : 스프링 빈 대상이 되는 객체를 생성한다.
2. 전달 : 생성된 객체를 빈 저장소에 등록하기 직전에 빈 후처리기에 전달한다.

 3-1. Advisor 빈 조회 : 스프링 컨테이너에서 `Advisor` 빈을 모두 조회한다.

 3-2. @Aspect Advisor 조회 : `@Aspect` 어드바이저 빌더 내부에 저장된 어드바이저를 모두 조회한다.

1. 프록시 적용 대상 체크 : 어드바이저에 포한된 포인트컷을 사용해서 해당 객체가 프록시를 적용할 대상인지 판단한다.
    - 이때, 객체의 클래스 정보는 물론이고 해당 객체의 모든 메서드를 포인트컷에 하나하나 매칭해본다.
2. 프록시 생성 : 프록시 적용 대상이면 프록시를 생성하고 반환한다. 이 반환된 프록시가 스프링 빈으로 등록된다.
3. 빈 등록 : 반환된 객체가 스프링 빈으로 등록된다.
