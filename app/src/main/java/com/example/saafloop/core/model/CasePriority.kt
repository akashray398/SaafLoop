package com.example.saafloop.core.model

enum class CasePriority(val label: String, val weight: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    CRITICAL("Critical", 4)
}

enum class IssueSeverity(val label: String, val description: String) {
    MINOR("Minor", "Small scattered litter or single item"),
    MODERATE("Moderate", "Medium accumulation or full bin"),
    SEVERE("Severe", "Large dumping pile obstructing pathway"),
    CRITICAL("Critical", "Hazardous, toxic, or emergency dumping site")
}

enum class EstimatedImpact(val label: String) {
    LOW("Low Impact"),
    MODERATE("Moderate Impact"),
    HIGH("High Impact"),
    COMMUNITY_WIDE("Community Wide Impact")
}
