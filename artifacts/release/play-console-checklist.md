# Google Play 비공개 테스트 체크리스트

## 로컬에서 준비된 항목

- 패키지명: `com.lottolab.probability`
- 앱 이름: `로또 누적확률 연구소 (AI)`
- 버전: `versionCode 1`, `versionName 1.0`
- 업로드 산출물: `app/build/outputs/bundle/release/app-release.aab`
- 개인정보처리방침 HTML: `docs/privacy-policy-ko.html`
- Play Console 복사용 문구: `artifacts/release/store-listing-ko.md`
- Data safety 초안: `artifacts/release/data-safety-draft.md`
- 스토어 스크린샷 폴더: `artifacts/release/store-screenshots`

## GitHub Pages 준비

1. 이 저장소를 GitHub에 올립니다.
2. GitHub 저장소 설정에서 Pages를 켭니다.
3. Source는 `Deploy from a branch`, branch는 `main`, folder는 `/docs`로 설정합니다.
4. 공개 URL이 열리는지 확인합니다.
5. Play Console 개인정보처리방침 URL에 아래 형식을 입력합니다.

`https://<github-id>.github.io/<repo>/privacy-policy-ko.html`

## Play Console 앱 생성

1. 새 앱을 만듭니다.
2. 앱 유형은 `앱`, 가격은 `무료`, 기본 언어는 `한국어`로 설정합니다.
3. 배포 국가는 우선 대한민국으로 설정합니다.
4. 스토어 등록정보에 `store-listing-ko.md`의 문구를 입력합니다.
5. 광고 포함 여부는 `예`로 신고합니다.

## 정책/등급 입력

1. 개인정보처리방침 URL을 입력합니다.
2. Data safety는 `data-safety-draft.md` 기준으로 작성합니다.
3. Content rating을 작성합니다.
4. Target audience and content를 작성합니다.
5. 앱이 복권 판매, 현금성 게임, 베팅, 공식 모바일 슬립 연동을 제공하지 않는 참고 도구임을 유지합니다.

## 비공개 테스트 트랙

1. `app-release.aab`를 비공개 테스트 트랙에 업로드합니다.
2. Google 계정 이메일 12개 이상을 테스터로 등록합니다.
3. 테스터에게 opt-in 링크를 보냅니다.
4. 각 테스터가 링크를 눌러 참여 상태가 되었는지 확인합니다.
5. 새 개인 개발자 계정 기준 14일 연속 테스트 기간을 채웁니다.

## 업로드 직전 로컬 검증

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug bundleRelease
```

바탕화면 미리보기 실행:

```powershell
& 'C:\Users\wjdqk\OneDrive\바탕 화면\launch-lotto-preview.cmd'
```
