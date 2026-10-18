package com.example.data.model

data class AspectRatioOption(
    val label: String,
    val value: String,
    val iconName: String
)

val SupportedAspectRatios = listOf(
    AspectRatioOption("1:1", "1:1", "Square"),
    AspectRatioOption("16:9", "16:9", "Landscape"),
    AspectRatioOption("9:16", "9:16", "Portrait / Story"),
    AspectRatioOption("4:3", "4:3", "Classic"),
    AspectRatioOption("3:4", "3:4", "Tall Classic"),
    AspectRatioOption("3:2", "3:2", "Photo 3:2"),
    AspectRatioOption("2:3", "2:3", "Poster 2:3"),
    AspectRatioOption("21:9", "21:9", "Cinematic Ultrawide")
)

val SupportedImageSizes = listOf("1K", "2K", "4K")

data class GeneratedArtAsset(
    val id: String,
    val prompt: String,
    val type: String, // "IMAGE", "VIDEO", "MUSIC"
    val model: String,
    val mediaUrl: String,
    val aspectRatio: String = "1:1",
    val resolution: String = "2K",
    val durationSeconds: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)
