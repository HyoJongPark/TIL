# MVC 프레임워크

![Untitled](https://user-images.githubusercontent.com/75190035/150505352-b9345ad1-6f67-420f-9d44-807e8788a9b4.png)

프런트 컨트롤러 도입 전

![Untitled 1](https://user-images.githubusercontent.com/75190035/150505359-9221749b-d1be-4440-8b54-1c856a820060.png)

프런트 컨트롤러 도입 후

## FrontController 패턴 특징

- 프론트 컨트롤러 서블릿 하나로 클라이언트의 요청을 받음
- 프론트 컨트롤러가 요청에 맞는 컨트롤러를 찾아서 호출
- 공통처리 가능
- 프론트 컨트롤러를 제외한 나머지 컨트롤러는 서블릿을 사용하지 않아도 됨

> 스프링 웹 MVC의 핵심도 FrontController다. 스프링 웹 MVC의 `DispatcherServlet`이 FrontController패턴으로 구현되어 있음
> 

---

## 프론트 컨트롤러 도입 V1

### 구조

![Untitled 2](https://user-images.githubusercontent.com/75190035/150505381-78adf703-41b0-41fd-98ce-577f7a6053c6.png)

```java
public interface ControllerV1 {
    void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
```

```java
public class MemberSaveControllerV1 implements ControllerV1 {

    private MemberRepository memberRepository = MemberRepository.getInstance();
    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));

        Member member = new Member(username, age);
        memberRepository.save(member);

        //Model에 데이터를 보관
        request.setAttribute("member", member);

        String viewPath = "/WEB-INF/views/save-result.jsp";
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);
    }
}
```

```java
@WebServlet(name = "frontControllerServletV1", urlPatterns = "/front-controller/v1/*")
public class FrontControllerServletV1 extends HttpServlet {

    private Map<String, ControllerV1> controllerMap = new HashMap<>();

    public FrontControllerServletV1() {
        controllerMap.put("/front-controller/v1/members/new-form", new MemberFormControllerV1());
        controllerMap.put("/front-controller/v1/members/save", new MemberSaveControllerV1());
        controllerMap.put("/front-controller/v1/members", new MemberListControllerV1());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("FrontControllerServletV1.service");

        String requestURI = request.getRequestURI();

        ControllerV1 controller = controllerMap.get(requestURI);
        if (controller == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        controller.process(request,response);
    }
}
```

- 프런트 컨트롤러 서블릿 하나로 클라이언트의 요청을 받는다 → 이전에는 각 컨트롤러마다 서블릿을 상속했다.
- 프런트 컨트롤러에 매핑된 URL이 들어올 때마다 그에 맞는 컨트롤러를 호출해 준다.

---

## View 분리(V2)

![Untitled 3](https://user-images.githubusercontent.com/75190035/150505405-9c12939d-f430-4ffc-837b-654a6b88168b.png)

```java
public class MyView {
    private String viewPath;

    public MyView(String viewPath) {
        this.viewPath = viewPath;
    }

    public void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);
    }
}
```

- MyView 에서는 기존 컨트롤러에서 반복되던 JSP를 불러오기 위한 코드를 줄여주는 역할을 한다.

```java
public interface ControllerV2 {
    MyView process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
```

```java
public class MemberSaveControllerV2 implements ControllerV2 {
    private MemberRepository memberRepository = MemberRepository.getInstance();
    @Override
    public MyView process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));

        Member member = new Member(username, age);
        memberRepository.save(member);

        //Model에 데이터를 보관
        request.setAttribute("member", member);

        return new MyView("/WEB-INF/views/save-result.jsp");
    }
}
```

```java
@WebServlet(name = "frontControllerServletV2", urlPatterns = "/front-controller/v2/*")
public class FrontControllerServletV2 extends HttpServlet {

    private Map<String, ControllerV2> controllerMap = new HashMap<>();

    public FrontControllerServletV2() {
        controllerMap.put("/front-controller/v2/members/new-form", new MemberFormControllerV2());
        controllerMap.put("/front-controller/v2/members/save", new MemberSaveControllerV2());
        controllerMap.put("/front-controller/v2/members", new MemberListControllerV2());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        ControllerV2 controller = controllerMap.get(requestURI);
        if (controller == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        MyView view = controller.process(request, response);
        view.render(request, response);
    }
}
```

- 이제 컨트롤러들은 직접 `dispatcher.forward()`로 JSP화면을 불러오지 않고, MyView객체만 생성해서 반환한다.
- 그리고 프런트 컨트롤러에서 반환된 MyView 객체를 받아 `view.render()`를 호출하면 `forward`로직을 실행해 JSP가 실행된다.

---

## Model 추가(V3)

- 서블릿 종속성 제거
    - 컨트롤러에서 `HttpServletRequest` 와 `HttpServletResponse`는 꼭 필요해 보이지 않는다.
        - 요청 파라미터 정보는 자바의 Map 으로 대신 넘기도록하면 컨트롤러가 위의 서블릿 기술을 몰라도 동작할 수 있다.
        - request 객체를 Model 로 사용하는 대신 별도의 Model 객체를 만들어 반환하면 된다.
        - 결과적으로 구현 코드가 단순해지고, 테스트코드 작성이 쉬워진다.
- 뷰 이름 중복 제거
    - 컨트롤러에서 지정하는 뷰 이름에 중복이 발생한다.
        - 컨트롤러에서는 뷰의 논리 이름을 반환하고, 실제 물리 위치 이름은 프런트 컨트롤러에서 처리하도록 변경한다.
        - 결과적으로 이후 뷰의 폴더 위치가 변경되어도 프런트 컨트롤러만 고치면 된다.

### 구조

![Untitled 4](https://user-images.githubusercontent.com/75190035/150505425-33fd0b29-792b-46ed-99ba-a64ef9567d04.png)

### ModelView

기존에는 컨트롤러에서 서블릿에 종속적인 `HttpServletRequest, HttpServletResponse` 를 사용했다.

또한 Model도 `request.setAttribute()`를 통해 데이터를 저장하고 뷰에 전달했다.

서블릿의 종속성을 제거하기 위해 Model을 직접 만들고, 추가로 View 이름까지 전달하는 객체를 만든다.

```java
public class MyView {
		...
		//기존 render
		public void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);
    }
		//V3에서 추가된 render
    public void render(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        modelToRequestAttribute(model, request);
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);
    }

    private void modelToRequestAttribute(Map<String, Object> model, HttpServletRequest request) {
        model.forEach((key, value) -> request.setAttribute(key, value));
    }
}
```

기존 MyView 에 model 객체를 추가로 받는 render 를 추가해주었다.

V3 에서는 model에 담긴 정보를 `request.setAttribute`로 모델에 넣어준다.

```java
@Getter
@Setter
public class ModelView {
    private String viewName;
    private Map<String, Object> model = new HashMap<>();

    public ModelView(String viewName) {
        this.viewName = viewName;
    }
}
```

```java
public interface ControllerV3 {
    ModelView process(Map<String, String> paramMap);
}
```

```java
public class MemberSaveControllerV3 implements ControllerV3 {

    private MemberRepository memberRepository = MemberRepository.getInstance();
    @Override
    public ModelView process(Map<String, String> paramMap) {
        String username = paramMap.get("username");
        int age = Integer.parseInt(paramMap.get("age"));

        Member member = new Member(username, age);
        memberRepository.save(member);

        ModelView mv = new ModelView("save-result");
        mv.getModel().put("member", member);
        return mv;
    }
}
```

```java
@WebServlet(name = "frontControllerServletV3", urlPatterns = "/front-controller/v3/*")
public class FrontControllerServletV3 extends HttpServlet {

    private Map<String, ControllerV3> controllerMap = new HashMap<>();

    public FrontControllerServletV3() {
        controllerMap.put("/front-controller/v3/members/new-form", new MemberFormControllerV3());
        controllerMap.put("/front-controller/v3/members/save", new MemberSaveControllerV3());
        controllerMap.put("/front-controller/v3/members", new MemberListControllerV3());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        ControllerV3 controller = controllerMap.get(requestURI);
        if (controller == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Map<String, String> paramMap = createParamMap(request);
        ModelView mv = controller.process(paramMap);

        String viewName = mv.getViewName();
        MyView view = viewResolver(viewName);

        view.render(mv.getModel() ,request, response);
    }

    private MyView viewResolver(String viewName) {
        return new MyView("/WEB-INF/views/" + viewName + ".jsp");
    }

    private Map<String, String> createParamMap(HttpServletRequest request) {
        Map<String, String> paramMap = new HashMap<>();
        request.getParameterNames().asIterator()
                .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
        return paramMap;
    }
}
```

- 뷰 리졸버
    - 컨트롤러가 반환한 놀리 뷰 이름을 실제 뷰 경로로 변경한다. 그리고 실제 물리 경로가 있는 MyView 객체를 반환한다.
        - 논리 뷰 이름 : members
        - 물리 뷰 경로 : /WEB-INF/views/members.jsp
- view.render()
    - 뷰 객체를 통해서 HTML 화면을 렌더링 한다.
    - 뷰 객체의 render() 는 모델 정보도 함께 받는다.
    - JSP 는 `request.getAttribute()`로 데이터를 조회하기 때문에 모델의 객체를 꺼내서 `request.setAttribute()`로 담아둔다.
    - JSP를 렌더링 한다.

<aside>
📌 V3에서는 프런트 컨트롤러를 제외한 컨트롤러는 서블릿에 종속적인 기술을 사용하지 않는다.  프런트 컨트롤러는 요청이 들어온 컨트롤러의 process 로직을 실행하고, 들어온 정보를 ModelView 의 viewName에 뷰 이름을 저장하고, ModelView의 model 객체에 정보를 저장한다.                                                                                                                                         프런트 컨트롤러는 요청이 들어온 viewName맞는 컨트롤러 로직을 실행하고, 실행된 컨트롤러는 ModelView에 정보를 담아 리턴한다.                                                                                                **이전에는 각 컨트롤러에서 모델에 데이터를 직접 저장했다면, 이제는 컨트롤러는 MyView 객체에 정보를 담아 프런트 컨트롤러에 전달하고, 프런트 컨트롤러에서 모델에 정보를 저장하는 것 까지 수행한다.**

</aside>

---

## 단순하고 실용적인 컨트롤러(V4)

### 구조

![Untitled 5](https://user-images.githubusercontent.com/75190035/150505446-e2f46a08-154d-43ca-89b1-8751578eb1fb.png)

기본적인 구조는 V3와 같지만 컨트롤러가 ModelView를 반환하는 것이 아닌 ViewName만 반환하도록 한다.

```java
public interface ControllerV4 {
    String process(Map<String, String> paramMap, Map<String, Object> model);
}
```

```java
public class MemberSaveControllerV4 implements ControllerV4 {

    MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
        String username = paramMap.get("username");
        int age = Integer.parseInt(paramMap.get("age"));

        Member member = new Member(username, age);
        memberRepository.save(member);

        model.put("member", member);

        return "save-result";
    }
}
```

```java
@WebServlet(name = "frontControllerServletV4", urlPatterns = "/front-controller/v4/*")
public class FrontControllerServletV4 extends HttpServlet {

    private Map<String, ControllerV4> controllerMap = new HashMap<>();

    public FrontControllerServletV4() {
        controllerMap.put("/front-controller/v4/members/new-form", new MemberFormControllerV4());
        controllerMap.put("/front-controller/v4/members/save", new MemberSaveControllerV4());
        controllerMap.put("/front-controller/v4/members", new MemberListControllerV4());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        ControllerV4 controller = controllerMap.get(requestURI);
        if (controller == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Map<String, String> paramMap = createParamMap(request);
        Map<String, Object> model = new HashMap<>();
        String viewName = controller.process(paramMap, model);

        MyView view = viewResolver(viewName);

        view.render(model ,request, response);
    }

    private MyView viewResolver(String viewName) {
        return new MyView("/WEB-INF/views/" + viewName + ".jsp");
    }

    private Map<String, String> createParamMap(HttpServletRequest request) {
        Map<String, String> paramMap = new HashMap<>();
        request.getParameterNames().asIterator()
                .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
        return paramMap;
    }
}
```

이전에는 각 컨트롤러가 ModelView 객체 내부에 있는 model에 정보를 담고, 뷰 이름을 함께 반환했다.

지금은 ModelView를 생성하지 않고, 뷰 이름을 반환하고, 파라미터로 받은 model에 정보를 저장한다.

---

## 유연한 컨트롤러 (V5)

지금은 컨트롤러의 버전이 고정되어있어 다른 컨트롤러를 사용하고 싶어도 사용할 수 없다. 이 문제점을 어댑터 패턴으로 해결 할 수 있다.

## 어댑터 패턴

현재 ControllerV3 와 ControllerV4 는 완전히 다른 인터페이스다. 이때 어댑터 패턴을 사용해 프론트 컨트롤러가 다양한 방식의 컨트롤러를 처리할 수 있도록 변경 가능하다

### 구조

![Untitled 6](https://user-images.githubusercontent.com/75190035/150505467-ffd689a1-9797-4816-b0f9-b93e4553ee70.png)

- 핸들러 어댑터:
    - 중간에 어댑터 역할을 하는 핸들러 어댑터다. 여기서 어댑터의 역할을 수행해 다양한 종류의 컨트롤러를 호출할 수 있게 된다.
- 핸들러:
    - 컨트롤러의 이름을 더 넓은 범위인 핸들러로 변경. 이제 어댑터가 있기 때문에 컨트롤러의 개념 뿐만 아니라 어떠한 것이든 해당하는 종류의 어댑터만 있으면 다 처리할 수 있기 때문이다.

```java
public interface MyHandlerAdapter {
    boolean supports(Object handler);

    ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws ServletException, IOException;
}
```

- `boolean supports(Object handler):`
    - handler는 컨트롤러를 말한다
    - 어댑터가 해당 컨트롤러를 처리할 수 있는지 판단하는 매서드다.
- `ModelView handle()`
    - 어댑터는 실제 컨트롤러를 호출, 그 결과로 ModelView를 반환한다.
    - 실제 컨트롤러가 ModelView를 반환하지 못하면, 어댑터가 직접 생성해서 반환해야한다.
    - 이전에는 프런트 컨트롤러가 각 컨트롤러를 호출, 변경 후에는 이 어댑터를 통해 호출한다.

```java
public class ControllerV3HandlerAdapter implements MyHandlerAdapter {
    @Override
    public boolean supports(Object handler) {
        return (handler instanceof ControllerV3);
    }

    @Override
    public ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException, IOException {
        ControllerV3 controller = (ControllerV3) handler;

        Map<String, String> paramMap = createParamMap(request);
        ModelView mv = controller.process(paramMap);

        return mv;
    }

    private Map<String, String> createParamMap(HttpServletRequest request) {
        Map<String, String> paramMap = new HashMap<>();
        request.getParameterNames().asIterator()
                .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
        return paramMap;
    }
}
```

```java
public class ControllerV4HandlerAdapter implements MyHandlerAdapter {
    @Override
    public boolean supports(Object handler) {
        return (handler instanceof ControllerV4);
    }

    @Override
    public ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException, IOException {
        ControllerV4 controller = (ControllerV4) handler;

        Map<String, String> paramMap = createParamMap(request);
        Map<String, Object> model = new HashMap<>();

        String viewName = controller.process(paramMap, model);
        ModelView mv = new ModelView(viewName);
        mv.setModel(model);

        return mv;
    }

    private Map<String, String> createParamMap(HttpServletRequest request) {
        Map<String, String> paramMap = new HashMap<>();
        request.getParameterNames().asIterator()
                .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
        return paramMap;
    }
}
```

```java
@WebServlet(name = "frontControllerV5", urlPatterns = "/front-controller/v5/*")
public class FrontControllerV5 extends HttpServlet {

    private final Map<String, Object> handlerMappingMap = new HashMap<>();
    private final List<MyHandlerAdapter> handlerAdapters = new ArrayList<>();

    public FrontControllerV5() {
        initHandlerMappingMap();
        initHandlerAdapters();
    }

    private void initHandlerMappingMap() {
        //v3
        handlerMappingMap.put("/front-controller/v5/v3/members/new-form", new MemberFormControllerV3());
        handlerMappingMap.put("/front-controller/v5/v3/members/save", new MemberSaveControllerV3());
        handlerMappingMap.put("/front-controller/v5/v3/members", new MemberListControllerV3());
        //v4
        handlerMappingMap.put("/front-controller/v5/v4/members/new-form", new MemberFormControllerV4());
        handlerMappingMap.put("/front-controller/v5/v4/members/save", new MemberSaveControllerV4());
        handlerMappingMap.put("/front-controller/v5/v4/members", new MemberListControllerV4());
    }

    private void initHandlerAdapters() {
        handlerAdapters.add(new ControllerV3HandlerAdapter());
        handlerAdapters.add(new ControllerV4HandlerAdapter());

    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Object handler = getHandler(request);

        if (handler == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        MyHandlerAdapter adapter = getHandlerAdapter(handler);
        ModelView mv = adapter.handle(request, response, handler);

        MyView view = viewResolver(mv.getViewName());

        view.render(mv.getModel() ,request, response);
    }

    private Object getHandler(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return handlerMappingMap.get(requestURI);
    }

    private MyHandlerAdapter getHandlerAdapter(Object handler) {
        for (MyHandlerAdapter adapter : handlerAdapters) {
            if (adapter.supports(handler)) {
                return adapter;
            }
        }
        throw new IllegalArgumentException("handler adapter를 찾을 수 없습니다. handler = " + handler);
    }

    private MyView viewResolver(String viewName) {
        return new MyView("/WEB-INF/views/" + viewName + ".jsp");
    }

}
```

ControllerV4 에서는 process 의 리턴타입으로 String을 반환한다. 하지만 어댑터 패턴을 적용한 프런트 컨트롤러는 ModelView 객체를 원한다.  따라서 어댑터 V4는 그에 맞는 형식으로 리턴타입을 변환하도록 지원한다.
