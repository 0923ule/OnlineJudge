# OnlineJudge : 온라인 코드 채점 서버

이 프로젝트는 온라인으로 제출된 코드를 컴파일하고 실행하여 결과를 반환하는 서버 애플리케이션입니다. Java와 Spring Boot를 사용하여 구현되었습니다.

## 기능

- 코드 제출
  - 사용자는 C, C++, Java 코드를 제출할 수 있습니다.
  - 제출된 코드는 데이터베이스(MySQL)에 저장됩니다.
  
- 컴파일 및 실행
  - 제출된 코드는 파일로 저장되고, 절대 경로를 사용하여 컴파일 및 실행됩니다.
  - 컴파일 및 실행 과정에서 발생하는 오류를 처리하고 결과를 반환합니다.

- 채점
  - 문제 ID별로 디렉토리를 생성하여 코드 파일 및 입력 파일을 관리합니다.
  - 입력 데이터를 파일로 저장하고 실행 결과를 검증합니다.

## 요구 사항

- Java 17
- Spring Boot 3.3.1
- MySQL 데이터베이스

## 설정

1. MySQL 데이터베이스 설정

```sql
CREATE DATABASE online_judge;
```

2. application.yml 파일 설정

```
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/online_judge
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

## API 사용법

코드 제출
- URL: /api/submit
- 메서드: POST
- 헤더: Content-Type: application/json
- 요청 바디:
```
 {
  "language": "cpp",
  "filename": "AdditionSolution.cpp",
  "code": "#include <iostream>\nusing namespace std;\n\nint main() {\n    int a, b;\n    cin >> a >> b;\n    cout << (a + b) << endl;\n    return 0;\n}",
  "problemId": 1
}
```
- 응답:
```
 [
  "Input: 2 3\nOutput: 5\nExpected: 5\nResult: Correct",
  "Input: 10 20\nOutput: 30\nExpected: 30\nResult: Correct",
  "Input: 1 1\nOutput: 2\nExpected: 2\nResult: Correct"
]
```





