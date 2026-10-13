package com.example.saafloop.feature.report.model

import com.example.saafloop.R

enum class WasteCategory(
    val titleRes: Int,
    val description: String
) {
    MIXED_GARBAGE(
        titleRes = R.string.report_category_mixed,
        description = "Household, organic, or unseparated general waste"
    ),
    PLASTIC_DRY(
        titleRes = R.string.report_category_plastic,
        description = "Bottles, packaging, cardboard, and dry recyclables"
    ),
    OVERFLOWING_BIN(
        titleRes = R.string.report_category_overflow,
        description = "Public dumpsters or municipal waste bins spilling over"
    ),
    CONSTRUCTION_DEBRIS(
        titleRes = R.string.report_category_debris,
        description = "Concrete, bricks, plaster, or renovation rubble"
    ),
    BLOCKED_DRAIN(
        titleRes = R.string.report_category_drain,
        description = "Waterlogging, choked stormwater drains, or canal blockage"
    ),
    SMOKE_AIR_POLLUTION(
        titleRes = R.string.report_category_smoke,
        description = "Open garbage burning, chemical fumes, or dense smoke"
    ),
    NOISE_POLLUTION(
        titleRes = R.string.report_category_noise,
        description = "Unlawful industrial or public loudspeaker noise disturbance"
    ),
    DAMAGED_TREES(
        titleRes = R.string.report_category_trees,
        description = "Fallen or hazardous trees, neglected parks, or damaged green belts"
    ),
    WATER_LEAK_POLLUTION(
        titleRes = R.string.report_category_water,
        description = "Leaking public water pipes, sewage overflow, or water contamination"
    ),
    OTHER_UNSURE(
        titleRes = R.string.report_category_other,
        description = "Unidentified or complex environmental observation"
    )
}

enum class WasteSizeEstimate(
    val labelRes: Int,
    val detail: String
) {
    SMALL(
        labelRes = R.string.report_size_small,
        detail = "Fits in single bag or small box"
    ),
    MEDIUM(
        labelRes = R.string.report_size_medium,
        detail = "Multiple bags or handcart volume"
    ),
    LARGE(
        labelRes = R.string.report_size_large,
        detail = "Large dump pile requiring heavy team cleanup"
    )
}
