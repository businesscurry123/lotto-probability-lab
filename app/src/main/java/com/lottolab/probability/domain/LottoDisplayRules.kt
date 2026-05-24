package com.lottolab.probability.domain

object LottoDisplayRules {
    fun mainNumberSets(numberSets: List<SavedNumberSet>): List<SavedNumberSet> =
        numberSets.filter(SavedNumberSet::favorite)
}

object SlipQrPlanner {
    fun pages(numberSets: List<SavedNumberSet>, mode: SlipQrDisplayMode): List<SlipQrPage> {
        if (numberSets.isEmpty()) return emptyList()
        val pageSize = when (mode) {
            SlipQrDisplayMode.GROUP_BY_FIVE -> 5
            SlipQrDisplayMode.SINGLE -> 1
        }
        return numberSets
            .chunked(pageSize)
            .mapIndexed { index, sets -> SlipQrPage(pageIndex = index + 1, sets = sets) }
    }

    fun qrContent(page: SlipQrPage): String {
        val body = page.sets.joinToString("|") { numberSet ->
            "${numberSet.name}:${numberSet.numbers.sorted().joinToString(",")}"
        }
        return "lotto-lab://slip-qr?page=${page.pageIndex}&sets=$body"
    }
}
