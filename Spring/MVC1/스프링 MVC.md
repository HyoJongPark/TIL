# 스프링 MVC

>[김영한-스프링 MVC 1편](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-mvc-1#)

# 구조 이해

## 스프링 MVC 시작

스프링이 제공하는 컨트롤러는 애노테이션 기반으로 동작해서, 매우 유연하고 실용적이다.

- `@RequestMapping`
    - 스프링은 애노테이션을 활용한 매우 유연하고 실용적인 컨트롤러를 만들었는데 이것이 `@RequestMapping`이다.
    - `RequestMappingHandlerMapping`
    - `RequestMappingHandlerAdapter`
        - 교양에서 살펴 본것 처럼 가장 우선순위가 높은 매핑과 어댑터는 위의 두개다.

```java
@Controller
public class SpringMemberFormControllerV1 {

    @RequestMapping("/springmvc/v1/members/new-form")
    public ModelAndView process() {
        return new ModelAndView("new-form");
    }
}
```

- `@Controller`:
    - 스프링이 자동으로 스프링 빈으로 등록
        - 컨트롤러 애노테이션 내부 @Component 덕분
- `@RequestMapping`
    - 요청 정보를 매핑, 해당 URL이 호출되면 이 메서드가 호출
    - 애노테이션 기반이기 때문에 메서드의 이름은 임의로 지으면 된다.
- `ModelAndView`
    - 모델과 뷰 정보를 담아서 반환
- `RequestMappingHandlerMapping`은 스프링 빈 중에서 `@RequestMapping, @Controller`가 **“클래스 레벨”**에 붙어 있는 경우에 매핑 정보로 인식한다.

```java
@Controller
public class SpringMemberSaveControllerV1 {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @RequestMapping("/springmvc/v1/members/save")
    public ModelAndView process(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));

        Member member = new Member(username, age);
        memberRepository.save(member);

        ModelAndView mv = new ModelAndView("save-result");
        mv.addObject("member", member);
        return mv;
    }
}
```

새로운 savecontroller다.

클래스 레벨에 `@Controller`와 메서드 레벨에 `@RequestMapping`을 추가한것 이외에 차이는 거의 없다.

---

## 컨트롤러 통합

```java
@Controller
@RequestMapping("/springmvc/v2/members")
public class SpringMemberControllerV2 {

    MemberRepository memberRepository = MemberRepository.getInstance();

    @RequestMapping("/new-form")
    public ModelAndView newForm() {
        return new ModelAndView("new-form");
    }

    @RequestMapping("/save")
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));

        Member member = new Member(username, age);
        memberRepository.save(member);

        ModelAndView mv = new ModelAndView("save-result");
        mv.addObject("member", member);
        return mv;
    }

    @RequestMapping()
    public ModelAndView members() {
        List<Member> members = memberRepository.findAll();

        ModelAndView mv = new ModelAndView("members");
        mv.addObject("members", members);
        return mv;
    }
}
```

- `@RequestMapping`을 클래스레벨에 붙혀 중복되는 url부분을 제거할 수 있다.
- 하나의 컨트롤러에서 연관성 있는 컨트롤러 들을 통합 관리한다.

---

## 실용적인 방식

```java
@Controller
@RequestMapping("/springmvc/v3/members")
public class SpringMemberControllerV3 {

    MemberRepository memberRepository = MemberRepository.getInstance();

    @GetMapping("/new-form")
    public String newForm() {
        return "new-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam("username") String username,
                             @RequestParam("age") int age,
                             Model model) {
        Member member = new Member(username, age);
        memberRepository.save(member);

        model.addAttribute("member", member);
        return "save-result";
    }

    @GetMapping()
    public String members(Model model) {
        List<Member> members = memberRepository.findAll();

        model.addAttribute("members", members);
        return "members";
    }
}
```

- Model 파라미터
- View 논리이름 직접 반환
- `@RequestParam`
    - 기존에 `request.getParameter(”username”)` 과 같은 코드를 줄여준다.
    - GET 쿼리 파라미터, POST form 방식을 모두 지원한다.
- `@RequestMapping` → `@PostMapping, @GetMapping`
    - URL만 매칭하는 것이 아닌 HTTP Method 도 함께 구분하는 애노테이션을 별도로 제공한다.

---

---

## 기본 기능

## 로깅

> 참고. @RestController 는 반환값이 String 이면 컨트롤러와는 달리 HTTP 메시지 바디에 바로 입력한다. 따라서 실행결과로 화면에 원하는 메시지를 띄울 수 있다. 이번 챕터에서 배우는 내용이 아니니 간단히
> 

```java
@RestController
public class LogTestController {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @RequestMapping("/log-test")
    public String logTest() {
        String name = "spring";

        log.trace("trace log={}", name);
        log.debug("debug log={}",name);
        log.info("info log={}", name);
        log.warn("warn log={}", name);
        log.error("error log={}", name);

        return "OK";
    }
}
```

```java
#전체 로그 레벨 설정(디폴트는 info)
logging.level.root=debug

#hello.springmvc 패키지와 그 하위 로그 레벨 설정(디폴트는 info)
logging.level.hello.springmvc=trace
```

- 로그가 출력되는 포멧
    - 시간, 로그 레벨, 프로세스 ID, 쓰레드 명, 클래스명, 로그 메시지
- 로그 레벨
    - 로그 레벨은 application.properties에서 설정 가능하다
    - trace > debug > info > warn > error
        - 개발 서버는 debug, 운영 서버는 info 로 하는게 일반적
- @Slf4j

```java
@Slf4j
@RestController
public class LogTestController {

    @RequestMapping("/log-test")
    public String logTest() {
        String name = "spring";

        log.trace("trace log={}", name);
        log.debug("debug log={}",name);
        log.info("info log={}", name);
        log.warn("warn log={}", name);
        log.error("error log={}", name);

        return "OK";
    }
}
```

- 주의!
    - `log.trace("trace log=" + name);`
        - 위와 같은 코드는 “+” 연산자로 인해 불필요한 연산이 발생하고, 특히! 연산이 일어난 후에 → 설정 레벨에 따른 출력이 발생하기 때문에 설정레벨은 info로 설정해도 위의 trace레벨의 로그는 출력되지 않지만 불필요한 연산이 발생한다.
    - `log.trace(”trace log={}”, name);`
        - 다음과 같이 사용해야 불필요한 연산을 수행하지 않는다.
- 장점
    - 쓰레드 정보, 클래스 이름과 같은 부가 정보를 함께 볼 수 있고, 출력 모양을 조정할 수 있다.
    - 로그 레벨에 따라 개발 서버에서는 모든 로그를 출력하고, 운영 서버에서는 출력하지 않는 등 로그를 상황에 맞게 조절할 수 있다.
    - 시스템 아웃 콘솔에만 출력하는 것이 아니라, 파일이나 네트워크 등 로그를 별도의 위치에 남길 수 있다.
        - 파일로 남길 때는 일별, 특정 용량에 따라 로그를 분할하는 것도 가능하다.
    - 성능도 일반 system.out 보다 좋다.

<aside>
📖 로그에 대해서 더 자세한 내용은 slf4j, logback을 검색해보자.
SLF4J - [http://www.slf4j.org](http://www.slf4j.org/)
Logback - [http://logback.qos.ch](http://logback.qos.ch/)
스프링 부트가 제공하는 로그 기능은 다음을 참고하자.
[https://docs.spring.io/spring-boot/docs/current/reference/html/spring-bootfeatures.html#boot-features-logging](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-bootfeatures.html#boot-features-logging)

</aside>

---

## 요청 매핑

```java
@Slf4j
@RestController
public class MappingController {

    @RequestMapping("/hello-basic")
    public String helloBasic() {
        log.info("helloBasic");
        return "ok";
    }

    /**
     * 편리한 축약 애노테이션 (코드보기)
     * @GetMapping
     * @PostMapping
     * @PutMapping
     * @DeleteMapping
     * @PatchMapping
     */
    @GetMapping(value = "/mapping-get-v2")
    public String MappingV2() {
        log.info("mapping-get-v2");
        return "ok";
    }

    /**
     * PathVariable 사용
     * 변수명이 같으면 생략 가능
     * @PathVariable("userId") String userId -> @PathVariable userId
     */
    @GetMapping("/mapping/{userId}")
    public String mappingPath(@PathVariable String userId) {
        log.info("mappingPath userId={}", userId);
        return "OK";
    }

    /**
     * PathVariable 사용 다중
     */
    @GetMapping("/mapping/users/{userId}/orders/{orderId}")
    public String mappingPath(@PathVariable String userId, @PathVariable Long orderId) {
        log.info("mappingPath userId={}, orderId={}", userId, orderId);
        return "OK";
    }

    /**
     * 파라미터로 추가 매핑
     * params="mode",
     * params="!mode"
     * params="mode=debug"
     * params="mode!=debug" (! = )
     * params = {"mode=debug","data=good"}
     */
    @GetMapping(value = "/mapping-param", params = "mode=debug")
    public String mappingParam() {
        log.info("mappingParam");
        return "ok";
    }

    /**
     * 특정 헤더로 추가 매핑
     * headers="mode",
     * headers="!mode"
     * headers="mode=debug"
     * headers="mode!=debug" (! = )
     */
    @GetMapping(value = "/mapping-header", headers = "mode=debug")
    public String mappingHeader() {
        log.info("mappingHeader");
        return "ok";
    }

    /**
     * Content-Type 헤더 기반 추가 매핑 Media Type
     * consumes="application/json"
     * consumes="!application/json"
     * consumes="application/*"
     * consumes="*\/*"
     * MediaType.APPLICATION_JSON_VALUE
     */
    @PostMapping(value = "/mapping-consume", consumes = "application/json")
    public String mappingConsumes() {
        log.info("mappingConsumes");
        return "ok";
    }

    /**
     * Accept 헤더 기반 Media Type
     * produces = "text/html"
     * produces = "!text/html"
     * produces = "text/*"
     * produces = "*\/*"
     */
    @PostMapping(value = "/mapping-produce", produces = "text/html")
    public String mappingProduces() {
        log.info("mappingProduces");
        return "ok";
    }
}
```

- `@RestController`
    - `@Controller` 는 반환 값이 String이면 뷰 이름으로 인식한다.
    - `@RestController` 는 반환 값이 String 이면 HTTP 메시지 바디에 바로 입력한다.
        - 이것은 `@ResponseBody`와 관련이 있는데, `@RestController` 내부에는 `@Controller`와 `@ResponseBody` 가 존재해 둘 모두의 역할을 동시에 수행한다
- `@RequestMapping`
    - 해당 URL 호출이 오면 해당 애노테이션이 붙은 메서드가 실행된다.
    - 다중 설정이 가능하다. → 다른 URL을 같은 요청으로 매핑
        - @RequestMapping({”/helllo”, “/spring”})
- HTTP 메서드
    - `@RequestMapping` 에 Method 속성으로 특정 HTTP Method를 지정할 수 있다.
        - HTTP Method: GET, HEAD, POST, PUT, PATCH, DELETE
        - Method 속성을 지정하지 않고 `@GetMapping, @PostMapping` 와 같은 애노테이션 형태로 특정 속성을 지정하는 방식도 지원한다.
    - 지정된 Method 와 다른 Method 요청이 오면 → 405 상태코드를 반환
- `PathVariable`
    - HTTP API 는 리소스 경로에 식별자를 넣는 스타일을 선호한다.
        - 예) /mapping/userA
    - `@RequestMapping`은 URL 경로를 템플릿화 할 수 있는데, `@PathVariable` 을 사용하면 매칭되는 부분을 편리하게 조회할 수 있다.
        - `@PathVariable`이름과 파라미터 이름이 같으면 생략 가능
- params, headers 등의 조건을 통해서 특정 파라미터, 헤더, 미디어 타입을 조건으로 가지는 HTTP 요청만 불러들일 수도 있다.

---

# HTTP 요청

## 기본, 헤더 조회

```java
@Slf4j
@RestController
public class RequestHeaderController {

    @RequestMapping("/headers")
    public String headers(HttpServletRequest request, HttpServletResponse response,
                          HttpMethod httpMethod, Locale locale,
                          @RequestHeader MultiValueMap<String, String> headerMap,
                          @RequestHeader("host") String host,
                          @CookieValue(value = "myCookie", required = false) String cookie) {
        log.info("request={}", request);
        log.info("response={}", response);
        log.info("httpMethod={}", httpMethod);
        log.info("locale={}", locale);
        log.info("headerMap={}", headerMap);
        log.info("header host={}", host);
        log.info("myCookie={}", cookie);
        return "ok";
    }
}
```

- 위 사용예시처럼 Locale정보, 특정 혹은 모든 HTTP 헤더 등을 조회할 수 있다.
- MultiValueMap
    - Map 과 유사하지만 하나의 키에 여러 값을 받을 수 있다.
    - HTTP header, HTTP 쿼리 파라미터와 같이 하나의 키에 여러 값을 받을 때 사용한다.
        - keyA=value1&keyA=value2
        

<aside>
📖 참고
@Conroller 의 사용 가능한 파라미터 목록은 다음 공식 메뉴얼에서 확인할 수 있다.
[https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-annarguments](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-annarguments)
@Conroller 의 사용 가능한 응답 값 목록은 다음 공식 메뉴얼에서 확인할 수 있다.
[https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-annreturn-types](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-annreturn-types)

</aside>

---

# HTTP 요청

## 쿼리 파라미터, HTML Form

### @RequestParam 조회 기본

```java
@Slf4j
@Controller
public class RequestParamController {

    @RequestMapping("/request-param-v1")
    public void requestParamV1(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));
        log.info("username={}, age={}", username, age);

        response.getWriter().write("ok");
    }

    @ResponseBody
    @RequestMapping("/request-param-v2")
    public String requestParamV2(@RequestParam("username") String name, @RequestParam("age") int userAge) {
        log.info("username={}, age={}", name, userAge);
        return "ok";
    }

    @ResponseBody
    @RequestMapping("/request-param-v3")
    public String requestParamV3(@RequestParam String username, @RequestParam int age) {
        log.info("username={}, age={}", username, age);
        return "ok";
    }

    @ResponseBody
    @RequestMapping("/request-param-v4")
    public String requestParamV4(String username, int age) {
        log.info("username={}, age={}", username, age);
        return "ok";
    }
}
```

- 클래스 레벨에 @RestController를 붙이면 모든 클래스에 대해 설정하는 것이고, 메서드 단위로 하고 싶으면 메서드에 @ResponseBody로 각각 설정
1. `request.getParameter(parameterName)`
2. `@RequestParam(parameterName) Type name`
    1. 파라미터 이름과 변수 이름이 같으면 파라미터 이름을 생략 가능하다
    2. String, int, Integet 등의 단순 타입이면 @RequestParam 또한 생략가능하다
        - 다만 애노테이션을 생략하는 것은 보는 사람 입장에서 명확하지 않을 수 있다.
        

### @RequestParam 의 속성(required, defaultValue) 과 Map 조회

```java
@ResponseBody
    @RequestMapping("/request-param-required")
    public String requestParamRequired(@RequestParam(required = true) String username,
                                       @RequestParam(required = false) Integer age) {
        log.info("username={}, age={}", username, age);
        return "ok";
    }

    @ResponseBody
    @RequestMapping("/request-param-default")
    public String requestParamDefault(@RequestParam(required = true, defaultValue = "guest") String username,
                                      @RequestParam(required = false, defaultValue = "-1") Integer age) {
        log.info("username={}, age={}", username, age);
        return "ok";
    }

    @ResponseBody
    @RequestMapping("/request-param-map")
    public String requestParamMap(@RequestParam Map<String, Object> paramMap) {
        log.info("username={}, age={}", paramMap.get("username"), paramMap.get("age"));
        return "ok";
    }
```

- `@RequestParam(required=true)`
    - required 속성으로 해당 파라미터를 필수로 입력할 것인지 설정할 수 있다. (default = true)
    - 필수 파라미터가 없으면 → 400 에러 발생
    - 파라미터 이름만 넘기고 값을 넘기지 않을 경우 공백으로 입력한 것으로 인식하고 통과된다.
        - 예) /spring?username=
    - 기본형에 null 입력
        - int 타입은 null을 입력할 수 없다. 따라서 required=false로 설정할 경우 int → Integer, 혹은 defaultValue를 사용해 입력되지 않았을 때 default 값을 설정해 해결한다.
- `@RequestParam(defalutValue=”value”)`
    - 파라미터에 값이 없이 넘어온 경우 defaultValue를 설정하면 기본 값을 설정 할 수 있다.
    - 공백으로 넘어온 경우에도 적용된다.
    - 기본 값이 있기 때문에 required 속성은 의미가 없다.
- Map으로 조회
    - `@RequestParam Map`
        - Map(key=value)
    - `@RequestParam MultiValueMap`
        - MultiValueMap(key=[value1, value2])
    - 파라미터의 값이 1개가 확실하면 Map, 아니라면 MultiValueMap 을 사용

### @ModelAttribute

```java
    @ResponseBody
    @RequestMapping("/model-attribute-v1")
    public String modelAttributeV1(@ModelAttribute HelloData helloData) {
        log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());
        return "OK";
    }
    @ResponseBody
    @RequestMapping("/model-attribute-v2")
    public String modelAttributeV2(HelloData helloData) {
        log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());
        return "OK";
    }
```

- 스프링은 요청 파라미터를 받아 필요한 객체를 만들고 그 객체에 값을 넣어주는 과정을 `@ModelAttribute`를 통해 자동화 해준다.
    1. HelloData 객체 생성
    2. 요청 파라미터 이름으로 HelloData 객체의 프로퍼티를 찾고, 해당 프로퍼티의 setter를 호출해서 파라미터의 값을 입력
    - 프로퍼티
        - 객체에 `getUsername()`, `setUsername()` 메서드가 있으면 이 객체는 username이라는 프로퍼티를 가지고 있다.
- `@ModelAttribute` 생략
    - 스프링은 `@RequestParam, @ModelAttribute` 둘다 생략가능함으로써 발생하는 혼란을 막기 위해 규칙을 적용한다.
    - String, int, Integet 같은 단순 타입 → `@RequestParam`
    - 나머지 → `@ModelAttribute` (argument resolver로 지정해둔 타입 외)
    

---

# HTTP 요청 메시지

## 단순 텍스트

요청 파라미터와 다르게, HTTP 메시지 바디를 통해 데이터가 직접 넘어오는 경우는 `@RequestParam, @ModelAttribute`를 사용할 수 없다.

### InputStream

```java
@PostMapping("request-body-string-v1")
public void requestBodyStringV1(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ServletInputStream inputStream = request.getInputStream();
    String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

    log.info("messageBody={}", messageBody);

    response.getWriter().write("ok");
    }

@PostMapping("request-body-string-v2")
public void requestBodyStringV2(InputStream inputStream, Writer responseWriter) throws IOException {
    String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
    log.info("messageBody={}", messageBody);
    responseWriter.write("ok");
    }
```

- `HttpServletRequest, HttpServletResponse` 를 파라미터로 받아 그것으로 부터 `InputStream`을 받아올수도 있지만 번거롭고 스프링 MVC는 더 간단한 방법을 지원한다.
- 스프링 MVC는 다음 파라미터를 지원한다.
    - `InputStream(Reader)`: HTTP 요청 메시지 바디의 내용을 직접 조회
    - `OutputStream(Writer)`: HTTP 응답 메시지의 바디에 직접 결과 출력

### HttpEntity

```java
@PostMapping("request-body-string-v3")
public HttpEntity<String> requestBodyStringV3(HttpEntity<String> httpEntity) throws IOException {
    String messageBody = httpEntity.getBody();

    log.info("messageBody={}", messageBody);
    return new HttpEntity<>("ok");
}
```

- 스프링 MVC는 다음 파라미터를 지원한다.
    - `HttpEntity`: HTTP header, body 정보를 편리하게 조회
        - 메시지 바디 정보를 직접 조회
        - 요청 파라미터를 조회하는 기능과 관계 없음
    - `HttpEntity`는 응답에도 사용 가능
        - 메시지 바디 정보 직접 반환
        - 헤더 정보 포함 가능
        - view 조회 x
    - `HttpEntity`를 상속받은 객체들도 같은 기능을 제공
        - `RequestEntity`
            - HttpMethod, url 정보가 추가, 요청에서 사용
        - `ResponseEntity`
            - HTTP 상태 코드 설정 가능, 응답에서 사용

> 스프링 MVC 내부에서 HTTP 메시지 바디를 읽어서 문자나 객체로 변환해서 전달해 주는데 이것은 메시지 컨버터(HttpMessageConverter) 라는 기능을 사용한 것이다.
> 

### @RequestBody

```java
@ResponseBody
@PostMapping("request-body-string-v4")
public String requestBodyStringV4(@RequestBody String messageBody) throws IOException {
    log.info("messageBody={}", messageBody);
    return "ok";
}
```

- `@RequestBody`
    - 이 애노테이션을 사용하면 HTTP 메시지 바디 정보를 편리하게 조회할 수 있다.
        - 헤더정보가 필요할 때는 `HttpEntity, @RequestHeader`를 사용

정리

요청 파라미터를 조회하는 기능: `@RequestParam, @ModelAttribute`

HTTP 메시지 바디를 직접 조회하는 기능: `@RequestBody`

---

# HTTP 요청 메시지

## JSON

### ObjectMapper

```java
private ObjectMapper objectMapper = new ObjectMapper();

@PostMapping("/request-body-json-v1")
public void requestBodyJsonV1(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ServletInputStream inputStream = request.getInputStream();
    String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

    log.info("messageBody={}", messageBody);
    HelloData helloData = objectMapper.readValue(messageBody, HelloData.class);
    log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());

    response.getWriter().write("ok");
}
```

- `HttpServletRequest`를 사용해 HTTP 메시지 바디에서 데이터를 읽어와 문자로 변환한다.
- 문자로 변환된 JSON 데이터를 Jackson 라이브러리인 `objectMapper`를 사용해 자바 객체로 변환한다.

### @RequestBody

```java
@ResponseBody
@PostMapping("/request-body-json-v2")
public String requestBodyJsonV2(@RequestBody String messageBody) throws IOException {
    log.info("messageBody={}", messageBody);
    HelloData helloData = objectMapper.readValue(messageBody, HelloData.class);
    log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());

    return "ok";
}
@ResponseBody
@PostMapping("/request-body-json-v3")
public String requestBodyJsonV3(@RequestBody HelloData helloData) throws IOException {
    log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());
    return "ok";
}
```

- `@RequestBody`를 사용해 HTTP 메시지에서 데이터를 꺼내고 messageBody에 저장, objectMapper를 통해 변환한다.
    - 그러나 @RequestBody에는 직접 만든 객체를 지정해 위 과정을 하나로 통합할 수 있다.(v3)
- `@RequestBody` 객체 파라미터
    - `HttpEntity`, `@RequesetBody` 를 사용하면 HTTP 메시지 바디의 내용을 우리가 원하는 문자나 객체 등으로 변환해준다.
    - 문자 뿐 아니라 JSON 도 객체로 변환해 준다.

### HttpEntity, 리턴타입을 객체로 설정할 시 예시

```java
@ResponseBody
@PostMapping("/request-body-json-v4")
public String requestBodyJsonV4(HttpEntity<HelloData> httpEntity) {
    HelloData data = httpEntity.getBody();
    log.info("username={}, age={}", data.getUsername(), data.getAge());
    return "ok";
}

@ResponseBody
@PostMapping("/request-body-json-v5")
public HelloData requestBodyJsonV5(@RequestBody HelloData data) {
    log.info("username={}, age={}", data.getUsername(), data.getAge());
    return data;
}
```

- `@ResponseBody` 대신 `HttpEntity`를 사용해도 된다.
- 리턴 타입을 HelloData와 같은 객체로 설정하면, 해당 객체를 HTTP 메시지 바디에 직접 넣어줄 수 있다.
    - 이 경우에서 `HttpEntity` 사용 가능

- `@RequestBody` 요청
    - JSON 요청 → HTTP 메시지 컨버터 → 객체
- `@ResponseBody` 응답
    - 객체 → HTTP 메시지 컨버터 → JSON 응답

---

# HTTP 응답

## 정적 리소스, 뷰 템플릿

- 정적 리소스
    - 정적 리소스는 해당 파일을 변경 없이 그대로 서비스하는 것이다.
- 뷰 템플릿 사용
- HTTP 메시지 사용

### 뷰 템플릿 사용

```java
@RequestMapping("/response-view-v2")
public String responseViewV2(Model model) {
  model.addAttribute("data", "hello!!");
  return "response/hello";
}
@RequestMapping("/response/hello")
public void responseViewV3(Model model) {
  model.addAttribute("data", "hello!!");
}
```

- String 을 반환하는 경우
    - `@ResponseBody` 가 없으면 `response/hello` 로 뷰 리졸버가 실행되어서 뷰를 찾고, 렌더링 한다.
        - 경로 : `templates/response/hello.html`
    - `@ResponseBody` 가 있으면 뷰 리졸버를 실행하지 않고, HTTP 메시지 바디에 직접 response/hello 라는 **문자가 입력**된다.
- void를 반환하는 경우
    - `@Controller` 를 사용하고, `HttpServletResponse` , `OutputStream`(Writer) 같은 HTTP 메시지
    바디를 처리하는 파라미터가 없으면 요청 URL을 참고해서 논리 뷰 이름으로 사용
        - 요청 URL: `/response/hello`
        - 경로 : `templates/response/hello.html`
    - 이 방식은 명시성이 떨어지고 저렇게 맞아 떨이지는 경우가 드물어 사용하지 않는걸 권장함.

---

## HTTP 응답 - HTTP API, 메시지 바디에 직접 입력

HTTP API를 제공하는 경우에는 HTML이 아니라 데이터를 전달해야 하므로, HTTP 메시지 바디에 JSON같은 형식으로 데이터를 보낸다.

```java
@Slf4j
@Controller
public class ResponseBodyController {

  @GetMapping("/response-body-string-v1")
  public void responseBodyV1(HttpServletResponse response) throws IOException {
	  response.getWriter().write("ok");
  }
 /**
 * HttpEntity, ResponseEntity(Http Status 추가)
 * @return
 */
  @GetMapping("/response-body-string-v2")
  public ResponseEntity<String> responseBodyV2() {
	  return new ResponseEntity<>("ok", HttpStatus.OK);
  }
  @ResponseBody
  @GetMapping("/response-body-string-v3")
  public String responseBodyV3() {
	  return "ok";
  }
  @GetMapping("/response-body-json-v1")
  public ResponseEntity<HelloData> responseBodyJsonV1() {
	  HelloData helloData = new HelloData();
	  helloData.setUsername("userA");
	  helloData.setAge(20);
	  return new ResponseEntity<>(helloData, HttpStatus.OK);
  }
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @GetMapping("/response-body-json-v2")
  public HelloData responseBodyJsonV2() {
	  HelloData helloData = new HelloData();
	  helloData.setUsername("userA");
	  helloData.setAge(20);
	  return helloData;
  }
}
```

- `HttpServletResponse` 객체를 통해서 응답 메시지 전달 (V1)
    - 서블릿을 직접 다룰 때 처럼 HttpServletResponse 객체를 통해서 HTTP 메시지 바디에 직접 ok 응답 메시지를 전달한다.
        - `response.getWriter().write("ok")`
- `ResponseEntity`(V2)
    - `ResponseEntity`는 `HttpEntity` 를 상속 받았는데, `HttpEntity`는 HTTP 메시지의 헤더, 바디
    정보를 가지고 있다.
    - `ResponseEntity` 는 여기에 더해서 HTTP 응답 코드를 설정할 수 있다. `HttpStatus.CREATED` 로 변경하면 201 응답이 나가는 것을 확인할 수 있다.
- `@ResponseBody` (V3)
    - `@ResponseBody` 를 사용하면 view를 사용하지 않고, HTTP 메시지 컨버터를 통해서 HTTP 메시지를 직접 입력할 수 있다.
    - `ResponseEntity` 도 동일한 방식으로 동작한다.
- JSON 반환
    - `ResponseEntity 반환`
        - `ResponseEntity`에 문자가 아닌 객체를 실어 보내면, HTTP 메시지 컨버터를 통해서 JSON 형식으로 변환되어서 반환된다.
    - `@ResponseBody`
        - `ResponseEntity` 는 HTTP 응답 코드를 설정할 수 있는데, `@ResponseBody` 를 사용하면 이런 것을 설정하기 까다롭다.
        - `@ResponseStatus(HttpStatus.OK)` 애노테이션을 사용하면 응답 코드도 설정할 수 있다.
            - 다만 애노테이션이기 때문에 동적으로 변경은 불가능하다. 동적으로 변경하는 것이 필요하면 `ResponseEntity` 를 사용

> `@RestController`
`@Controller` 대신에 `@RestController` 애노테이션을 사용하면, 해당 컨트롤러에 모두
`@ResponseBody`가 적용되는 효과가 있다. 따라서 뷰 템플릿을 사용하는 것이 아니라, HTTP 메시지 바디에 직접 데이터를 입력한다. 이름 그대로 Rest API(HTTP API)를 만들 때 사용하는 컨트롤러이다.
이렇게 동작할 수 있는건 내부에 `@ResponseBody` 가 적용되어 있기 때문이다.
> 

---

## HTTP 메시지 컨버터

스프링 MVC는 다음의 경우에 HTTP 메시지 컨버터를 적용한다.

- HTTP 요청: `@RequestBody` , `HttpEntity(RequestEntity)`
- HTTP 응답: `@ResponseBody` , `HttpEntity(ResponseEntity)`

HTTP 메시지 컨버터 인터페이스

```java
public interface HttpMessageConverter<T> {
  boolean canRead(Class<?> clazz, @Nullable MediaType mediaType);
  boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType);
  List<MediaType> getSupportedMediaTypes();
  T read(Class<? extends T> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException;
  void write(T t, @Nullable MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException;
}
```

- 위에서 살펴봤듯이 HTTP 메시지 컨버터는 HTTP 요청, HTTP 응답 둘 다 사용된다.
- `canRead()` , `canWrite()` : 메시지 컨버터가 해당 클래스, 미디어타입을 지원하는지 체크
- `read()` , `write()` : 메시지 컨버터를 통해서 메시지를 읽고 쓰는 기능

### 스프링 부트 기본 메시지 컨버터(일부 생략)

```
0 = ByteArrayHttpMessageConverter
1 = StringHttpMessageConverter
2 = MappingJackson2HttpMessageConverter
```

스프링 부트는 다양한 메시지 컨버터를 제공하는데, 대상 클래스 타입과 미디어 타입 둘을 체크해서
사용여부를 결정한다. 만약 만족하지 않으면 다음 메시지 컨버터로 우선순위가 넘어간다.

- `ByteArrayHttpMessageConverter` : byte[] 데이터를 처리한다.
    - 클래스 타입: byte[] , 미디어타입: */* ,
    - 요청 예) `@RequestBody byte[] data`
    - 응답 예) `@ResponseBody return byte[]` 쓰기 미디어타입 `application/octet-stream`
- `StringHttpMessageConverter` : String 문자로 데이터를 처리한다.
    - 클래스 타입: String , 미디어타입: */*
    - 요청 예) `@RequestBody String data`
    - 응답 예) `@ResponseBody return "ok"` 쓰기 미디어타입 `text/plain`
- `MappingJackson2HttpMessageConverter` : application/json
    - 클래스 타입: 객체 또는 HashMap , 미디어타입 application/json 관련
    - 요청 예) `@RequestBody HelloData data`
    - 응답 예) `@ResponseBody return helloData` 쓰기 미디어타입 `application/json` 관련

**HTTP 요청 데이터 읽기**

- HTTP 요청이 오고, 컨트롤러에서 `@RequestBody` , `HttpEntity` 파라미터를 사용한다.
- 메시지 컨버터가 메시지를 읽을 수 있는지 확인하기 위해 `canRead()` 를 호출한다.
    - 대상 클래스 타입을 지원하는가.
        - 예) `@RequestBody` 의 대상 클래스 ( byte[] , String , HelloData )
    - HTTP 요청의 Content-Type 미디어 타입을 지원하는가.
        - 예) text/plain , application/json , */*
- `canRead()` 조건을 만족하면 `read()` 를 호출해서 객체 생성하고, 반환한다.

**HTTP 응답 데이터 생성**

- 컨트롤러에서 `@ResponseBody` , `HttpEntity` 로 값이 반환된다.
- 메시지 컨버터가 메시지를 쓸 수 있는지 확인하기 위해 canWrite() 를 호출한다.
    - 대상 클래스 타입을 지원하는가.
        - 예) return의 대상 클래스 (byte[] , String , HelloData)
    - HTTP 요청의 Accept 미디어 타입을 지원하는가.(정확히는 `@RequestMapping`의produces )
        - 예) text/plain , application/json , */*
- `canWrite()` 조건을 만족하면 `write()` 를 호출해서 HTTP 응답 메시지 바디에 데이터를 생성한다.

---

## 요청 매핑 핸들러 어뎁터 구조

그렇다면 HTTP 메시지 컨버터는 스프링 MVC에서 어디서 동작할까?

**스프링 MVC 구조**

![Untitled](https://user-images.githubusercontent.com/75190035/150939063-b7ca9b41-301d-4c6f-a54b-86af12b78eea.png)

모든 비밀은 애노테이션 기반의 컨트롤러, 그러니까 `@RequestMapping` 을 처리하는 핸들러 어댑터인 `RequestMappingHandlerAdapter` (요청 매핑 헨들러 어뎁터)에 있다.

**RequestMappingHandlerAdapter 동작 방식**

![Untitled 1](https://user-images.githubusercontent.com/75190035/150939085-54ffc2a3-dafd-48cc-b19f-9ac9dc886188.png)

### **ArgumentResolver (정확히는 HandlerMethodArgumentResolver)**

생각해보면, 애노테이션 기반의 컨트롤러는 매우 다양한 파라미터를 사용할 수 있었다.
`HttpServletRequest` , `Model` 은 물론이고, `@RequestParam` , `@ModelAttribute` 같은 애노테이션
그리고 `@RequestBody` , `HttpEntity` 같은 HTTP 메시지를 처리하는 부분까지 매우 큰 유연함을
보여준다.
이렇게 파라미터를 유연하게 처리할 수 있는 이유가 바로 `ArgumentResolver`덕분이다.
애노테이션 기반 컨트롤러를 처리하는 `RequestMappingHandlerAdaptor` 는 바로 이
`ArgumentResolver` 를 호출해서 컨트롤러(핸들러)가 필요로 하는 다양한 파라미터의 값(객체)을 생성한다.

그리고 이렇게 파리미터의 값이 모두 준비되면 컨트롤러를 호출하면서 값을 넘겨준다.
스프링은 30개가 넘는 `ArgumentResolver` 를 기본으로 제공한다.

> 참고: 가능한 파라미터 목록
[https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-annarguments](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-annarguments)
> 

```java
public interface HandlerMethodArgumentResolver {
  boolean supportsParameter(MethodParameter parameter);
	@Nullable
	Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
												 NativeWebRequest webRequest, @Nullable WebDataBinderFactory
												 binderFactory) throws Exception;
}
```

- 동작 방식
    - `ArgumentResolver` 의 `supportsParameter()` 를 호출해서 해당 파라미터를 지원하는지 체크하고, 지원하면 `resolveArgument()` 를 호출해서 실제 객체를 생성한다. 그리고 이렇게 생성된 객체가 컨트롤러 호출시 넘어가는 것이다.
    - 그리고 원한다면 직접 이 인터페이스를 확장해서 원하는 ArgumentResolver 를 만들 수도 있다. 이것이 인터페이스 구현의 장점

> 참고 : 확장
스프링은 다음을 모두 인터페이스로 제공한다. 따라서 필요하면 언제든지 기능을 확장할 수 있다.
`HandlerMethodArgumentResolver`
`HandlerMethodReturnValueHandler`
`HttpMessageConverter`
> 

### **ReturnValueHandler (정확히는 HandlerMethodReturnValueHandler)**

`ArgumentResolver`와 비슷한데, 이것은 응답 값을 변환하고 처리한다.

컨트롤러에서 String으로 뷰 이름을 반환해도, 동작하는 이유가 바로 `ReturnValueHandler` 덕분이다.

스프링은 10여개가 넘는 ReturnValueHandler 를 지원한다.

- 예) `ModelAndView` , `@ResponseBody` , `HttpEntity` , `String`

> 참고: 가능한 응답 값 목록은 다음 공식 메뉴얼에서 확인할 수 있다.
[https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-annreturn-types](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-annreturn-types)
> 

### HTTP 메시지 컨버터

**HTTP 메시지 컨버터 위치**

![Untitled 2](https://user-images.githubusercontent.com/75190035/150939125-631acd32-0905-4a11-a403-96d81e09fd5a.png)

- **요청**의 경우 `@RequestBody` 를 처리하는 `**ArgumentResolver**`가 있고, `HttpEntity`를 처리하는
`**ArgumentResolver**`가 있다. 이 **`ArgumentResolver`**들이 HTTP 메시지 컨버터를 사용해서 필요한
객체를 생성하는 것이다.
- **응답**의 경우 `@ResponseBody` 와 `HttpEntity` 를 처리하는 `ReturnValueHandler`가 있다. 그리고
여기에서 HTTP 메시지 컨버터를 호출해서 응답 결과를 만든다.
- 스프링 MVC는
    - `@RequestBody` , `@ResponseBody` 가 있으면, `RequestResponseBodyMethodProcessor` (`ArgumentResolver`)
    - `HttpEntity` 가 있으면 `HttpEntityMethodProcessor` (`ArgumentResolver`)를 사용한다.
