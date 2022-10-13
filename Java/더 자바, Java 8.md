# 더 자바, Java 8

> [백기선 - 더 자바, Java 8](https://www.inflearn.com/course/the-java-java8#)
> 

자바8은 2014년에 출시한 자바 LTS(Long-Term-Support)버전이다.

- 주요기능
    - 람다 표현식
    - 메소드 레퍼런스
    - 스트림 API
    - Optional
    - …

---

# 1. 함수형 인터페이스와 람다

## 1.1 소개

**함수형 인터페이스 (Functional Interface)**

- **단 하나의 추상 메소드를** 가지고 있는 인터페이스
- `@FunctionalInterface` 애노테이션을 가지고 있는 인터페이스

**람다 표현식 (Lambda Expressions)**

```java
//익명 내부 클래스
Foo foo = new Foo() {
	@Override
	public void doIt() {
		System.out.println("Hi");
	}
}

//람다를 사용한 함수형 인터페이스 구현
Foo foo = () -> System.out.println("Hi");
```

- 함수형 인터페이스의 인스턴스를 만드는 방법으로 쓰일 수 있다.
- 메소드의 매개변수, 리턴 타입, 변수로 만들어 사용할 수도 있다.

## 1.2 자바에서 제공하는 함수형 인터페이스

Java에서는 여러 함수형 인터페이스를 기본으로 제공한다.

하나의 예는 아래와 같다.

- `Function<T, R>`
    - T 타입을 받아서 R 타입을 리턴하는 함수 인터페이스
        - `R apply(T t)`
    - 함수 조합용 메소드
        - `andThen`
        - `compose`

> 자바가 제공하는 모든 함수형 인터페이스는 위 메서드 처럼 정의되어 있으며,
> 
> 
> **몇 개의 타입을 받아서 → 어떤 타입의 값을 리턴**하는지에 따라 다양하게 정의되어 있다.
> [https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html](https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html)
> 

## 1.3 람다 표현식

```java
int currentValue = 10; //effective final
BinaryOperator<Integer> sum = 
	(Integer a, Integer b) -> currentValue + a + b;
```

- 구성
    - `(인자 리스트) → {바디}`
- 인자 리스트
    - 위 예시처럼 타입을 명시해도 되지만 생략 가능하다.
    - `(a, b) → a + b`
- 바디
    - 화살표 오른쪽에 함수의 본문을 정의한다.
    - 리턴 값이 있을 경우 `return`은 생략 가능하다.
- 변수 캡처(Variable capture)
    - 로컬 변수 캡처
        - `final`키워드가 존재하는 지역 변수는 람다 표현식에서 참조할 수 있다.
        - `final`키워드가 없다면, 동시성 문제를 방지하기 위해 컴파일러가 방지한다.
    - 익명 클래스 구현체와 달리 쉐도윙하지 않는다.
        - 익명 클래스는 새로 스콥을 만들지만, 람다는 람다를 감싸고 있는 스콥과 같다.

> **effective final**
자바 8에서 지원하는 기능으로 사실상 `final`인 변수.
`final`키워드를 사용하지 않은 변수를 익명 클래스 구현체 또는 람다에서 참조할 수 있다.
후에 해당 변수가 변경된다면 `effective final`이 아니다.

**쉐도윙(Shadowing)**
쉐도윙이란 마치 그림자가 물체를 덮는 것처럼 내부에 선언한 변수로 외부의 변수 값을 덮는 것으로, 람다는 변수의 범위로 인해 쉐도윙이 되지않는다.
> 

## 1.4 메소드 레퍼런스

```java
Public class Greeting {
	public static String hi(String name) {
		return "hi" + name;
	}
}

//람다 표현식
UnaryOperator<String> hi = (name) -> "hi" + name;

//메소드 레퍼런스
UnaryOperator<String> hi = Greeting::hi;
```

람다가 하는 일은 기존 메소드 또는 생성자를 호출하는 일이다.

메소드 레퍼런스를 사용하면 이 또한 매우 간결하게 표현할 수 있다.

---

# 2. 인터페이스의 변화

## 2.1 인터페이스 기본 메소드와 스태틱 메소드

```java
public interface Foo {
	void printName();

	/**
	* @implSpec
	* 이 구현체는 getName()으로 가져온 문자열을 대문자로 변환, 출력한다.
	* getName()이 Null을 반환하면 런타임에러 발생
	**/
	default void printNameUpperCase() {
		System.out.println(getName().toUpperCase());
	}

	String getName();

	static void printAnything() {
		System.out.println("any");
	}
}

public interface Bar extends Foo {
	void printNameUpperCase();
}
```

**기본 메소드(Default Methods)**

- 인터페이스에 메소드 선언이 아닌 구현체를 제공하는 방법
- 해당 인터페이스를 구현한 클래스를 깨뜨리지 않고 새 기능을 추가할 수 있다.
- 기본 메소드는 구현체가 모르게 추가된 기능으로 그만큼 리스크가 있다.
    - 컴파일 에러는 아니지만 구현체에 따라 런타임 에러가 발생 가능
    - 반드시 문서화(`@impleSpec` 활용)
- `Obejct`가 제공하는 기능(`equals, hasCode`)는 기본 메소드로 제공할 수 없다.
- 본인이 수정할 수 있는 인터페이스에만 기본 메소드를 제공할 수 있다.
- 인터페이스를 상속받는 인터페이스에서 다시 추상 메소드로 변경할 수 있다.
- 인터페이스 구현체가 재정의 할 수도 있다.

**스태틱 메소드(Static Method)**

- 해당 타입 관련 헬터 또는 유틸리티 메소드를 제공할 때 인터페이스에 스태틱 메소드를 제공할 수 있다.

## 2.2 자바 8 API에서 기본 메소드와 스태틱 메소드

**Iterable의 기본 메소드**

- `forEach()`
- `spliterator()`

**Collection의 기본 메소드**

- `stream() / parallelStream()`
- `removeIf(Predicate)`
- `spliterator()`

**Comparator의 기본 메소드 및 스태틱 메소드**

- `reversed()`
- `thenComparing()`
- `static reverseOrder() / naturalOrder()`
- `static nullsFirst() / nullsLast()`
- `static comparing()`
