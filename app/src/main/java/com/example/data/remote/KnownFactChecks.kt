package com.example.data.remote

import com.example.data.model.FactCheckResult
import com.example.data.model.FactCheckVerdict

object KnownFactChecks {
    val sampleList: List<FactCheckResult> = listOf(
        FactCheckResult(
            id = 1,
            extractedClaim = "UNESCO ne Indian National Anthem (Jana Gana Mana) ko duniya ka best anthem declare kiya hai.",
            verdict = FactCheckVerdict.FAKE,
            confidence = "High",
            explanation = "UNESCO ne aisa koi declaration ya award nahi diya hai. Yeh WhatsApp forward saalon se ghum raha hai aur UNESCO ne khud confirm kiya hai ki wo national anthems ki koi ranking nahi karte.",
            sources = listOf(
                "Alt News: UNESCO has not declared Jana Gana Mana as best anthem",
                "PIB Fact Check: Old viral message debunked"
            ),
            userAdvice = "Yeh ek purana viral hoax hai, kripya aage kisi bhi WhatsApp group mein forward na karein.",
            originalInput = "Breaking News! UNESCO declares Indian National Anthem Jana Gana Mana as the best national anthem in the world! Proud moment for all Indians! Please forward to all."
        ),
        FactCheckResult(
            id = 2,
            extractedClaim = "Tata / Jio company apni anniversary par sabhi users ko 3 mahine ka free recharge de rahi hai diye gaye link par click karne par.",
            verdict = FactCheckVerdict.FAKE,
            confidence = "High",
            explanation = "Yeh ek phishing scam link hai. Na to Tata aur na hi Jio aisi koi free recharge scheme chala rahe hain. Is link par click karne se personal data ya bank details chori ho sakti hain.",
            sources = listOf(
                "PIB Fact Check: Viral free recharge links are fraudulent",
                "Telecom Regulatory Authority of India (TRAI) Advisory"
            ),
            userAdvice = "Anjaan link par click bilkul na karein aur na hi kisi group mein share karein. Yeh data phishing ka zariya hai.",
            originalInput = "Tata group ki 150th anniversary par sabhi mobile users ko 3 mahine ka free recharge mil raha hai. Neeche diye link par click karein aur apna recharge claim karein!"
        ),
        FactCheckResult(
            id = 3,
            extractedClaim = "NASA ne Diwali ki raat space se India ki lights ki satellite photo release ki hai.",
            verdict = FactCheckVerdict.MISLEADING,
            confidence = "High",
            explanation = "Yeh photo Diwali ki raat li gayi direct satellite photo nahi hai. Yeh 1992 se 2003 tak ke data par bani population growth aur night lights ki ek composite image (visual art) thi jise National Oceanic and Atmospheric Administration (NOAA) ke scientist ne banaya tha.",
            sources = listOf(
                "NASA Earth Observatory: The Real Story Behind the 'India at Night' Diwali Image",
                "BOOM Live Fact Check: Old composite NASA image shared as Diwali night"
            ),
            userAdvice = "Photo sach mein space data par based hai lekin Diwali night ka real-time photo nahi hai. Sahi context ke bina share na karein.",
            originalInput = "NASA releases stunning photo of India on Diwali night! See how beautifully the country is illuminated from outer space."
        ),
        FactCheckResult(
            id = 4,
            extractedClaim = "Kanya Sumangala / PM Kanya Yojna ke tehat har ladki ke account mein Rs 5000 aane ka message viral hai.",
            verdict = FactCheckVerdict.FAKE,
            confidence = "High",
            explanation = "PIB Fact Check ne spasht kiya hai ki aisi koi direct Rs 5000 cash transfer yojana WhatsApp links ke through nahi chalayi ja rahi hai. Official sarkari yojanaayein sirf sarkari portals par apply hoti hain.",
            sources = listOf(
                "PIB Fact Check Twitter/X: Fake scheme message debunked",
                "Ministry of Women and Child Development Notice"
            ),
            userAdvice = "Sarkari yojanaon ki jankari sirf official government websites (.gov.in ya .nic.in) se hi verify karein.",
            originalInput = "Sarkaar de rahi hai sabhi betiyon ko 5000 rupaye har mahine. Form bharne ke liye yahan click karein."
        ),
        FactCheckResult(
            id = 5,
            extractedClaim = "5G mobile towers se radiation nikal kar bird deaths ya respiratory illness faila rahi hai.",
            verdict = FactCheckVerdict.FAKE,
            confidence = "High",
            explanation = "World Health Organization (WHO) aur Department of Telecommunications (DoT) dono ne clear kiya hai ki 5G radio frequency waves non-ionizing hoti hain aur iska bimariyon se koi lena dena nahi hai.",
            sources = listOf(
                "World Health Organization (WHO) 5G Radiation Q&A",
                "Alt News & PIB Fact Check Joint Advisory on Telecom Misinformation"
            ),
            userAdvice = "Health aur science related claims bina authentic medical ya scientific authority ke forward na karein.",
            originalInput = "5G network testing is causing illness and harmful radiation to people and birds. Stop 5G towers immediately!"
        )
    )

    fun findMatch(query: String): FactCheckResult? {
        val q = query.lowercase()
        return when {
            q.contains("unesco") || q.contains("jana gana mana") || q.contains("best anthem") -> sampleList[0]
            q.contains("free recharge") || q.contains("recharge") || q.contains("tata") && q.contains("anniversary") -> sampleList[1]
            q.contains("diwali") && (q.contains("nasa") || q.contains("space") || q.contains("satellite")) -> sampleList[2]
            q.contains("5000") && (q.contains("kanya") || q.contains("beti") || q.contains("sarkaar") || q.contains("yojna")) -> sampleList[3]
            q.contains("5g") && (q.contains("radiation") || q.contains("tower") || q.contains("bird") || q.contains("illness")) -> sampleList[4]
            else -> null
        }
    }
}
