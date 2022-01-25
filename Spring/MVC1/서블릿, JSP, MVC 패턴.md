# 서블릿, JSP, MVC 패턴

>[김영한-스프링 MVC 1편](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-mvc-1#)

## 회원 관리 웹 애플리케이션

- 요구사항
    - 회원 정보
        - 이름: username
        - 나이: age
    - 기능 요구사항
        - 회원 저장
        - 회원 목록 조회

---

## 서블릿 사용

```java
@WebServlet(name = "memberListServlet", urlPatterns = "/servlet/members")
public class MemberListServlet extends HttpServlet {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Member> members = memberRepository.findAll();

        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");
        PrintWriter w = response.getWriter();

        w.write("<html>");
        w.write("<head>");
        w.write(" <meta charset=\"UTF-8\">");
        w.write(" <title>Title</title>");
        w.write("</head>");
        w.write("<body>");
        w.write("<a href=\"/index.html\">메인</a>");
        w.write("<table>");
        w.write(" <thead>");
        w.write(" <th>id</th>");
        w.write(" <th>username</th>");
        w.write(" <th>age</th>");
        w.write(" </thead>");
        w.write(" <tbody>");
        for (Member member : members) {
            w.write(" <tr>");
            w.write(" <td>" + member.getId() + "</td>");
            w.write(" <td>" + member.getUsername() + "</td>");
            w.write(" <td>" + member.getAge() + "</td>");
            w.write(" </tr>");
        }
        w.write(" </tbody>");
        w.write("</table>");
        w.write("</body>");
        w.write("</html>");
    }
}
```

- 자바 코드만으로 HTML을 구현한 것이다. 서블릿 덕분에 동적으로 원하는 HTML을 만들 수 있다.
    - 정적인 HTML문서라면 위 코드 처럼 멤버가 계속 달라질 때마다 새로운 화면을 만들 수 없었을 것이다.
- 그러나 이렇게 코드를 짜는 것은 매우 비효율 적이다. 자바 코드로 모든 HTML을 만드는 것이 아니라 HTML 중 동적으로 변해야 하는 부분만 자바 코드로 할 수 있으면 더 편할 것이다.
    - 이것을 해주는 것이 템플릿 엔진이다. 템플릿 엔진은 HTML문서에서 필요한 곳만 코드를 적용해서 동적으로 변경할 수 있다.
        - JSP, Thymeleaf, Freemarker, Velocity 등

> JSP는 성능과 기능면에서 다른 템플릿 엔진과 경쟁이 밀려 사장되어가는 추세다. 스프링에서는 Thymeleaf를 사용하는 추세
> 

---

## JSP 사용

- JSP를 사용하기 위한 라이브러리

```
implementation 'org.apache.tomcat.embed:tomcat-embed-jasper'
implementation 'javax.servlet:jstl'
```

- jsp 코드

```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
 <title>Title</title>
</head>
<body>
<form action="/jsp/members/save.jsp" method="post">
 username: <input type="text" name="username" />
 age: <input type="text" name="age" />
 <button type="submit">전송</button>
</form>
</body>
</html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
```

```java
<%@ page import="hello.servlet.domain.member.MemberRepository" %>
<%@ page import="hello.servlet.domain.member.Member" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
//request, response 사용 가능
 MemberRepository memberRepository = MemberRepository.getInstance();
 System.out.println("save.jsp");
 String username = request.getParameter("username");
 int age = Integer.parseInt(request.getParameter("age"));
 Member member = new Member(username, age);
 System.out.println("member = " + member);
 memberRepository.save(member);
%>
<html>
<head>
 <meta charset="UTF-8">
</head>
<body>
성공
<ul>
 <li>id=<%=member.getId()%></li>
 <li>username=<%=member.getUsername()%></li>
 <li>age=<%=member.getAge()%></li>
</ul>
<a href="/index.html">메인</a>
</body>
</html>
```

```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
```

JSP 문서를 시작할 때는  위 코드를 사용하며, JSP문서 내부에서 자바코드 또한 사용가능하다.

- 자바코드를 사용할 때는 ‘`<% ~~ %>`’ 괄호 내부에 자바코드를 작성
    - `<%= ~~ %>` 를 사용해 자바코드를 출력할 수 있다.
    - `<%@ page import="hello.servlet.domain.member.Member" %>` import 문 또한 사용가능
- 코드는 HTML을 중심으로 하고, 중간에 동적인 부분만 자바 코드로 작성이 가능하게 됬다.

### 한계

> 서블릿으로 개발할 때는 뷰(View)화면을 위한 HTML을 만드는 작업이 자바 코드에 섞여 복잡하고, 비효율적이였다. JSP를 사용해 뷰(View)를 생성하는 HTML작업을 깔끔하게 가져가고, 동적인 부분만 자바코드로 변경할 수 있었다.다만, 회원 저장 JSP의 경우 코드의 절반은 비지니스 로직이고 그것이 모두 JSP에 노출되어 있다. 이렇게 하면 JSP가 너무 많은 역할을 한다.
> 

MVC 패턴의 등장

> MVC 패턴을 사용하면, 비지니스 로직은 서블릿 처럼 다른곳에서 처리하고, JSP는 목적에 맞게 HTML로 화면(VIew)을 그리는 일에 집중하도록 할 수 있다.
> 

---

## MVC 패턴

개요

- 하나의 서블릿, JSP 만으로 비지니스 로직과 뷰 렌더링까지 모두 처리하게 되는 것은 너무 많은 역할을 부여한 것이고, 유지보수가 어려워진다.
- 변경의 라이프 사이클
    - UI를 일부 수정하는 일과 비지니스 로직을 수정하는 일은 각각 다르게 발생할 가능성이 매우 높고 대부분 서로에게 영향을 주지 않는다.
    - 이렇게 변경의 라이프 사이클이 다른 부분을 하나의 코드로 관리하는 것은 유지보수하기 좋지 않다.
- 기능 특화
    - JSP 같은 뷰 템플릿은 화면을 렌더링 하는데 최적화 되어 있기 때문에 이 부분의 업무만 담당하는 것이 가장 효과적이다.

### Model View Controller

MVC패턴은 하나의 JSP, 서블릿으로 처리하던 것을 컨트롤러와 뷰라는 영역으로 서로 역할을 나눈 것이다.

- 컨트롤러: HTTP 요청을 받아서 파라미터를 검증하고, 비지니스 로직을 실행한다. 그리고 뷰에 전달할 결과 데이터를 조회해서 모델에 담는다.
- 모델: 뷰에 출력할 데이터를 담아둔다. 뷰가 필요한 데이터를 모두 모델에 담아서 전달해 주는 덕분에 뷰는 비지니스 로직이나 데이터 접근을 모르고, 화면을 렌더링 하는 일에만 집중할 수 있다.
- 뷰: 모델에 담겨있는 데이터를 사용해서 화면을 렌더링한다.

> 컨트롤러에 비지니스 로직을 둘 수도 있지만, 컨트롤러가 너무 많은 업무를 담당하게 된다. 일반적으로 비지니스 로직은 서비스라는 계층을 별도로 만들어서 처리한다. 그리고 컨트롤러는 비지니스 로직이 있는 서비스를 호출하는 것이다.
> 

MVC 패턴 이전

![Untitled](https://user-images.githubusercontent.com/75190035/150504360-281a5ee5-9564-4afe-a7c1-71d662562a01.png)

MVC 패턴 이후

![Untitled 1](https://user-images.githubusercontent.com/75190035/150504380-1f93612c-54cf-4039-9499-4321acbaa2db.png)

컨트롤러 예시

```java
@WebServlet(name = "mvcMemberSaveServlet", urlPatterns = "/servlet-mvc/members/save")
public class MvcMemberSaveServlet extends HelloServlet {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

- `dispatcher.forward()` : 다른 서블릿이나 JSP로 이동할 수 있는 기능이다. 서버 내부에서 다시 호출이 발생한다.
- `HttpServletRequest`를 Model로 사용한다.
    - `setAttribute()` 를 사용해 request 객체에 데이터를 보관해서 뷰에 전달할 수 있다.
- /WEB-INF:
    - 이 경로 안에 JSP가 있으면 외부에서 직접 호출할 수 있다. MVC 패턴에서 요구하는 것은 뷰를 컨트롤러로 호출하는 것이다.

<aside>
📌 redirect vs forward                                                                                                                               리다이렉트는 실제 클라이언트에 응답이 나갔다가, 클라이언트가 redirect 경로로 다시 호출한다. 따라서 클라이언트가 인지할 수 있고, URL 경로도 실제로 변경된다.                                                                   반면에 포워드는 서버 내부에서 일어나는 호출이기 때문에 클라이언트가 인지하지 못하고, URL경로도 변경되지 않는다.

</aside>

뷰 예시

```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
 <meta charset="UTF-8">
 <title>Title</title>
</head>
<body>
<!-- 상대경로 사용, [현재 URL이 속한 계층 경로 + /save] -->
<form action="save" method="post">
 username: <input type="text" name="username" />
 age: <input type="text" name="age" />
 <button type="submit">전송</button>
</form>
</body>
</html>
```

```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
 <meta charset="UTF-8">
 <title>Title</title>
</head>
<body>
<a href="/index.html">메인</a>
<table>
 <thead>
 <th>id</th>
 <th>username</th>
 <th>age</th>
 </thead>
 <tbody>
 <c:forEach var="item" items="${members}">
 <tr>
 <td>${item.id}</td>
 <td>${item.username}</td>
 <td>${item.age}</td>
 </tr>
 </c:forEach>
 </tbody>
</table>
</body>
</html>
```

- 뷰는 모델에 저장된 객체를 `<%= request.getAttribute(”member”)%>`로 꺼내야 하지만 JSP는 다른 문법을 제공한다.
    - JSP는 ${} 문법을 모델의 데이터를 꺼내는데 사용하도록 지원한다.
- 모델에 담아둔 members를 JSP가 제공하는 taglib를 사용해 반복 출력했다.
    - 선언문
        - `<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>`
- new-form의 action을 보면 “/”를 사용하지 않아 상대경로로 시작한다.
    - 상대경로
        - 현재 URL이 속한 계층 경로 + 상대경로(save)
        - 현재 계층 경로: /servlet-mvc/members/
        - 결과: /servlet-mvc/members/save

## 한계

MVC 패턴의 적용으로 역할의 구분을 어느정도 이루어냈다.

하지만 컨트롤러에서는 많은 중복이 발생하고, 필요하지 않아 보이는 코드도 발생했다.

1. 포워드 중복
- Controller에서 View로 이동하기 위해서 포워드문은 필수적이다. 그렇기 때문에 모든 컨트롤러에서 같은 코드를 사용하고 있다.
1. ViewPath 중복
- 경로에서도 유사한 부분이 많다.
    - prefix: /WEB-INF/view/
    - suffix: .jsp
1. 사용하지 않는 코드
- 다음 코드는 사용할 때도 그렇지 않을 때도 있다. 특히 response는 현재 단계에서는 사용하지 않았다.
- 그리고 `HttpServletRequest, Response`를 사용하는 코드는 테스트 작성이 어렵다.

```java
HttpServletRequest request, HttpServletResponse response
```

1. 공통 처리가 어렵다.
- 기능이 복잡해질 수록 컨트롤러에서 공통으로 처리해야 하는 부분이 많아질 것이다. 단순히 공통기능을 메서드로 만들면 될 것 같지만 메서드를 항상 호출해야 하고, 실수로 호출하지 않으면 문제가 발생할 것이다. 또한 이 호출과정 또한 중복이다.

<aside>
📌 정리하면 공통처리가 어렵다는 문제점이 있다. 이부분을 프런트 컨트롤러패턴을 도입해 해결할 수 있다.

</aside>
