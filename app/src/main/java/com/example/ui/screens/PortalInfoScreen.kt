package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class PortalType {
    ADMISSION,
    EXAM_FORM,
    ADMIT_CARD,
    SCHOLARSHIPS,
    PYQ_PAPERS,
    SYLLABUS,
    IMPORTANT_LINKS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalInfoScreen(
    portalType: PortalType,
    onBackClick: () -> Unit,
    onOpenWebView: ((url: String, title: String) -> Unit)? = null
) {
    val context = LocalContext.current

    val title = when (portalType) {
        PortalType.ADMISSION -> "PPU All Courses Admission"
        PortalType.EXAM_FORM -> "PPU Exam Form Portal"
        PortalType.ADMIT_CARD -> "PPU Admit Card Portal"
        PortalType.SCHOLARSHIPS -> "🎓 Scholarships"
        PortalType.PYQ_PAPERS -> "📚 Previous Year Question Papers (PYQ)"
        PortalType.SYLLABUS -> "📘 COURSE STRUCTURE & SYLLABUS"
        PortalType.IMPORTANT_LINKS -> "🔗 Important Links"
    }

    val openUrl: (String) -> Unit = { url ->
        if (portalType == PortalType.EXAM_FORM && onOpenWebView != null) {
            onOpenWebView(url, title)
        } else {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } catch (_: Exception) {
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("portal_info_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (portalType) {
                PortalType.ADMISSION, PortalType.EXAM_FORM, PortalType.ADMIT_CARD -> {
                    StandardPortalContent(
                        portalType = portalType,
                        onOpenUrl = openUrl,
                        onOpenWebView = onOpenWebView,
                        portalTitle = title
                    )
                }
                PortalType.SCHOLARSHIPS -> {
                    ScholarshipsContent(onOpenUrl = openUrl)
                }
                PortalType.PYQ_PAPERS -> {
                    PyqPapersContent(onOpenUrl = openUrl)
                }
                PortalType.SYLLABUS -> {
                    SyllabusContent(onOpenUrl = openUrl)
                }
                PortalType.IMPORTANT_LINKS -> {
                    ImportantLinksContent(onOpenUrl = openUrl)
                }
            }
        }
    }
}

@Composable
private fun StandardPortalContent(
    portalType: PortalType,
    onOpenUrl: (String) -> Unit,
    onOpenWebView: ((url: String, title: String) -> Unit)? = null,
    portalTitle: String = "PPU Portal"
) {
    val targetUrl = when (portalType) {
        PortalType.ADMISSION -> "https://ppupadm.samarth.edu.in/"
        PortalType.EXAM_FORM -> "https://ppuponline.in/exam_form_search_student_semester.php"
        PortalType.ADMIT_CARD -> "https://ppuponline.in/"
        else -> "https://ppup.ac.in"
    }

    val buttonText = when (portalType) {
        PortalType.ADMISSION -> "🌐 PPU Samarth Portal खोलें"
        PortalType.EXAM_FORM -> "📝 Exam Form भरें"
        PortalType.ADMIT_CARD -> "📄 Admit Card डाउनलोड करें"
        else -> "Open Portal"
    }

    // Header Banner Card
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (portalType) {
                            PortalType.ADMISSION -> Icons.Outlined.School
                            PortalType.EXAM_FORM -> Icons.Outlined.EditNote
                            PortalType.ADMIT_CARD -> Icons.Outlined.Badge
                            else -> Icons.Outlined.Link
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = when (portalType) {
                        PortalType.ADMISSION -> "Samarth Admission Portal"
                        PortalType.EXAM_FORM -> "Examination Portal"
                        PortalType.ADMIT_CARD -> "Admit Card Download"
                        else -> "PPU Portal"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Patliputra University, Patna",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }

    // Main Info Content Box
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (portalType) {
                PortalType.ADMISSION -> {
                    Text(
                        text = "पाटलिपुत्र विश्वविद्यालय (PPU) में सभी UG, PG एवं अन्य पाठ्यक्रमों में नामांकन अब समार्थ (Samarth) पोर्टल के माध्यम से किया जाता है।",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "ऑनलाइन आवेदन करने से पहले कृपया आधिकारिक निर्देश ध्यानपूर्वक पढ़ें। इसके बाद नीचे दिए गए लिंक पर क्लिक करके Samarth Portal पर जाकर अपना आवेदन करें।",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Column {
                        Text(
                            text = "PPU Samarth Portal",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "https://ppupadm.samarth.edu.in/",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                PortalType.EXAM_FORM -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📝 परीक्षा फॉर्म (Exam Form)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "पाटलिपुत्र विश्वविद्यालय द्वारा सभी Courses का Examination Form ऑनलाइन भरा जाता है।",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "अपने Course का Examination Form भरने के लिए, Re-Payment करने अथवा Form Number जानने के लिए नीचे दिए गए संबंधित बटन पर क्लिक करें।",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "PPU Exam Form Links:-",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "• Fill Exam Form: https://ppuponline.in/exam_form_search_student_semester.php",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "• Re-Payment / Print Receipt: https://www.ppuponline.in/examination_payment_search_1.php",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "• Know Your Form Number: https://www.ppuponline.in/Exam_Form_Students_Search_ID.php",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                PortalType.ADMIT_CARD -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📄 प्रवेश पत्र (Admit Card)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "पाटलिपुत्र विश्वविद्यालय द्वारा सभी Courses का ऑनलाइन Examination Form भरने के बाद, जब विश्वविद्यालय द्वारा Admit Card जारी किया जाता है, तो उसका लिंक इस पेज पर उपलब्ध करा दिया जाता है।",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "आप अपने Course के Admit Card लिंक पर क्लिक करके अपना Admit Card आसानी से डाउनलोड कर सकते हैं।",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Column {
                        Text(
                            text = "Admit Card Link:-",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "https://ppuponline.in/",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                else -> {}
            }
        }
    }

    // Large Action Button
    Button(
        onClick = { onOpenUrl(targetUrl) },
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("portal_info_action_button")
    ) {
        Text(
            text = buttonText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center
        )
    }

    if (portalType == PortalType.EXAM_FORM) {
        Spacer(modifier = Modifier.height(12.dp))

        // Button 1: Re-Payment Examination Form / Print Receipt
        Button(
            onClick = {
                val url = "https://www.ppuponline.in/examination_payment_search_1.php"
                val title = "Re-Payment Exam Form / Print Receipt"
                if (onOpenWebView != null) {
                    onOpenWebView(url, title)
                } else {
                    onOpenUrl(url)
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            contentPadding = PaddingValues(vertical = 14.dp, horizontal = 20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("repayment_exam_form_button")
        ) {
            Icon(
                imageVector = Icons.Outlined.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "💳 Re-Payment Examination Form / Print Receipt",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Button 2: Know Your Form Number
        Button(
            onClick = {
                val url = "https://www.ppuponline.in/Exam_Form_Students_Search_ID.php"
                val title = "Know Your Form Number"
                if (onOpenWebView != null) {
                    onOpenWebView(url, title)
                } else {
                    onOpenUrl(url)
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            contentPadding = PaddingValues(vertical = 14.dp, horizontal = 20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("know_form_number_button")
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "🔍 Know Your Form Number",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Button 3: PPU Online Home Portal
        Button(
            onClick = {
                val url = "https://www.ppuponline.in/"
                val title = "PPU Online Home Portal"
                if (onOpenWebView != null) {
                    onOpenWebView(url, title)
                } else {
                    onOpenUrl(url)
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            contentPadding = PaddingValues(vertical = 14.dp, horizontal = 20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ppu_online_home_portal_button")
        ) {
            Icon(
                imageVector = Icons.Outlined.Language,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "🌐 PPU Online Home Portal",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }

    // Separate Note Card for Admit Card or Exam Form
    val noteText = when (portalType) {
        PortalType.ADMIT_CARD -> "यदि Admit Card डाउनलोड करते समय \"Not Verify by College\" संदेश दिखाई देता है, तो कृपया कुछ समय बाद पुनः प्रयास करें। आपके महाविद्यालय द्वारा परीक्षा फॉर्म का सत्यापन (Verification) होते ही आपका Admit Card जारी कर दिया जाएगा।"
        PortalType.EXAM_FORM -> "Examination Form को Final Submit करने से पहले एक बार सभी विवरण (Details) अच्छी तरह जाँच लें। परीक्षा फॉर्म भरने की अंतिम तिथि एवं अन्य महत्वपूर्ण निर्देशों के लिए पाटलिपुत्र विश्वविद्यालय द्वारा जारी आधिकारिक सूचना अवश्य देखें।"
        else -> null
    }

    if (noteText != null) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Note",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "📌 महत्वपूर्ण नोट:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = noteText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ScholarshipsContent(onOpenUrl: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Intro Banner Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "इस पेज पर पाटलिपुत्र विश्वविद्यालय (PPU) एवं भारत सरकार/बिहार सरकार द्वारा संचालित प्रमुख छात्रवृत्ति योजनाओं की जानकारी उपलब्ध है। कृपया अपनी पात्रता के अनुसार संबंधित छात्रवृत्ति योजना का चयन करें।",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    lineHeight = 22.sp
                )
            }
        }

        // 1. Bihar Post Matric Scholarship (PMS)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "1. Bihar Post Matric Scholarship (PMS) 2026-27",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "बिहार पोस्ट मैट्रिक छात्रवृत्ति (PMS) बिहार सरकार द्वारा संचालित एक महत्वपूर्ण छात्रवृत्ति योजना है। इस योजना के अंतर्गत बिहार के पात्र छात्र-छात्राओं को उच्च शिक्षा के लिए आर्थिक सहायता प्रदान की जाती है।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = "Eligibility:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• बिहार का स्थायी निवासी होना चाहिए।\n• Post Matric (Intermediate के बाद) किसी मान्यता प्राप्त संस्थान में अध्ययनरत होना चाहिए।\n• संबंधित श्रेणी के अनुसार पात्र होना चाहिए।\n• आधार एवं बैंक खाता DBT से लिंक होना चाहिए।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = "Required Documents:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• Aadhaar Card\n• Income Certificate\n• Caste Certificate (यदि लागू हो)\n• Residential Certificate\n• Bonafide Certificate\n• Previous Marksheet\n• Bank Passbook\n• Passport Size Photo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row {
                    Text(text = "Application Mode: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text(text = "Online", style = MaterialTheme.typography.bodySmall)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📅 Last Date: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text(text = "15 September 2026", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }

                Button(
                    onClick = { onOpenUrl("https://pmsonline.bihar.gov.in/pms/pms_online/Default.aspx") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🎓 Bihar PMS Portal खोलें", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. National Scholarship Portal (NSP)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "2. National Scholarship Portal (NSP) 2026-27",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // (A) Central Sector Scheme (CSSS)
                Text(
                    text = "(A) Central Sector Scheme (CSSS)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "यह भारत सरकार द्वारा मेधावी विद्यार्थियों के लिए संचालित छात्रवृत्ति योजना है।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Eligibility:\n• 12वीं में अच्छे अंक।\n• परिवार की वार्षिक आय निर्धारित सीमा के भीतर।\n• Regular UG Course में Admission।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📅 Last Date: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text(text = "31 October 2026", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // (B) Post Graduate Scholarship
                Text(
                    text = "(B) Post Graduate Scholarship",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "यह योजना PG Course में अध्ययनरत पात्र विद्यार्थियों के लिए है।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Eligibility:\n• Regular PG Course में Admission।\n• आधिकारिक पात्रता शर्तें पूरी करनी होंगी।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Button(
                    onClick = { onOpenUrl("https://scholarships.gov.in/") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🌐 NSP Portal खोलें", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 3. Mukhyamantri Kanya Utthan Graduation Scholarship
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "3. Mukhyamantri Kanya Utthan Graduation Scholarship",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "मुख्यमंत्री कन्या उत्थान (स्नातक) छात्रवृत्ति योजना बिहार सरकार द्वारा स्नातक उत्तीर्ण छात्राओं को प्रोत्साहन राशि प्रदान करने के उद्देश्य से संचालित की जाती है।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = "Eligibility:\n• बिहार की स्थायी निवासी छात्रा।\n• मान्यता प्राप्त विश्वविद्यालय से स्नातक उत्तीर्ण।\n• सरकार द्वारा निर्धारित सभी शर्तें पूर्ण होनी चाहिए।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                // Note Box
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📢 Note:\n\nजिन सभी छात्राओं का नाम Medhasoft Portal पर अपलोड हो गया है, उनका ऑनलाइन आवेदन बहुत जल्द प्रारम्भ किया जाएगा। कृपया नियमित रूप से आधिकारिक पोर्टल पर अपडेट देखते रहें।",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            lineHeight = 18.sp
                        )
                    }
                }

                Button(
                    onClick = { onOpenUrl("https://medhasoft.bihar.gov.in/") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("👩‍🎓 Medhasoft Portal खोलें", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PyqPapersContent(onOpenUrl: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Previous Year Question Papers (PYQ)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = "पाटलिपुत्र विश्वविद्यालय (PPU) में अब तक जितने भी Courses की परीक्षाएँ आयोजित हुई हैं, उन सभी के Previous Year Question Papers (PYQ) की PDF हमारे Telegram Channel पर उपलब्ध है।",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                )

                Text(
                    text = "आप अपने Course एवं Semester के अनुसार Previous Year Question Papers को आसानी से डाउनलोड करके अपनी परीक्षा की तैयारी कर सकते हैं।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "📢 Note:\n\nजिन भी Courses के Previous Year Question Papers (PYQ) अभी तक अपलोड नहीं किए गए हैं, उन्हें भी बहुत जल्द हमारे Telegram Channel पर अपलोड कर दिया जाएगा।",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onOpenUrl("https://t.me/PPUeSLM") },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0088CC) // Telegram Blue
            ),
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "📚 PPU PYQ Telegram Channel खोलें",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SyllabusContent(onOpenUrl: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Intro Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "इस पेज पर पाटलिपुत्र विश्वविद्यालय (PPU) के विभिन्न Courses का Course Structure एवं Syllabus उपलब्ध है।",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    lineHeight = 22.sp
                )
            }
        }

        // 4 Cards
        val syllabusItems = listOf(
            SyllabusItemData(
                title = "1️⃣ Course Structure",
                url = "https://ppup.ac.in/course-structure",
                buttonText = "📘 Course Structure देखें"
            ),
            SyllabusItemData(
                title = "2️⃣ UG Syllabus",
                url = "https://ppup.ac.in/ug-syllabus",
                buttonText = "🎓 UG Syllabus देखें"
            ),
            SyllabusItemData(
                title = "3️⃣ PG Syllabus",
                url = "https://ppup.ac.in/pg-syllabus",
                buttonText = "🎓 PG Syllabus देखें"
            ),
            SyllabusItemData(
                title = "4️⃣ Vocational Courses",
                url = "https://ppup.ac.in/vocational-courses",
                buttonText = "💼 Vocational Courses देखें"
            )
        )

        syllabusItems.forEach { item ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = { onOpenUrl(item.url) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(item.buttonText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Telegram Note Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "📢 Note:\n\nजिन भी Courses का Syllabus Patliputra University की Official Website पर उपलब्ध नहीं है, वे हमारे Telegram Channel पर जाकर देख सकते हैं। वहाँ Patliputra University के लगभग सभी Courses का Syllabus PDF उपलब्ध है।",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    lineHeight = 20.sp
                )

                Button(
                    onClick = { onOpenUrl("https://t.me/PPUeSLM") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📚 PPU Syllabus Telegram Channel खोलें", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

private data class SyllabusItemData(
    val title: String,
    val url: String,
    val buttonText: String
)

@Composable
private fun ImportantLinksContent(onOpenUrl: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. WhatsApp Channel
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "1️⃣ PPU Patna Info WhatsApp Channel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF25D366) // WhatsApp Green
                )

                Text(
                    text = "पाटलिपुत्र विश्वविद्यालय (PPU) से जुड़ी हर छोटी-बड़ी खबर, Admission, Exam, Result, Scholarship, Notice एवं अन्य महत्वपूर्ण अपडेट सबसे पहले प्राप्त करने के लिए हमारे WhatsApp Channel से जुड़े रहें।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )

                Button(
                    onClick = { onOpenUrl("https://whatsapp.com/channel/0029VaFM6uUFnSzHCwXDBq21") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🟢 WhatsApp Channel Join करें", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // 2. Telegram Channel
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "2️⃣ PPU Patna Info Telegram Channel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0088CC) // Telegram Blue
                )

                Text(
                    text = "Patliputra University से संबंधित सभी महत्वपूर्ण सूचनाएँ, Notice, Result, Admit Card, PYQ, Syllabus, Scholarship एवं अन्य अध्ययन सामग्री प्राप्त करने के लिए हमारे Telegram Channel से जुड़ें।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )

                Button(
                    onClick = { onOpenUrl("https://t.me/PPUPatnaInfo") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔵 Telegram Channel Join करें", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // 3. Telegram Discussion Group
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "3️⃣ PPU Patna Discussion Group",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "यदि आपको Patliputra University या किसी भी संबद्ध कॉलेज से संबंधित कोई प्रश्न पूछना है, किसी समस्या का समाधान चाहिए या अन्य विद्यार्थियों से चर्चा करनी है, तो हमारे Discussion Group से जुड़ें।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )

                Button(
                    onClick = { onOpenUrl("https://t.me/PPUPatnaGroup") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("👥 Discussion Group Join करें", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
