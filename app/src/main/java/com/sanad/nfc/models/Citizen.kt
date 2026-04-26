package com.sanad.nfc.models
import com.sanad.nfc.R

data class Role(val id: String, val name: String, val symbol: String, val documents: List<Document>)
data class Document(val id: String, val title: String, val description: String, val type: DocType)
enum class DocType(val label: String, val colorRes: Int, val icon: String) {
    BIRTH("شهادة ميلاد", R.color.primary_light, "م"),
    LICENSE("رخصة قيادة", R.color.gold, "ر"),
    PASSPORT("جواز سفر", R.color.primary_dark, "ج"),
    VACCINE("مطاعيم", R.color.primary, "ط"),
    HEALTH("سجل صحي", R.color.emergency, "ـ"),
    INSURANCE("تأمين صحي", R.color.primary, "ت"),
    BLOOD("زمرة دم", R.color.emergency, "د"),
    DEATH("شهادة وفاة", R.color.emergency, "و"),
    DEED("صك", R.color.gold, "ص"),
    PROPERTY("سند ملكية", R.color.primary_dark, "س")
}
data class Citizen(val name: String, val id: String, val bloodType: String, val age: Int, val healthStatus: String, val gender: String, val chronicDiseases: List<String>)

object CitizenGenerator {
    fun generate(): Citizen = Citizen("حمزة البسايطة", "9991234567", "O+", 25, "سليم", "ذكر", listOf())
}
