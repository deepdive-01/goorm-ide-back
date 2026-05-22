---
name: feedback-commit
description: 커밋은 사용자가 직접 한다. Claude가 git add / git commit 하지 않는다.
metadata:
  type: feedback
---

커밋은 사용자가 직접 처리한다. Claude는 코드 작업 완료 후 커밋 명령을 실행하지 않는다.

**Why:** 사용자가 직접 커밋하기를 원함.

**How to apply:** 코드 작업이 끝나면 변경된 파일 목록만 알려주고, git add / git commit은 실행하지 않는다.
