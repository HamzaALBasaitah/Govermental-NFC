package com.sanad.nfc

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.sanad.nfc.models.*
import com.sanad.nfc.utils.ShakeDetector
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // (أبقيت البيانات العربية هنا للـ Demo، ولكن الواجهة ستنعكس LTR/RTL وتتغير نصوصها)
    private val roles = listOf(
        Role("police", "شرطة", "ش", listOf(
            Document("b", "شهادة ميلاد", "مستخرج رسمي", DocType.BIRTH),
            Document("l", "رخصة قيادة", "صلاحية حتى 2027", DocType.LICENSE)
        )),
        Role("civil", "أحوال / جوازات", "خ", listOf(
            Document("b", "شهادة ميلاد", "نسخة طبق الأصل", DocType.BIRTH),
            Document("v", "المطاعيم", "كوفيد-19 مكتمل", DocType.VACCINE),
            Document("p", "جواز سفر", "ساري المفعول", DocType.PASSPORT)
        )),
        Role("court", "محكمة", "م", listOf(
            Document("d", "شهادة وفاة", "وثيقة رسمية", DocType.DEATH),
            Document("b", "شهادة ميلاد", "نسخة قضائية", DocType.BIRTH),
            Document("de", "صك", "رقم 8812", DocType.DEED)
        )),
        Role("paramedic", "مسعف / إسعاف", "إ", listOf(
            Document("hr", "السجل الصحي (صحتي)", "محدث", DocType.HEALTH),
            Document("ins", "التأمين الصحي", "الشركة الوطنية", DocType.INSURANCE),
            Document("bl", "زمرة الدم", "حرجة", DocType.BLOOD)
        ))
    )

    private var currentRole = roles[0]
    private var citizen: Citizen? = null
    private var isScanning = false
    private var isCitizenVisible = false
    private lateinit var shakeDetector: ShakeDetector

    private lateinit var tabsContainer: LinearLayout
    private lateinit var roleTitle: TextView
    private lateinit var nfcZone: LinearLayout
    private lateinit var nfcText: TextView
    private lateinit var nfcSubtext: TextView
    private lateinit var citizenCard: LinearLayout
    private lateinit var citizenName: TextView
    private lateinit var citizenId: TextView
    private lateinit var citizenDetails: TextView
    private lateinit var citizenAvatar: TextView
    private lateinit var docsContainer: LinearLayout
    private lateinit var emptyState: TextView
    private lateinit var btnLangToggle: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupLanguageToggle()
        setupTabs()
        setupNfcZone()
        setupShakeDetector()
        updateUI()
    }

    private fun initViews() {
        tabsContainer = findViewById(R.id.tabsContainer)
        roleTitle = findViewById(R.id.roleTitle)
        nfcZone = findViewById(R.id.nfcZone)
        nfcText = findViewById(R.id.nfcText)
        nfcSubtext = findViewById(R.id.nfcSubtext)
        citizenCard = findViewById(R.id.citizenCard)
        citizenName = findViewById(R.id.citizenName)
        citizenId = findViewById(R.id.citizenId)
        citizenDetails = findViewById(R.id.citizenDetails)
        citizenAvatar = findViewById(R.id.citizenAvatar)
        docsContainer = findViewById(R.id.docsContainer)
        emptyState = findViewById(R.id.emptyState)
        btnLangToggle = findViewById(R.id.btnLangToggle)
    }

    private fun setupLanguageToggle() {
        btnLangToggle.setOnClickListener {
            // التحقق من اللغة الحالية والتبديل
            val currentLang = resources.configuration.locales.get(0).language
            val newLang = if (currentLang == "ar") "en" else "ar"
            
            // استخدام AppCompatDelegate للتبديل السلس (يدعم من API 14+)
            val appLocale = LocaleListCompat.forLanguageTags(newLang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    private fun setupTabs() {
        tabsContainer.removeAllViews()
        for (role in roles) {
            val tab = TextView(this).apply {
                text = "${role.symbol} ${role.name}"
                setTextColor(android.graphics.Color.WHITE)
                textSize = 13f
                setPadding(28, 10, 28, 10)
                setBackgroundResource(R.drawable.tab_bg)
                setOnClickListener {
                    if (!isScanning) {
                        currentRole = role
                        isCitizenVisible = false
                        citizen = null
                        updateUI()
                    }
                }
            }
            tabsContainer.addView(tab)
        }
    }

    private fun setupNfcZone() {
        nfcZone.setOnClickListener {
            if (!isScanning && !isCitizenVisible) {
                startScan()
            }
        }
    }

    private fun startScan() {
        isScanning = true
        nfcText.text = getString(R.string.scanning)
        nfcSubtext.text = "" // إخفاء النص الفرعي أثناء المسح
        nfcZone.isEnabled = false
        nfcZone.setBackgroundResource(R.drawable.nfc_zone_scanning_bg)

        Handler(Looper.getMainLooper()).postDelayed({
            citizen = CitizenGenerator.generate()
            
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(200)
            }
            
            isScanning = false
            isCitizenVisible = true
            nfcZone.isEnabled = true
            nfcZone.setBackgroundResource(R.drawable.nfc_card_style)
            
            // إرجاع النصوص بعد المسح حسب اللغة
            nfcText.text = getString(R.string.nfc_scan_hint)
            nfcSubtext.text = if (currentRole.id == "paramedic") 
                                getString(R.string.nfc_subhint_paramedic) 
                              else 
                                getString(R.string.nfc_subhint_default)
            updateUI()
        }, 1800)
    }

    private fun setupShakeDetector() {
        shakeDetector = ShakeDetector(this) {
            startActivity(Intent(this, EmergencyActivity::class.java))
        }
    }

    private fun updateUI() {
        roleTitle.text = currentRole.name
        
        for (i in 0 until tabsContainer.childCount) {
            val tab = tabsContainer.getChildAt(i) as TextView
            tab.setBackgroundResource(
                if (roles[i].id == currentRole.id) R.drawable.tab_active_bg
                else R.drawable.tab_bg
            )
        }

        if (isCitizenVisible && citizen != null) {
            citizenCard.apply {
                visibility = View.VISIBLE
                alpha = 0f
                animate().alpha(1f).duration = 600
            }
            citizenName.text = citizen!!.name
            // استخدام التنسيق من ملف Strings
            citizenId.text = getString(R.string.id_number, citizen!!.id)
            citizenAvatar.text = citizen!!.name.first().toString()

            if (currentRole.id == "paramedic") {
                citizenDetails.visibility = View.VISIBLE
                citizenDetails.text = getString(
                    R.string.blood_type_age, 
                    citizen!!.bloodType, 
                    citizen!!.age, 
                    citizen!!.healthStatus
                )
            } else {
                citizenDetails.visibility = View.GONE
            }

            buildDocumentList()
            emptyState.visibility = View.GONE
            
            docsContainer.apply {
                visibility = View.VISIBLE
                alpha = 0f
                animate().alpha(1f).duration = 700
            }
        } else {
            citizenCard.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
            docsContainer.visibility = View.GONE
            
            // تحديث النص الفرعي في حالة عدم وجود مواطن
            nfcSubtext.text = if (currentRole.id == "paramedic") 
                                getString(R.string.nfc_subhint_paramedic) 
                              else 
                                getString(R.string.nfc_subhint_default)
        }
    }

    private fun buildDocumentList() {
        docsContainer.removeAllViews()
        for (doc in currentRole.documents) {
            val docView = layoutInflater.inflate(R.layout.item_document, docsContainer, false)
            docView.alpha = 0f
            docView.animate().alpha(1f).setDuration(500).start()
            
            val iconView = docView.findViewById<TextView>(R.id.docIcon)
            val titleView = docView.findViewById<TextView>(R.id.docTitle)
            val descView = docView.findViewById<TextView>(R.id.docDesc)
            
            iconView.text = doc.type.icon
            iconView.background.setTint(getColor(doc.type.colorRes))
            titleView.text = doc.title
            descView.text = doc.description
            
            docsContainer.addView(docView)
        }
    }

    override fun onResume() {
        super.onResume()
        shakeDetector.start()
    }

    override fun onPause() {
        super.onPause()
        shakeDetector.stop()
    }
}
