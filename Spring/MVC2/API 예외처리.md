# API 예외 처리

>[김영한-스프링 MVC 2편](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-mvc-2#)


HTML 의 경우 4xx, 5xx 같은 오류페이지만 있으면 오류 해결이 가능하다.

그러나 API 의 경우는 문제가 단순하지 않다. API 는 각 오류 상황에 맞는 오류 응답 스펙을 정하고, JSON으로 데이터를 내려주어야 한다.

```java
@Slf4j
@RestController
public class ApiExceptionController {
 @GetMapping("/api/members/{id}")
 public MemberDto getMember(@PathVariable("id") String id) {
 if (id.equals("ex")) {
 throw new RuntimeException("잘못된 사용자");
 }
 return new MemberDto(id, "hello " + id);
 }
 @Data
 @AllArgsConstructor
 static class MemberDto {
 private String memberId;
 private String name;
 }
}
```

해당 URL 에 id 값으로 ex 가 들어오면 예외가 발생하도록 컨트롤러를 생성

그러나 해당 URL 로 오류를 발생시키면 미리 만들어둔 예외 페이지가 호출된다. 이건 우리가 원하는 바가 아니다.

클라이언트는 정상, 오류 요청 모두에서 JSON 이 반환되는 것을 원한다. 웹 브라우저가 아닌 이상 HTML 을 직접 받아서 할 수 있는 것은 별로 없다.

```java
@RequestMapping(value = "/error-page/500", produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<Map<String, Object>> errorPage500Api(HttpServletRequest request, HttpServletResponse response) {
 log.info("API errorPage 500");

 Map<String, Object> result = new HashMap<>();
 Exception ex = (Exception) request.getAttribute(ERROR_EXCEPTION);
 result.put("status", request.getAttribute(ERROR_STATUS_CODE));
 result.put("message", ex.getMessage());

 Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
 return new ResponseEntity(result, HttpStatus.valueOf(statusCode));
}
```

- `produces = MediaType.APPLICATION_JSON_VALUE`
    - 클라이언트가 요청하는 HTTP Header 의 Acept 값이 `Application/json` 일 때 해당 메서드가 호출된다.
- 응답 데이터를 위해 Map 을 만들고, `ResponseEntity` 를 사용해 응답한다.
    - 메시지 컨버터가 동작하며 클라이언트에 JSON 이 반환된다.

---

## 스프링 부트 기본 오류 처리

스프링 부트가 제공하는 `BasicErrorController`  중 `/error` 동일한 경로를 처리하는 두 메서드를 확인할 수 있다.

```java
@RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {}
@RequestMapping
public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {}
```

- `errorHtml()` : `produces = MediaType.TEXT_HTML_VALUE`
    - 즉 Acept 값이 `text/html` 인 경우 호출되며 view 를 제공한다.
- `error()` : 그 외의 경우에 호출되며, `ResponseEntity` 로 HTTP Body 에 JSON 을 반환한다.
- 결과

```java
{
 "timestamp": "2021-04-28T00:00:00.000+00:00",
 "status": 500,
 "error": "Internal Server Error",
 "exception": "java.lang.RuntimeException",
 "trace": "java.lang.RuntimeException: 잘못된 사용자\n\tat
hello.exception.web.api.ApiExceptionController.getMember(ApiExceptionController
.java:19...,
 "message": "잘못된 사용자",
 "path": "/api/members/ex"
}
```

- 이전에 했던것 처럼 오류 설정에 따라 해당되는 정보들을 활용해 오류 API 를 생성해준다.

### HTML 페이지 VS API 오류

`BasicErrorController` 를 확장하면 JSON 메시지도 변경 가능하지만 이후 학습할 `@ExceptionHandler` 가 제공하는 기능을 사용하는 편이 더 나은 방법이다.

스프링 부트가 제공하는 `BasicErrorController` 는 HTML 페이지를 제공하는 경우 매우 편리하지만 API 오류 처리의 경우는 다른 이야기다.

API 마다 각각의 컨트롤러나 예외마다 서로 다른 응답 결과를 출력해야할 수 있다. 이 경우에는 `@ExceptionHandler` 를 사용하자.

---

## HandlerExceptionResolver

스프링 MVC는 컨트롤러(핸들러) 밖으로 예외가 던져진 경우 예외를 해결하고, 동작을 새로 정의할 수 있는 방법을 제공한다.

 

![Untitled](https://user-images.githubusercontent.com/75190035/151326854-9cb58356-df35-4e95-8e3f-96a637701589.png)

ExceptionResolver 적용 전

![Untitled 1](https://user-images.githubusercontent.com/75190035/151326871-a38700ab-6967-48e6-92f1-85a5c67779c5.png)

ExceptionResolver 적용 후

### 구현

```java
@Slf4j
public class MyHandlerExceptionResolver implements HandlerExceptionResolver {
 @Override
 public ModelAndView resolveException(HttpServletRequest request,
												HttpServletResponse response, Object handler, Exception ex) {
	 try {
		 if (ex instanceof IllegalArgumentException) {
			 log.info("IllegalArgumentException resolver to 400");
			 response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
			 return new ModelAndView();
		 }
	 } catch (IOException e) {
			 log.error("resolver ex", e);
	 }
   return null;
  }
}
```

- `HandlerExceptionResolver` 는 인터페이스 이고, 내부에 `resolveException` 이라는 request, response, handler, ex를 파라미터로 받는 메서드를 가지고 있다.
- `ExceptionResolver` 가 `ModelAndView` 를 반환하는 이유는 마치 try, catch를 하듯이, Exception 을
처리해서 정상 흐름 처럼 변경하는 것이 목적이다. 이름 그대로 Exception 을 Resolver(해결)하는 것이 목적이다.
- 여기서는 `IllegalArgumentException` 이 발생하면, `response.sendError(400)` 를 호출해서 상태코드를 400으로 지정하고, 빈 `ModelAndView` 를 반환한다.
- 반환 값에 따른 동작 방식
    - `빈 ModelAndView` : `new ModelAndView()` 처럼 빈 `ModelAndView` 를 반환하면 뷰를 렌더링 하지 않고, 정상 흐름으로 서블릿이 리턴된다.
    - `ModelAndView 지정` : `ModelAndView` 에 view, model 등의 정보를 지정해 반환하면 뷰를 렌더링 한다.
    - `null` : `null` 을 반환하면, 다음 `ExceptionResolver` 를 찾아서 실행한다. 만약 처리할 수 있는 `ExceptionResolver` 가 없으면 예외처리가 되지 않고, 서블릿 밖으로 예외를 던진다.

---

### ExceptionResolver 활용1

- 예외 상태 코드 반환
    - 예외를 `response.sendError(xxx)` 호출로 변경해서 서블릿에서 상태 코드에 따른 오류를 처리하도록 위임
    - 이후 WAS 는 서블릿 오류 페이지를 찾아서 내부 호출, 예를들어 스프링부트의 기본 경로인 `/error`
- 뷰 템플릿 처리
    - `ModelAndView` 에 값을 채워서 예외에 따른 새로운 오류 화면을 뷰 렌더링 해서 고객에게 제공
- API 응답 처리
    - `response.getWriter().println(”hello”);` 처럼 HTTP 응답 바디에 직접 데이터를 넣어주는 것도 가능. 여기에 JSON 으로 응답하면 API 응답 처리를 할 수 있다.

### 등록

```java
@Override
public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
 resolvers.add(new MyHandlerExceptionResolver());
}
```

- `configureHandlerExceptionResolvers()` 를 사용하면 스프링이 기본 등록하는 `ExceptionResolver` 가 제거되므로 주의해 사용.
    
    → 여기서는 `extendHandlerExceptionResolvers` 사용
    

---

### ExceptionResolver 활용2

예외를 여기서 마무리하기

예외가 발생하면 WAS 까지 예외가 던져지고, WAS 에서 오류 페이지 정보를 찾아서 다시 `/error` 를 호출하는 과정은 복잡해 보인다.

`ExceptionResolver` 를 활용하면 이런 과정없이 문제를 해결할 수 있다.

```java
@Slf4j
public class UserHandlerExceptionResolver implements HandlerExceptionResolver {
 private final ObjectMapper objectMapper = new ObjectMapper();
 @Override
 public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
																			Object handler, Exception ex) {
  try {
	  if (ex instanceof UserException) {
		  log.info("UserException resolver to 400");
		  String acceptHeader = request.getHeader("accept");
		  response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

		  if ("application/json".equals(acceptHeader)) {
			  Map<String, Object> errorResult = new HashMap<>();
			  errorResult.put("ex", ex.getClass());
				errorResult.put("message", ex.getMessage());
			  String result = objectMapper.writeValueAsString(errorResult);

			  response.setContentType("application/json");
				response.setCharacterEncoding("utf-8");
			  response.getWriter().write(result);
			  return new ModelAndView();
		  } else {
				//TEXT/HTML
				return new ModelAndView("error/500");
		  }
		}
  } catch (IOException e) {
	  log.error("resolver ex", e);
  }
  return null;
  }
}
```

HTTP 요청 헤더의 ACCEPT 값이 application/json 이면 JSON 으로 오류를 내려주고, 그 외의 경우 `return new ModelAndView("error/500");` 에 따라 `error/500` 에 있는 HTML 오류 페이지를 보여준다.

### 정리

`ExceptionResolver` 를 사용하면 **컨트롤러에서 예외가 발생해도 `ExceptionResolver` 에서 예외를
처리한다.**
따라서 `예외가 발생해도 서블릿 컨테이너까지 예외가 전달되지 않고, 스프링 MVC에서 예외 처리는 끝이
난다.` 결과적으로 **WAS 입장에서는 정상 처리가 된 것**이다. 이렇게 예외를 이곳에서 모두 처리할 수 있다는 것이
핵심이다.
서블릿 컨테이너까지 예외가 올라가면 복잡하고 지저분하게 추가 프로세스가 실행된다. 반면 `ExceptionResolver` 를 사용하면 예외처리가 상당히 깔끔해진다.

---

## 스프링이 제공하는 ExceptionResovler - ResponseStatusExceptionResolver

스프링 부트가 기본으로 제공하는 `ExceptionResolver`

`HandlerExceptionResolverComposite` 에 다음 순서로 등록

1. `ExceptionHandlerExceptionResolver`
    - `@ExceptionHandler`를 처리한다. API 예외처리는 대부분 이 기능으로 해결
2. `ResponseStatusExceptionResolver`
    - HTTP 상태 코드를 지정해 준다.
        - 예) `@ResponseStatus(value = HttpStatus.NOT_FOUND)`
3. `DefaultHandlerExceptionResolver`
    - 스프링 내부 기본 예외 처리

### ResponseStatusExceptionResolver

다음 두가지 경우를 처리

1. `@ResponseStatus` 가 달려있는 예외
2. `ResponseStatusException` 예외

```java
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "잘못된 요청 오류")
public class BadRequestException extends RuntimeException {
}
```

다음 같은 경우에는 `BadRequestException` 이 컨트롤러 밖으로 넘어가면, `ResponseStatusExceptionResolver` 예외가 해당 애노테이션을 확인하고, 오류코드를 변경, 메시지도 담는다.

`ResponseStatusExceptionResolver` 코드를 확인해보면 결국 `response.sendError` 를 호출한다. 이때문에 WAS 에서 다시 오류 페이지를 내부 요청한다.

또한 `reason=”error.bad”` 와 같은 형태로 사용하면 메시지 기능을 사용할 수 있다.

**ResponseStattusException**

@ResponseStatus 는 개발자가 직접 변경할 수 없는 예외에는 적용할 수 없다. (애노테이션을 직접 넣어야 하는데, 내가 코드를 수정할 수 없는 라이브러리의 예외 코드 같은 곳에는 적용할 수 없다.)

추가로 애노테이션을 사용하기 때문에 조건에 따라 동적으로 변경하기도 어렵다. 이때는 `ResponseStatusException` 예외를 사용한다.

```java
@GetMapping("/api/response-status-ex2")
public String responseStatusEx2() {
 throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error.bad", new IllegalArgumentException());
}
```

---

## 스프링이 제공하는 ExceptionResovler - DefaultHandlerExceptionResolver

`DefaultHandlerExceptionResolver` 는 스프링 내부에서 발생하는 스프링 예외를 해결한다.

대표적으로 TypeMismatchException 을 그냥 방치하면 서블릿 컨테이너까지 오류가 올라가고, 결과적으로 500 오류가 발생한다.

`DefaultHandlerExceptionResolver` 는 이런 클라이언트가 잘못한 경우 500오류가 아니라 400 오류로 변경한다.

- 코드 확인
    - `DefaultHandlerExceptionResolver.handleTypeMismatch` 를 보면 다음과 같은 코드가 존재한다. → `response.sendError(HttpServletResponse.SC_BAD_REQUEST)`

### 정리

1. `ExceptionHandlerExceptionResolver`
2. `ResponseStatusExceptionResolver` → HTTP 응답 코드 변경
3. `DefaultHandlerExceptionResolver` → 스프링 내부 예외 처리

그러나 `HandlerExceptionResolver` 를 직접 사용하기는 번거롭다. API 응답은 `response` 에 직접 데이터를 넣어야해서 매우 불편하고 번거롭다. `ModelAndView` 를 반환해야하는 것도 API 에는 잘 맞지 않는다.

스프링은 이런 문제를 `@ExceptionHandler` 라는 예외 처리 기능을 통해 해결한다.

---

## @ExceptionHandler

전에 언급했듯이 웹 브라우저에 HTML 화면을 제공할 때는 오류가 발생하면 `BasicErrorCotroller` 를 사용하는게 편리하지만, API 는 각 시스템 마다 응답의 형태가 달라 단순 화면출력과 달리 세밀한 제어가 필요하다.

### API 예외처리의 어려운 점

- `HandlerExceptionResolver` 를 떠올려 보면 `ModelAndView`를 반환해야 했다. 이것은 API 응답에는 필요하지 않다.
- API 응답을 위해서 `HttpServletResponse` 에 직접 응답 데이터를 넣어주었지만 이것은 매우 불편하다.
- 특정 컨트롤러에서만 발생하는 예외에대해 별도 처리가 어렵다. (같은 예외에 대해서 컨트롤러마다 서로다른 방식으로 처리하고 싶다면?)

### @ExceptionHandler

스프링은 API 예외 처리 문제를 해결하기위해 `@ExceptionHanler` 라는 애노테이션을 사용하는 편리한 예외처리 기능을 제공한다. 이것이 `ExceptionHandlerExceptionResolver` 다.

스프링은 `ExceptionHandlerExceptionResolver` 를 기본으로 제공하고, 기본으로 제공하는 `ExceptionResolver` 중에 우선순위가 가장 높다.

```java
@Slf4j
@RestController
public class ApiExceptionV2Controller {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(IllegalArgumentException.class)
  public ErrorResult illegalExHandle(IllegalArgumentException e) {
	  log.error("[exceptionHandle] ex", e);
	  return new ErrorResult("BAD", e.getMessage());
  }

  @ExceptionHandler
  public ResponseEntity<ErrorResult> userExHandle(UserException e) {
	  log.error("[exceptionHandle] ex", e);
	  ErrorResult errorResult = new ErrorResult("USER-EX", e.getMessage());
	  return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
  }
 
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler
  public ErrorResult exHandle(Exception e) {
	  log.error("[exceptionHandle] ex", e);
	  return new ErrorResult("EX", "내부 오류");
  }
 
	@GetMapping("/api2/members/{id}")
  public MemberDto getMember(@PathVariable("id") String id) {
	  if (id.equals("ex")) {
		  throw new RuntimeException("잘못된 사용자");
	  }
	  if (id.equals("bad")) {
		  throw new IllegalArgumentException("잘못된 입력 값");
	  }
	  if (id.equals("user-ex")) {
		  throw new UserException("사용자 오류");
	  }
	  return new MemberDto(id, "hello " + id);
  }
  @Data
  @AllArgsConstructor
  static class MemberDto {
	  private String memberId;
	  private String name;
  }
}
```

**예외 처리 방법**

1. 해당 컨트롤러에서 처리할 예외 지정
    - 이 경우에 해당 예외의 자식 클래스까지 처리할 수 있지만 우선순위에 따라 처리된다.

```java
@ExceptionHandler(부모예외.class)
public String 부모예외처리()(부모예외 e) {}
@ExceptionHandler(자식예외.class)
public String 자식예외처리()(자식예외 e) {}
```

자식 예외가 발생하면 부모, 자식 예외 모두 호출 대상이지만, 스프링의 우선순위는 항상 구체적인 것이 우선이다. 따라서 자식 예외 처리가 호출된다.

1. 여러개의 예외
    - 처리할 예외를 여러개 지정할 수 있다.

```java
@ExceptionHandler({AException.class, BException.class})
public String ex(Exception e) {
 log.info("exception e", e);
}
```

1. 예외 생략
    - 생략하면 메서드 파라미터의 예외가 지정된다.

```java
@ExceptionHandler
public ResponseEntity<ErrorResult> userExHandle(UserException e) {}
```

---

### 처리 과정

**IllegalArgumentException 처리**

```java
@ResponseStatus(HttpStatus.BAD_REQUEST)
@ExceptionHandler(IllegalArgumentException.class)
public ErrorResult illegalExHandle(IllegalArgumentException e) {
	log.error("[exceptionHandle] ex", e);
  return new ErrorResult("BAD", e.getMessage());
}
```

실행 흐름

- 컨트롤러를 호출한 결과 `IllegalArgumentException` 예외가 컨트롤러 밖으로 던져진다.
- 예외가 발생했으로 `ExceptionResolver` 가 작동한다.
    - 가장 우선순위가 높은`ExceptionHandlerExceptionResolver` 가 실행된다.
- `ExceptionHandlerExceptionResolver` 는 해당 컨트롤러에 `IllegalArgumentException` 을 처리할 수 있는 `@ExceptionHandler` 가 있는지 확인한다.
- `illegalExHandle()` 를 실행한다. `@RestController` 이므로 `illegalExHandle()` 에도 `@ResponseBody` 가 적용된다. 따라서 HTTP 컨버터가 사용되고, 응답이 JSON으로 반환된다.
- @ResponseStatus(HttpStatus.BAD_REQUEST) 를 지정했으므로 HTTP 상태 코드 400으로 응답한다.

응답(JSON)

```java
{
 "code": "BAD",
 "message": "잘못된 입력 값"
}
```

### UserException 처리

```java
@ExceptionHandler
public ResponseEntity<ErrorResult> userExHandle(UserException e) {
  log.error("[exceptionHandle] ex", e);
  ErrorResult errorResult = new ErrorResult("USER-EX", e.getMessage());
  return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
}
```

- `@ExceptionHandler` 에 예외를 지정하지 않으면 해당 메서드 파라미터 예외를 사용한다.
    - 여기서는`UserException` 사용
- `ResponseEntity` 를 사용해서 HTTP 메시지 바디에 직접 응답한다. 물론 HTTP 컨버터가 사용된다.
- `ResponseEntity` 를 사용하면 HTTP 응답 코드를 프로그래밍해서 동적으로 변경할 수 있다.
    - `@ResponseStatus` 는 애노테이션이므로 HTTP 응답 코드를 동적으로 변경할 수 없다.

### Exception 처리

```java
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
@ExceptionHandler
public ErrorResult exHandle(Exception e) {
  log.error("[exceptionHandle] ex", e);
  return new ErrorResult("EX", "내부 오류");
}
```

- `throw new RuntimeException("잘못된 사용자")` 이 코드가 실행되면서, 컨트롤러 밖으로`RuntimeException` 이 던져진다.
- `RuntimeException` 은 `Exception` 의 자식 클래스이다. 따라서 이 메서드가 호출된다.
- `@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)` 로 HTTP 상태 코드를 500으로 응답한다.

### HTML 오류 화면

```java
@ExceptionHandler(ViewException.class)
public ModelAndView ex(ViewException e) {
  log.info("exception e", e);
  return new ModelAndView("error");
}
```

다음과 같이 `ModelAndView` 를 사용해 오류화면 응답에도 사용할 수 있다.

---

## @ControllerAdvice

지금은 청상 코드와 예외 코드가 섞여있다.

`@ControllerAdvice` 또는 `@RestControllerAdvice` 를 사용하면 둘을 분리할 수 있다.

### 분리된 예외처리 컨트롤러

```java
@Slf4j
@RestControllerAdvice
public class ExControllerAdvice {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(IllegalArgumentException.class)
  public ErrorResult illegalExHandle(IllegalArgumentException e) {
	  log.error("[exceptionHandle] ex", e);
	  return new ErrorResult("BAD", e.getMessage());
  }
  @ExceptionHandler
  public ResponseEntity<ErrorResult> userExHandle(UserException e) {
	  log.error("[exceptionHandle] ex", e);
	  ErrorResult errorResult = new ErrorResult("USER-EX", e.getMessage());
	  return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
  }
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler
  public ErrorResult exHandle(Exception e) {
	  log.error("[exceptionHandle] ex", e);
	  return new ErrorResult("EX", "내부 오류");
  }
}
```

### 정상 로직 컨트롤러

```java
@Slf4j
@RestController
public class ApiExceptionV2Controller {
  @GetMapping("/api2/members/{id}")
  public MemberDto getMember(@PathVariable("id") String id) {
	  if (id.equals("ex")) {
		  throw new RuntimeException("잘못된 사용자");
	  }
	  if (id.equals("bad")) {
		  throw new IllegalArgumentException("잘못된 입력 값");
	  }
	  if (id.equals("user-ex")) {
		  throw new UserException("사용자 오류");
	  }
	  return new MemberDto(id, "hello " + id);
  }
  @Data
  @AllArgsConstructor
  static class MemberDto {
	  private String memberId;
	  private String name;
  }
}
```

`@ControllerAdvice`

- 대상으로 지정한 여러 컨트롤러에 `@ExceptionHandler, @InitBinder` 기능을 부여한다.
- `@ControllerAdvice` 에 대상을 지정하지 않으면 모든 컨트롤러에 적용된다. (글로벌 적용)
- `@RestControllerAdvice` 는 `@ResponseBody` 가 추가된 것이다.

**대상 컨트롤러 지정 방법**

```java
//@RestController가 붙은 클래스들을 대상으로 한다.
@ControllerAdvice(annotations = RestController.class)
public class ExampleAdvice1 {}
//해당 패키지 하위에 있는 컨트롤러 모두 대상으로 한다.
@ControllerAdvice("org.example.controllers")
public class ExampleAdvice2 {}
//특정 클래스를 대상으로한다.
@ControllerAdvice(assignableTypes = {ControllerInterface.class,
AbstractController.class})
public class ExampleAdvice3 {}
```

> **파라미터와 응답**
> 
> 
> @ExceptionHandler 에는 마치 스프링의 컨트롤러의 파라미터 응답처럼 다양한 파라미터와 응답을 지정할 수 있다.
> 
> 스프링 메뉴얼 : 
> [https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-annexceptionhandler-args](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-annexceptionhandler-args)
>
