# NBE6-8-2-Team06(Bookers)

## 프로젝트 소개
프로그래머스 백엔드 데브코스에서 2차 프로젝트로 진행한 내 책장 관리 서비스입니다.

## 협업규칙
1. 작업은 main 브랜치가 아닌 작업용 브랜치를 따로 만들어서 진행해주세요.
2. 작업 진행중 커밋을 진행시, 아래의 Commit Message Convention을 참고하여 진행해주세요.
3. 작업에서 하나의 커밋 진행 후, `git pull origin main --rebase`를 통해 작업 브랜치의 최신화를 유지해주세요.
4. 작업이 끝나면 해당 작업을 브랜치에 push 후 main branch와 Squash merge 해주세요.
5. merge 후에는 브런치를 삭제해주시고, `git fetch --prune`을 통해 로컬에 남아있는 원격 레포지토리를 정리해주세요.

## Commit Message Convention
### 커밋 메시지 양식(예시)
```
Feat: "로그인 함수 추가" -> 제목

로그인 요청을 위한 함수 구현 -> 본문
```
### 제목 작성 두 가지 방식
```
둘 중 본인이 편한 방식으로 진행해주시면 됩니다.
1. Feat: "로그인 함수 추가"
2. be-feat-1 : "작업내용" -> 강사님 추천 방식
```

### Commit Type
- Feat : 새로운 기능 추가
- Fix : 버그 수정
- Env : 개발 환경 관련 설정
- Style : 코드 스타일 수정 (세미 콜론, 인덴트 등의 스타일적인 부분만)
- Refactor : 코드 리팩토링 (더 효율적인 코드로 변경 등)
- Design : CSS 등 디자인 추가/수정
- Comment : 주석 추가/수정
- Docs : 내부 문서 추가/수정
- Test : 테스트 추가/수정
- Chore : 빌드 관련 코드 수정
- Rename : 파일 및 폴더명 수정
- Remove : 파일 삭제

Commit Message Convention의 자세한 설명은 아래 링크를 참고바랍니다. <br/>
https://projectlog.tistory.com/57
