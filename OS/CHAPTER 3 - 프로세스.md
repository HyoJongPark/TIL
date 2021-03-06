# CHAPTER 3 - 프로세스

초기의 컴퓨터 시스템은 한 번에 하나의 프로그램만을 실행하도록 허용하였지만, 오늘날은 다수의 프로그램이 메모리에 적재되어 병행 실행 되는 것을 허용한다. 이런 발전은 프로그램을 보다 견고하게 제어하고 보다 구획화 할 것을 필요로 했고, 이 필요성이 프로세스의 개념을 낳았다.

프로세스란 실행 중인 프로그램을 말한다.

## 1. 프로세스 개념_Process Concept

### 1.1 프로세스_The Process

비공식적으로, 프로세스란 실행 중인 프로그램이다. 프로세스의 현재 활동의 상태는 프로그램 카운터(PC) 값과 프로세서 레지스터의 내용으로 나타낸다.

<img width="216" alt="Untitled" src="https://user-images.githubusercontent.com/75190035/173726992-6106c730-0265-4f50-ae9e-32cb5abb87c2.png">

- 프로세스의 메모리 배치는 일반적으로 여러 섹션으로 구분된다.
    - 텍스트 섹션 - 실행 코드
    - 데이터 섹션 - 전역 변수
    - 힙 섹션 - 프로그램 실행 중에 동적으로 할당되는 메모리
    - 스택 섹션 - 함수를 호출할 때 임시 데이터 저장장소(예: 함수 매개변수, 복귀 주소 및 지역 변수)
    - 각 섹션 별 특징
        - 텍스트 및 데이터 섹션의 크기는 고정되기 때문에 프로그램 실행 시간 동안 크기가 변하지 않는다.
        - 스택 및 힙 섹션은 프로그램 실행 중에 동적으로 줄어들거나 커질 수 있다.
            - 함수가 호출될 때마다 함수 매개변수, 지역 변수 및 복귀 주소를 포함하는 활성화 레코드가 스택, 힙에 푸시(`push`)된다.
            - 함수에서 제어가 되돌아오면 스택, 힙에서 활성화 레코드가 팝(`pop`)된다.
            - 스택, 힙 섹션이 서로의 방향으로 커지더라도 운영체제는 서로 겹치지 않도록 해야 한다.
- 프로그램 그 자체는 프로세스가 아니다.
    - **프로그램**은 명령어 리스트를 내용으로 가진 디스크에 저장된 파일과 같은 **수동적인 존재**
    - **프로세스**는 다음에 실행할 명령어를 지정하는 프로그램 카운터와 관련 자원의 집합을 가진 **능동적인 존재**
    - 실행 파일이 메모리에 적재될 때(아이콘 클릭 등) 프로그램은 프로세스가 된다.

### 1.2 프로세스 상태_Process State

프로세스는 실행 되면서 그 상태가 변한다. 프로세스의 상태는 부분적으로 그 프로세스의 현재의 활동에 따라서 정의된다. 

- 프로세스는 다음 상태 중 하나에 있게 된다.
    - 새로운(new): 프로세스가 생성 중이다.
    - 실행(running): 명령어들이 실행되고 있다.
    - 대기(waiting): 프로세스가 어떤 이벤트가 일어나기를 기다린다.
    - 준비(ready): 프로세스가 처리기에 할당되기를 기다린다.
    - 종료(terminated): 프로세스의 실행이 종료되었다.
- 이들의 이름은 임의적이지만, 이들이 나타내는 상태는 모든 시스템에서 찾아볼 수 있다.
- 어느 한순간에 한 처리기 코어에서는 오직 하나의 프로세스만이 실행된다.

### 1.3 프로세스 제어 블록_Process Control Block

각 프로세스는 운영체제에서 프로세스 제어 블록(process Control block, PCB)에 의해 표현된다.

<img width="133" alt="Untitled 1" src="https://user-images.githubusercontent.com/75190035/173727011-6dab6e11-124e-409b-81b7-34f70a33733d.png">

- 프로세스 제어 블록은 특정 프로세스와 연관된 여러 정보를 수록하며, 다음을 포함한다.
    - 프로세스 상태
        - 상태는 new, ready, running, wating, halted 등이다.
    - 프로그램 카운터
        - 해당 프로세스가 다음에 실행할 명령어의 주소를 가리킨다.
    - CPU 레지스터(들)
        - 컴퓨터 구조에 따라 다양한 수, 유형의 레지스터를 가진다.
            - 레지스터에는 누산기, 인덱스 레지스터, 스택 레지스터, 범용 레지스터들과 상태 코드 정보가 포함된다.
        - 프로세스가 다시 스케줄 될 때 계속 올바르게 실행되도록 하기 위해서 인터럽트 발생 시 저장되어야 한다.
    - CPU-스케줄링 정보
        - 프로세스 우선순위, 스케줄 큐에 대한 포인터와 다른 스케줄 매개변수를 포함한다.
    - 메모리 관리 정보
        - 운영 체제에 의해 사용되는 메모리 시스템에 따라 기준 레지스터와 한계 레지스터의 값, 페이지 테이블, 세그먼트 테이블 등과 같은 정보가 포함된다.
    - 회계 정보
        - CPU 사용 시간, 경과된 실시간, 시간 제한, 계정 번호, 잡 또는 프로세스 번호 등을 포함한다.
    - 입출력 상태 정보
        - 프로세스에 할당된 입출력 장치들과 열린 파일의 목록 등을 포함한다.

### 1.4 스레드_Threads

이전의 프로세스 모델은 프로세스가 단일 실행 스레드를 실행하는 프로그램이라 가정한다.

대부부느이 현대 운영체제는 프로세스 개념을 확장하여 한 프로세스가 다수의 실행 스레드를 가질 수 있도록 허용한다. 따라서 프로세스가 한 번에 하나 이상의 일을 수행할 수 있도록 허용한다.

---

## 2. 프로세스 스케줄링_Process Scheduling

다중 프로그래밍의 목적은 CPU 이용을 최대화 하기 위해 항상 어떤 프로세스가 실행되도록 하는 데 있다.

시분할의 목적은 각 프로그램이 실행되는 동안 사용자가 상호 작용할 수 있도록 프로세스들 사이에서 CPU 코어를 빈번하게 교체하는 것이다.

이 목적을 달성하기 위해 **프로세스 스케줄러**는 코어에서 실행 가능한 여러 프로세스 중에서 하나의 프로세스를 선택하며, 현재 메모리에 있는 프로세스 수를 다중 **프로그래밍 정도(degree of multiprogramming)**라고 한다.

### 2.1 스케줄링 큐_Scheduling Queue

![Untitled 2](https://user-images.githubusercontent.com/75190035/173727024-26fe9206-9d93-4122-a4fa-f688aa829fce.png)

- 잡 큐(job queue): 보조기억장치(하드디스크)에 있는 프로세스가 메모리로 올라가기 위해 대기하는 곳이다.
- 준비 큐(ready queue): 프로세스가 시스템에 들어가면 준비 큐에 들어가서 준비 상태가 되어 CPU 코어에서 실행되기를 기다린다.
    - 준비 큐(ready queue)는 일반적으로 연결 리스트로 저장된다.
- 대기 큐(waiting queue): 장치는 프로세서보다 매우 느리게 실행되므로 프로세스는 I/O가 사용 가능할 때까지 기다려야 한다. I/O 완료와 같은 특정 이벤트가 발생하기를 기다리는 프로세스는 대기 큐(waiting queue)에 삽입된다.
- 각 큐 내부에 저장된 실제 데이터는 각 프로세스의 **PCB**가 저장되어 있다. 그리고 이러한 순서를 기다리는 공간이 있다면 이 순서를 정해주는 알고리즘이 있어야 한다. 이러한 알고리즘을 **스케줄링(Scheduling)**이라 한다.

### 2.2 CPU 스케줄링_CPU Scheduling

- CPU 스케줄러의 역할은 준비 큐에 있는 프로세스 중 선택된 하나의 프로세스에 CPU 코어를 할당하는 것이다.
- 여기서 프로세스는 크게 두 가지로 나뉜다.
    - I/O 바운드 프로세스: 해당 프로세스에서 I/O 작업이 차지하는 비중이 높은 프로세스
    - CPU 바운드 프로세스: 해당 프로세스에서 CPU 작업(계산)이 차지하는 비중이 높은 프로세스
- CPU 스케줄링 방식은 크게 두 가지로 나뉜다.
    - 선점:   기존 프로세스를 강제로 종료하고 스케줄이 발생할 수 있음
    - 비선점: 기존 프로세스가 끝난 후 CPU 스케줄링 진행
- 스와핑
    - **Medium-term scheduler**로써 말 그대로 short-term 보다는 덜 발생하지만, long-term 보다는 자주 발생하는 스케줄러다.
    - 메인 메모리에서 장기간 사용하지 않는 프로세스를 하드디스크로 옮겨주고(**swap out**), 이 프로세스가 다시 사용될 때 다시 메인 메모리에 할당 해준다(**swap in**).
    - 스와핑은 일반적으로 메모리가 초과 사용되어 가용공간을 확보해야 할 때만 필요하다.

### 2.3 문맥 교환_Context Switch

<img width="444" alt="Untitled 3" src="https://user-images.githubusercontent.com/75190035/173727034-3256313b-0624-40d3-91f7-d80a7fca18de.png">

- 인터럽트가 발생하면 시스템은 인터럽트 처리가 끝난 후에 문맥을 복구할 수 있도록 현재 실행 중인 프로세스의 현재 문맥을 저장해야 하며, 이 문맥은 PCB에 저장된다.
- 문맥은 CPU 레지스터의 값, 프로세스 상태, 메모리 관리 정보 등을 포함한다.
- CPU 코어를 다른 프로세스로 교환 하려면 이전의 프로세스의 상태를 보관하고 새로운 프로세스의 보관된 상태를 복구하는 작업이 필요하다. 이 작업을 문맥 교환(Context switch)라고 한다.

---

## 3. 프로세스에 대한 연산_Operation on Processes

대부분의 시스템 내의 프로세스들은 병행 실행될 수 있으며, 반드시 동적으로 생성되고, 제거되어야 한다. 운영체제는 이를 위해 프로세스 생성, 종료를 위한 기법을 제공해야 한다.

### 3.1 프로세스 생성_Process Creation

<img width="494" alt="Untitled 4" src="https://user-images.githubusercontent.com/75190035/173727051-fbdaed11-881b-4593-ac92-dd47baa08be0.png">

- 실행 중인 프로세스는 여러 개의 새로운 프로세스를 생성할 수 있다.
    - 생성하는 프로세스는 부모 프로세스, 생성된 프로세스는 자식 프로세스라고 부르고, 이 프로세스들은 프로세스의 트리를 형성한다.
- 현대 운영체제들은 유일한 프로세스 식별자(pid)를 사용해 프로세스를 구분하며, 식별자는 보통 정수다.
- pid가 1인 프로세스가 모든 사용자 프로세스의 부모 프로세스 역할을 수행되고, 이 프로세스는 시스템이 부팅 될 때 생성된다.
- 프로세스가 새로운 프로세스를 생성할 때, 두 프로세스를 실행시키는 두 가지 방법이 있다.
    - 부모는 자식과 병행하게 실행을 계속한다.
    - 부모는 일부 또는 모든 자식이 실행을 종료될 때까지 기다린다.
- 새로운 프로세스들의 주소 공간 측면에서 볼 때 두 가지 가능성이 있다.
    - 자식 프로세스는 부모 프로세스의 복사본이다.(자식이 부모와 똑같은 데이터를 가진다.)
    - 자식 프로세스가 자신에게 적재될 새 프로그램을 가지고 있다.

### 3.2 프로세스 종료_Process Termination

- 프로세스가 마지막 문장의 실행을 끝내고, `exit` 시스템 콜을 사용해 운영체제에 자신의 삭제를 요청하면 종료된다. 이 때, 자신을 기다리고 있는 부모 프로세스에게 상태 값을 반환할 수 있다.
- 한 프로세스는 적당한 시스템 콜을 통해서 다른 프로세스의 종료를 유발할 수 있다. 보통, 종료될 프로세스의 부모만이 해당 시스템 콜을 호출할 수 있다.
    - 부모가 자식을 종료시키기 위해서는 자식의 pid를 알아야 한다. 그러므로 새 프로세스를 만들 때 신원(identity)이 부모에게 전달된다.
- 부모가 자식의 실행을 종료하는 경우
    - 자식이 자신에게 할당된 자원을 초과해 사용할 때(이 때 부모가 자식의 상태를 검사할 수 있어야 한다.)
    - 자식에게 할당된 태스크가 더 이상 필요 없을 때
    - 부모가 exit 하는 데, 운영체제에서 부모가 exit한 후 자식이 실행하는 것을 허용하지 않을 때(이 경우 보통 부모 프로세스가 종료될 때 자식도 같이 종료되는 연쇄식 종료 작업이 시행된다.)
- **좀비, 고아 프로세스**
    - 프로세스가 종료되면 사용하던 자원은 운영체제가 되찾아 간다.
    - 그러나 프로세스의 종료 상태가 저장되는 프로세스 테이블의 항목은 부모 프로세스가 `wait()` 를 호출할 때까지 남아 있게 된다.
    - 부모가 `wait()` 를 호출하지 않은 프로세스를 좀비 프로세스라 한다.
    - 부모가 `wait()` 호출 대신, 종료한다면 자식 프로세스는 고아 프로세스가 된다.

---

## 4. 프로세스 간 통신_Interporcess Communication

실행 중인 다른 프로세스들과 데이터를 공유하지 않는 프로세스는 독립적이다. 프로세스가 시스템에서 실행 중인 다른 프로세스들에 영향을 주거나 받는다면 이는 협력적인 프로세스들이다.

프로세스 협력을 허용하는 환경을 제공하는 데는 몇 가지 이유가 있다.

- 정보 공유(information sharing)
- 계산 가속화(computation speedup)
- 모듈성(modularity)

협력적인 프로세스들은 데이터를 교환할 수 있는, 즉 서로 데이터를 공유할 프로세스간 통신(IPC) 기법이 필요하다. 기본적으로 **공유 메모리(shared memory)와 메시지 전달(message passing)의 두 가지 모델이 있다.**

<img width="413" alt="Untitled 5" src="https://user-images.githubusercontent.com/75190035/173727064-2daf4a3c-178d-4ab7-92e6-67d0b03e2f5b.png">

- 공유 메모리
    - 공유 메모리 모델에서는 협력 프로세스들에 의해 공유되는 메모리의 영역이 구축된다.
    - 프로세스들은 해당 영역에 데이터를 읽고 쓰고 함으로써 정보를 교환할 수 있다.
- 메시지 전달
    - 메시지 전달 모델에서는 협력 프로세스들 사이에 교환되는 메시지를 통해 교환이 이루어 진다.

---

## 5. 공유 메모리 시스템에서의 프로세스 간 통신_IPC in Shared-Memory Systems

- 공유 메모리를 사용하는 프로세스 간 통신에서는 프로세스들이 공유 메모리 영역을 구축해야 한다. 통상 공유 메모리 영역은 공유 메모리 세그먼트를 생성하는 프로세스의 주소 공간에 위치한다.
- 일반적으로 운영체제는 한 프로세스가 다른 프로세스의 메모리에 접근하는 것을 금지하지만, 이 방식에서는 둘 이상의 프로세스가 이 제약 조건을 제거하는 것에 동의하는 것을 필요로 한다.
    - 프로세스들은 동시에 동일한 위치에 쓰지 않도록 책임져야 한다.
- **생산자-소비자 문제의 해결책**
    - 생산자 프로세스는 정보를 생산, 소비자 프로세스는 정보를 소비한다.
    - 생산자와 소비자 프로세스들이 병행으로 실행되도록 하려면, 생산자가 정보를 채워 넣고 소비자가 소모할 수 있는 항목들의 버퍼가 반드시 사용 가능해야 한다. 이 버퍼는 공유 메모리 영역에 존재해야 한다.
    - 이 때 두 가지 버퍼가 사용되는데, 무한 버퍼는 크기에 실질적인 한계가 없고 유한 버퍼는 크기가 고정되어있다고 가정한다.

---

## 6. 메시지 전달 시스템에서의 프로세스 간 통신_IPC in Message-Passing Systems

메시지 전달 방식은 동일한 주소 공간을 공유하지 않고 프로세스들이 통신하고, 동작을 동기화 할 수 있도록 허용하는 기법을 제공한다.

메시지 전달 방식은 통신하는 프로세스들이 네트워크에 의해 연결된 다른 컴퓨터들에 존재할 수 있는 분산 환경에서 특히 유용하다.

메시지 전달 방식은 최소한 두 가지 연산을 제공한다. 

- `send(message)`
- `receive(message)`

### 6.1 명명_Naming

- 통신을 원하는 프로세스들은 서로를 가리킬 방법이 있어야 한다. 이들은 직/간접 통신을 사용할 수 있다.
- **직접 통신(대칭)**
    - 직접 통신하에서, 통신을 원하는 각 프로세스는 통신의 수신자 또는 송신자의 이름을 명시해야 한다.
    - `send(P, message)` : 프로세스 P에 메시지를 전송한다.
    - `receive(Q, message)` : 프로세스 Q로부터 메시지를 수신한다.
    - 특징
        - 통신을 원하는 각 프로세스의 쌍들 사이에 연경이 자동으로 구축된다. 통신을 위해 프로세스들은 상대의 신원(identity)만 알면 된다.
        - 연결은 정확히 두 프로세스 사이에만 연관된다.
        - 통신하는 프로세스들의 각 쌍 사이에는 정확하게 하나의 연결이 존재해야 한다.
- **직접 통신(비대칭)**
    - 직접 통신의 변형으로 주소 지정 시에 비대칭을 사용할 수도 있다.
    - 송신자만 수신자의 이름을 지명하며, 수신자는 송신자의 이름을 제시 할 필요가 없다.
    - `send(P, message)` : 프로세스 P에 메시지를 전송한다.
    - `receive(id, message)` : 임의의 프로세스로부터 메시지를 수신한다. 변수 id는 통신을 발생시킨 이름으로 설정된다.
    - 두 직접 통신 방식은 모두 프로세스를 지정하는 방식 때문에 모듈성을 제한한다는 것이 단점이다.
- **간접 통신**
    - 간접 통신에서 메시지들은 메일박스(mailbox) 또는 포트(port)로 송신/수신 된다.
    - 메일박스는 추상적으로 프로세스들에 의해 메시지들이 넣어지고, 메시지들이 제거될 수 있는 객체라고도 볼 수 있다. 또한 각 메일박스는 고유 id를 가진다.
    - `send(A, message)` : 메시지를 메일박스 A로 송신한다.
    - `receive(A, message)` : 메시지를 메일박스 A로부터 수신한다.
    - 특징
        - 한 쌍의 프로세스들 사이의 연결은 이들 프로세스가 공유 메일박스를 가질 때만 구축된다.
        - 연결은 두 개 이상의 프로세스들과 연관될 수 있다.
        - 통신하고 있는 각 프로세스 사이에는 다수의 서로 다른 연결이 존재할 수 있고, 각 연결은 하나의 메일 박스에 대응된다.

### 6.2 동기화_Synchronization

- 메시지 전달은 봉쇄형(blocking, 동기식) 혹은 비봉쇄형(nonblocking, 비동기식) 방식으로 전달된다.
- 봉쇄형 보내기: 송신하는 프로세스는 메시지가 수신 프로세스 또는 메일박스에 의해 수신될 때까지 봉쇄된다.
- 비 봉쇄형 보내기: 송신하는 프로세스가 메시지를 보내고 작업을 재시작한다.
- 봉쇄형 받기: 메시지가 이용 가능할 때까지 수신 프로세스가 봉쇄된다.
- 비 봉쇄형 받기: 송신하는 프로세스가 유효한 메시지 또는 null을 받는다.

### 6.3 버퍼링_Buffering

- 통신이 직/간접적이든 간에 통신하는 프로세스들에 의해 교환되는 메시지는 임시 큐에 들어있다.
- 큐 구현 방식
    - 무용량(zero capacity)
        - 큐의 최대 길이가 0
        - 이 경우에 송신자는 수신자가 메시지를 수신할 때까지 기다려야 한다.
    - 유한 용량(bounded capacity)
        - 큐의 길이 n(유한)
        - 새 메시지가 전달될 때 큐가 만원이 아니라면, 메시지는 큐에 놓이며 송신자는 대기하지 않고 실행을 계속한다.
        - 새 메시지가 전달될 때 큐가 만원이라면, 송신자는 큐 안에 공간이 이용 가능할 때까지 반드시 봉쇄되어야 한다.
    - 무한 용량(unbounded capacity)
        - 큐는 잠재적으로 무한한 길이를 가진다.
        - 메시지들은 얼마든지 큐 안에서 대기할 수 있다.
        - 송신자는 절대 봉쇄되지 않는다.

---

## 3.7 클라이언트 서버 환경에서 통신_Communication in Client-Sever Systems

프로세스 간 통신 방법들은 클라이언트-서버 시스템의 통신에도 사용할 수 있다.

이 절에서는 클라이언트-서버에서 사용할 수 있는 다른 통신 전략에 대해 설명한다.

### 7.1 소켓_Socket

- 소켓은 통신의 극점을 뜻한다.
- 두 프로세스가 네트워크 상에서 통신을 하려면 양 프로세스마다 하나씩, 총 두 개의 소켓이 필요해진다.
- 각 소켓은 IP 주소와 포트 번호 두 가지를 접합해서 구별한다.
- 일반적으로 소켓은 클라이언트-서버 구조를 사용한다.
- 서버는 지정된 포트에 클라이언트 요청 메시지가 도착하기를 기다리며, 요청이 수신되면 서버는 클라이언트 소켓으로 부터 연결 요청을 수락함으로써 연결이 완성된다.

### 7.2 원격 프로시저 호출_Remote Procedure Calls, RPC

- 원격 서비스와 관련된 가장 보편적인 형태 중 하나는 RPC 패러다임으로, 네트워크에 연결된 두 시스템 사이의 통신에 사용하기 위해 프로시저 호출 기법을 추상화하는 방법으로 설계되었다.
- IPC와 많은 측면에서 유사하지만, 여기서는 프로세스들이 서로 다른 시스템 위에서 동작하기 때문에 원격 서비스를 제공하기 위해서는 메시지 기반 통신을 해야 한다.
- IPC 방식과 달리 RPC 통신에서 전달되는 메시지는 구조화되어 있고, 따라서 데이터의 패킷 수준을 넘어선다.
- RPC는 분산 파일 시스템을 구현하는데 유용하다.

---

> 참고
> 
> - 책
> 
> [⌜Operating System Concepts 10th - Abraham Silberschatz, Peter B. Galvin, Greg Gagne⌟](http://www.yes24.com/Product/Goods/78225791)
> 
> - 블로그
> 
> [https://blog.naver.com/bisu1532/221612466313](https://blog.naver.com/bisu1532/221614412152)
> 
> [https://suhwanc.tistory.com/176?category=879656](https://suhwanc.tistory.com/177?category=879656)
> 
> [https://velog.io/@codemcd/운영체제OS-5.-프로세스-관리](https://velog.io/@codemcd/%EC%9A%B4%EC%98%81%EC%B2%B4%EC%A0%9COS-5.-%ED%94%84%EB%A1%9C%EC%84%B8%EC%8A%A4-%EA%B4%80%EB%A6%AC)
>
