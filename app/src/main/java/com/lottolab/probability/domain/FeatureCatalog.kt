package com.lottolab.probability.domain

object FeatureCatalog {
    val all = listOf(
        FeatureDefinition(
            id = "daily_combo",
            name = "오늘의 조합",
            category = FeatureCategory.DAILY,
            description = "오늘 하루 고정되는 랜덤, 미출현, 인기번호 제외 조합 3가지를 보여줍니다.",
        ),
        FeatureDefinition(
            id = "today_draw",
            name = "오늘의 추첨번호",
            category = FeatureCategory.DAILY,
            description = "누적확률 체감 기준으로 만든 오늘의 조합을 15초 광고 뒤 하나씩 보여줍니다. 하루 4번까지만 열 수 있습니다.",
        ),
        FeatureDefinition(
            id = "save_number",
            name = "번호 저장 / QR 설정",
            category = FeatureCategory.MY_NUMBERS,
            description = "6개 번호 세트, 묶음 이름, 즐겨찾기, 모바일 슬립지 QR 표시 방식을 관리합니다.",
        ),
        FeatureDefinition(
            id = "match_history",
            name = "적중 기록",
            category = FeatureCategory.MY_NUMBERS,
            description = "3개 이상, 4개 이상, 5개 이상 맞았던 회차와 맞은 숫자를 확인합니다.",
        ),
        FeatureDefinition(
            id = "number_stats",
            name = "번호별 당첨횟수",
            category = FeatureCategory.STATISTICS,
            description = "회차 범위와 보너스 포함 여부를 골라 번호별 당첨횟수를 막대로 봅니다.",
        ),
        FeatureDefinition(
            id = "number_flow",
            name = "출현 흐름 히트맵",
            category = FeatureCategory.STATISTICS,
            description = "번호 공을 눌러 최근 출현 횟수, 오래 안 나온 기간, 상태를 색으로 확인합니다.",
        ),
        FeatureDefinition(
            id = "draw_history",
            name = "회차별 당첨번호",
            category = FeatureCategory.STATISTICS,
            description = "최근 회차부터 역대 당첨번호와 보너스 번호를 색상표로 봅니다.",
        ),
        FeatureDefinition(
            id = "probability",
            name = "누적확률 계산기",
            category = FeatureCategory.PROBABILITY,
            description = "같은 조합, 특정 번호, 여러 장 구매, 가챠 비교를 누적 시행 기준으로 계산합니다.",
        ),
        FeatureDefinition(
            id = "scattered_numbers",
            name = "흩어진 번호",
            category = FeatureCategory.PROBABILITY,
            description = "흩어진 6개 번호를 한 줄로 모아 보고, QR이나 저장 번호로 이어갈 수 있게 돕습니다.",
        ),
        FeatureDefinition(
            id = "digit_matcher",
            name = "앞뒤자리 추첨기",
            category = FeatureCategory.PROBABILITY,
            description = "앞자리 혹은 뒷자리 기준으로 누적확률 체감 조합을 만들어 보는 기능입니다.",
        ),
        FeatureDefinition(
            id = "combo_map",
            name = "로또 조합 지도",
            category = FeatureCategory.MAPS,
            description = "역대 당첨, 저장 번호, 직접 입력 번호의 조합 위치를 지도처럼 보여줍니다.",
        ),
        FeatureDefinition(
            id = "my_analysis",
            name = "내 번호 분석",
            category = FeatureCategory.MY_NUMBERS,
            description = "내 번호의 역대 근접 기록, 번호대 밸런스, 번호대 비교와 템플릿을 한 화면에서 봅니다.",
        ),
        FeatureDefinition(
            id = "cooccurrence",
            name = "번호 궁합",
            category = FeatureCategory.STATISTICS,
            description = "선택한 번호와 과거 회차에서 함께 나온 번호를 보여줍니다.",
        ),
        FeatureDefinition(
            id = "share_card",
            name = "공유 카드",
            category = FeatureCategory.SHARE,
            description = "이번 주 내 저장 번호 결과를 이미지 카드로 만들어 공유합니다.",
        ),
        FeatureDefinition(
            id = "notifications",
            name = "알림 설정",
            category = FeatureCategory.SHARE,
            description = "토요일 19시 번호 확인, 추첨 전날, 추첨 직후 결과와 리포트를 예약합니다.",
        ),
        FeatureDefinition(
            id = "qr",
            name = "모바일 슬립지 QR",
            category = FeatureCategory.SHARE,
            description = "구매 연동이 아닌 앱 내부 확인용 QR로 저장 번호 정보를 확인합니다.",
        ),
        FeatureDefinition(
            id = "privacy_policy",
            name = "개인정보처리방침",
            category = FeatureCategory.SHARE,
            description = "로컬 저장, 알림, 공식 회차 조회, 광고 SDK 사용 내용을 확인합니다.",
        ),
    )
}
