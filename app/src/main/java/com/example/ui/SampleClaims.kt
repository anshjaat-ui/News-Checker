package com.example.ui

data class SampleClaim(
    val title: String,
    val text: String,
    val category: String
)

object SampleClaimsProvider {
    val samples = listOf(
        SampleClaim(
            title = "UNESCO Best Anthem",
            text = "Breaking News! UNESCO declares Indian National Anthem Jana Gana Mana as the best national anthem in the world! Proud moment for all Indians! Please forward to all.",
            category = "Hoax"
        ),
        SampleClaim(
            title = "Tata Free Recharge",
            text = "Tata group ki 150th anniversary par sabhi mobile users ko 3 mahine ka free recharge mil raha hai. Neeche diye link par click karein aur apna recharge claim karein!",
            category = "Scam"
        ),
        SampleClaim(
            title = "NASA Diwali Night Photo",
            text = "NASA releases stunning photo of India on Diwali night! See how beautifully the country is illuminated from outer space.",
            category = "Misleading"
        ),
        SampleClaim(
            title = "Govt 5000 Allowance",
            text = "Sarkaar de rahi hai sabhi betiyon ko 5000 rupaye har mahine PM Kanya Yojna ke tehat. Form bharne ke liye yahan click karein.",
            category = "Scam"
        ),
        SampleClaim(
            title = "5G Tower Health Hazard",
            text = "5G network testing is causing illness and harmful radiation to people and birds. Stop 5G towers immediately!",
            category = "Rumor"
        )
    )
}
