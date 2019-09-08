# 실습을 위한 개발 환경 세팅
* https://github.com/slipp/web-application-server 프로젝트를 자신의 계정으로 Fork한다. Github 우측 상단의 Fork 버튼을 클릭하면 자신의 계정으로 Fork된다.
* Fork한 프로젝트를 eclipse 또는 터미널에서 clone 한다.
* Fork한 프로젝트를 eclipse로 import한 후에 Maven 빌드 도구를 활용해 eclipse 프로젝트로 변환한다.(mvn eclipse:clean eclipse:eclipse)
* 빌드가 성공하면 반드시 refresh(fn + f5)를 실행해야 한다.

# 웹 서버 시작 및 테스트
* webserver.WebServer 는 사용자의 요청을 받아 RequestHandler에 작업을 위임하는 클래스이다.
* 사용자 요청에 대한 모든 처리는 RequestHandler 클래스의 run() 메서드가 담당한다.
* WebServer를 실행한 후 브라우저에서 http://localhost:8080으로 접속해 "Hello World" 메시지가 출력되는지 확인한다.

# 각 요구사항별 학습 내용 정리
* 구현 단계에서는 각 요구사항을 구현하는데 집중한다. 
* 구현을 완료한 후 구현 과정에서 새롭게 알게된 내용, 궁금한 내용을 기록한다.
* 각 요구사항을 구현하는 것이 중요한 것이 아니라 구현 과정을 통해 학습한 내용을 인식하는 것이 배움에 중요하다. 

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답
* 클라이어트(브라우져) 에서 HTTP를 통해 요청을 inputstream 데이터를 읽어와 파싱해서 요청에 대한 처리를 해야 한다.
* html 파일을 요청 했을때 서버 디렉토리에서 해당 html 파일을 찾아 읽어와서 outputstream 으로 write 하여 렌더링 한다.

### 요구사항 2 - get 방식으로 회원가입
* N/A

### 요구사항 3 - post 방식으로 회원가입
* HTTP header가 끝난후 한줄 띄고 부터 HTTP body 값이다 (이떄 header에 포함된 Content-Length를 통해 body의 끝을 알 수 있다.)

### 요구사항 4 - redirect 방식으로 이동
* HTTP status code - 200 정상응답 
* HTTP status code - 302 는 정상응답 하여 redirect 해준다 이떄 Response header에 redirect할 url 주소를 location에 넣어 주면 브라우져 에서 새로운 URL로 요청을 한다.

### 요구사항 5 - cookie
* 

### 요구사항 6 - stylesheet 적용
* 

### heroku 서버에 배포 후
* 