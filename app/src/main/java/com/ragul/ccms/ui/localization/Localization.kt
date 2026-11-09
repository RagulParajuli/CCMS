package com.ragul.ccms.ui.localization

import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

enum class AppLanguage { NEPALI, ENGLISH }

val LocalAppLanguage = compositionLocalOf { AppLanguage.NEPALI }
val LocalSetAppLanguage = compositionLocalOf<(AppLanguage) -> Unit> { {} }

@Composable
fun LanguageToggle(modifier: Modifier = Modifier) {
    val language = LocalAppLanguage.current
    val setLanguage = LocalSetAppLanguage.current
    TextButton(onClick = {
        setLanguage(if (language == AppLanguage.NEPALI) AppLanguage.ENGLISH else AppLanguage.NEPALI)
    }, modifier = modifier) {
        MaterialText(if (language == AppLanguage.NEPALI) "English" else "नेपाली")
    }
}

private val nepali = mapOf(
    "Login" to "लगइन", "Sign in" to "साइन इन", "Sign In" to "साइन इन", "Sign up" to "खाता खोल्नुहोस्",
    "Sign Up" to "खाता खोल्नुहोस्", "Create account" to "खाता खोल्नुहोस्", "Forgot password?" to "पासवर्ड बिर्सनुभयो?",
    "Forgot Password" to "पासवर्ड बिर्सनुभयो", "Reset password" to "पासवर्ड रिसेट गर्नुहोस्", "Send reset link" to "रिसेट लिङ्क पठाउनुहोस्",
    "Email" to "इमेल", "Password" to "पासवर्ड", "Confirm password" to "पासवर्ड पुष्टि गर्नुहोस्", "Full name" to "पूरा नाम",
    "Name" to "नाम", "Phone" to "फोन", "or" to "वा", "Show" to "देखाउनुहोस्", "Hide" to "लुकाउनुहोस्",
    "Cancel" to "रद्द गर्नुहोस्", "Save" to "सुरक्षित गर्नुहोस्", "Delete" to "मेटाउनुहोस्", "Edit" to "सम्पादन",
    "Dashboard" to "ड्यासबोर्ड", "Membership" to "सदस्यता", "Loan / Deposit" to "ऋण / बचत", "Loan & Deposit" to "ऋण तथा बचत",
    "Monthly Due" to "मासिक किस्ता", "About App" to "एपबारे", "History" to "इतिहास", "Sign out" to "साइन आउट",
    "Sign Out" to "साइन आउट", "Add" to "थप्नुहोस्", "Search" to "खोज्नुहोस्", "Notes" to "कैफियत",
    "Status" to "अवस्था", "Active" to "सक्रिय", "Inactive" to "निष्क्रिय", "Paid" to "भुक्तानी भयो",
    "Pending" to "बाँकी", "Collected" to "संकलित", "Grand Total" to "कुल जम्मा", "Amount Deposit" to "जम्मा रकम",
    "Remaining Balance" to "बाँकी रकम", "BS Month" to "वि.सं. महिना", "BS Year" to "वि.सं. वर्ष",
    "Select member" to "सदस्य छान्नुहोस्", "Member Name" to "सदस्यको नाम", "Member ID" to "सदस्य नं.",
    "Select or Type" to "छान्नुहोस् वा लेख्नुहोस्", "Period" to "अवधि", "Payment status" to "भुक्तानी अवस्था",
    "Add Monthly Due" to "मासिक किस्ता थप्नुहोस्", "Edit Monthly Due" to "मासिक किस्ता सम्पादन गर्नुहोस्",
    "Delete monthly due?" to "मासिक किस्ता मेटाउने?", "Monthly due saved." to "मासिक किस्ता सुरक्षित भयो।",
    "Monthly due deleted." to "मासिक किस्ता मेटाइयो।", "No monthly dues yet" to "अहिलेसम्म मासिक किस्ता छैन",
    "Add Membership" to "सदस्य थप्नुहोस्", "Edit Membership" to "सदस्य सम्पादन गर्नुहोस्", "Delete membership?" to "सदस्य मेटाउने?",
    "Add Loan / Deposit" to "ऋण / बचत थप्नुहोस्", "Add Loan or Deposit" to "ऋण वा बचत थप्नुहोस्",
    "Delete record?" to "रेकर्ड मेटाउने?", "Clear history?" to "इतिहास हटाउने?", "No records yet" to "अहिलेसम्म कुनै रेकर्ड छैन",
    "No history yet" to "अहिलेसम्म इतिहास छैन", "About" to "बारेमा", "Members" to "सदस्यहरू", "Loans" to "ऋणहरू",
    "Deposits" to "बचतहरू", "Total" to "जम्मा", "Settings" to "सेटिङ", "English" to "English", "नेपाली" to "नेपाली",
    "Language" to "भाषा", "Tap + to add a due. The dashboard count updates automatically." to "किस्ता थप्न + थिच्नुहोस्। ड्यासबोर्डको संख्या स्वतः अद्यावधिक हुन्छ।",
    "Monthly contributions and payment status." to "मासिक योगदान र भुक्तानीको अवस्था।", "Added" to "थपिएको",
    "Select BS month" to "वि.सं. महिना छान्नुहोस्", "Select BS year" to "वि.सं. वर्ष छान्नुहोस्",
    "Enter a valid total and a deposit no greater than the total." to "मान्य कुल रकम र कुलभन्दा बढी नहुने जम्मा रकम लेख्नुहोस्।",
    "Enter a valid amount." to "मान्य रकम लेख्नुहोस्।", "Enter an amount greater than zero." to "शून्यभन्दा ठूलो रकम लेख्नुहोस्।",
    "Log in" to "लगइन", "Logout" to "लगआउट", "Continue with Google" to "Google बाट जारी राख्नुहोस्",
    "Already have an account?" to "पहिले नै खाता छ?", "Don't have an account?" to "खाता छैन?",
    "Good to see you!" to "तपाईंलाई फेरि भेटेर खुसी लाग्यो!", "Let's continue the journey." to "यात्रा जारी राखौँ।",
    "A quick overview of your community records." to "तपाईंको समुदायका रेकर्डहरूको संक्षिप्त जानकारी।",
    "Here is the latest snapshot of your community records." to "तपाईंको समुदायका रेकर्डहरूको पछिल्लो विवरण।",
    "Community records" to "समुदायका रेकर्ड", "Overview" to "अवलोकन", "Add member" to "सदस्य थप्नुहोस्",
    "Add Record" to "रेकर्ड थप्नुहोस्", "Add loan or deposit" to "ऋण वा बचत थप्नुहोस्",
    "Edit Member" to "सदस्य सम्पादन", "Edit record" to "रेकर्ड सम्पादन", "Delete member?" to "सदस्य मेटाउने?",
    "Delete due" to "किस्ता मेटाउनुहोस्", "Delete member" to "सदस्य मेटाउनुहोस्", "Delete record" to "रेकर्ड मेटाउनुहोस्",
    "Edit due" to "किस्ता सम्पादन", "No members yet" to "अहिलेसम्म सदस्य छैनन्", "No loan or deposit records" to "ऋण वा बचतको रेकर्ड छैन",
    "No activity yet" to "अहिलेसम्म गतिविधि छैन", "No matching members" to "मिल्ने सदस्य छैनन्",
    "People registered under this account." to "यस खातामा दर्ता भएका सदस्यहरू।",
    "Monthly contribution entries" to "मासिक योगदानका प्रविष्टि", "Monthly contributions and their payment status." to "मासिक योगदान र भुक्तानी अवस्था।",
    "Money lent or deposited" to "दिएको ऋण वा जम्मा बचत", "Money lent to members or deposited with them." to "सदस्यलाई दिएको ऋण वा उनीहरूबाट जम्मा बचत।",
    "Add, edit or delete a record and the change will appear here." to "रेकर्ड थप्दा, सम्पादन गर्दा वा मेटाउँदा परिवर्तन यहाँ देखिन्छ।",
    "Every record change made on this account." to "यस खातामा भएका सबै रेकर्ड परिवर्तन।", "Clear" to "हटाउनुहोस्",
    "History cleared." to "इतिहास हटाइयो।", "Address" to "ठेगाना", "Gender" to "लिङ्ग", "Male" to "पुरुष",
    "Female" to "महिला", "Other" to "अन्य", "Date of Birth" to "जन्म मिति", "Date of Birth (BS)" to "जन्म मिति (वि.सं.)",
    "Father/Mother" to "बुबा/आमा", "Father's or Mother's Name" to "बुबा वा आमाको नाम", "Grandfather" to "हजुरबुबा",
    "Grandfather's Name" to "हजुरबुबाको नाम", "Citizenship #" to "नागरिकता नं.", "Citizenship Number" to "नागरिकता नम्बर",
    "Mobile Number" to "मोबाइल नम्बर", "Email Address" to "इमेल ठेगाना", "Email & password" to "इमेल र पासवर्ड",
    "Email verified" to "इमेल प्रमाणित", "Name is required." to "नाम आवश्यक छ।", "Passwords do not match." to "पासवर्डहरू मेल खाँदैनन्।",
    "Date of birth cannot be in the future." to "जन्म मिति भविष्यको हुन सक्दैन।", "Must be greater than zero." to "शून्यभन्दा ठूलो हुनुपर्छ।",
    "Must be zero or more." to "शून्य वा त्योभन्दा बढी हुनुपर्छ।", "Deposit" to "बचत", "Loan" to "ऋण", "Interest" to "ब्याज",
    "Duration in years." to "अवधि (वर्षमा)", "Annual interest rate." to "वार्षिक ब्याजदर।", "Amount (Principal) *" to "रकम (मूलधन) *",
    "Not available" to "उपलब्ध छैन", "Not set" to "सेट गरिएको छैन", "OK" to "ठीक छ", "More" to "थप", "More details" to "थप विवरण",
    "Open navigation" to "नेभिगेसन खोल्नुहोस्", "Resend verification email" to "प्रमाणीकरण इमेल फेरि पठाउनुहोस्",
    "Could not save the record." to "रेकर्ड सुरक्षित गर्न सकिएन।", "Enter your email and we'll send you a reset link." to "इमेल लेख्नुहोस्, हामी रिसेट लिङ्क पठाउनेछौँ।",
    "Log In" to "लगइन", "Welcome!" to "स्वागत छ!", "Sign up to get started." to "सुरु गर्न खाता खोल्नुहोस्।",
    "Remembered your password?" to "पासवर्ड सम्झनुभयो?", "Loan or Deposit" to "ऋण वा बचत",
    "What CCMS does and how your data is stored." to "CCMS का काम र तपाईंको डाटा कसरी सुरक्षित हुन्छ।",
    "Resend verification email" to "प्रमाणीकरण इमेल फेरि पठाउनुहोस्", "Password reset link sent" to "पासवर्ड रिसेट लिङ्क पठाइयो",
    "Password reset link sent" to "पासवर्ड रिसेट लिङ्क पठाइयो",
    "Recent activity" to "हालका गतिविधि", "Record deleted." to "रेकर्ड मेटाइयो।", "Records overview" to "रेकर्डको सारांश",
    "Register Member" to "सदस्य दर्ता गर्नुहोस्", "Registered members" to "दर्ता भएका सदस्यहरू",
    "Registered members & family details." to "दर्ता भएका सदस्य र परिवारका विवरण।",
    "Records are stored on this device" to "रेकर्डहरू यस उपकरणमा सुरक्षित छन्",
    "reflects the records saved in the app database." to "एपको डाटाबेसमा सुरक्षित रेकर्डहरूको विवरण हो।",
    "Rate (% per Year) *" to "ब्याजदर (% वार्षिक) *", "Time (Years) *" to "अवधि (वर्ष) *",
    "Total (Amount + Interest)" to "जम्मा (रकम + ब्याज)", "Type" to "प्रकार", "Year" to "वर्ष", "Yes" to "हो",
    "Show less" to "कम देखाउनुहोस्", "Sign up with Google" to "Google बाट खाता खोल्नुहोस्",
    "Signed in" to "लगइन भयो", "Signed in with email" to "इमेलबाट लगइन भयो", "Signed in with Google" to "Google बाट लगइन भयो",
    "Signed-in account" to "लगइन गरिएको खाता", "Sign-in method" to "लगइन विधि", "Version & data" to "संस्करण र डाटा",
    "Select a Bikram Sambat date. Month lengths change every year." to "विक्रम संवत् मिति छान्नुहोस्। महिनाको दिन संख्या हरेक वर्ष फरक हुन्छ।",
    "Select Nepali date of birth" to "नेपाली जन्म मिति छान्नुहोस्", "Select member" to "सदस्य छान्नुहोस्",
    "Your chart will appear when you add the first record." to "पहिलो रेकर्ड थपेपछि चार्ट देखिनेछ।",
    "Tap + to add a loan or deposit. The dashboard count updates automatically." to "ऋण वा बचत थप्न + थिच्नुहोस्। ड्यासबोर्डको संख्या स्वतः अद्यावधिक हुन्छ।",
    "Tap + to register the first member with auto-generated ID." to "स्वतः बन्ने सदस्य नम्बरसहित पहिलो सदस्य दर्ता गर्न + थिच्नुहोस्।",
    "Verification email sent. Check your inbox for the verification link." to "प्रमाणीकरण इमेल पठाइयो। आफ्नो इनबक्समा लिङ्क हेर्नुहोस्।",
    "Your email is not verified yet. Check your inbox for the verification link." to "तपाईंको इमेल प्रमाणित भएको छैन। इनबक्समा आएको लिङ्क खोल्नुहोस्।",
    "will be removed and the dashboard count will update." to "हटाइनेछ र ड्यासबोर्डको संख्या अद्यावधिक हुनेछ।",
    "Firebase is not configured. Add app/google-services.json and rebuild. See docs/FIREBASE_SETUP.md for the steps." to "Firebase सेटअप भएको छैन। app/google-services.json थपेर फेरि बिल्ड गर्नुहोस्।",
    "Member Name (Select or Type) *" to "सदस्यको नाम (छान्नुहोस् वा लेख्नुहोस्) *", "Member Name *" to "सदस्यको नाम *",
    "Member or Title (Select or Type) *" to "सदस्य वा शीर्षक (छान्नुहोस् वा लेख्नुहोस्) *", "Member or Title *" to "सदस्य वा शीर्षक *",
    "Member's Name *" to "सदस्यको नाम *", "Membership ID:" to "सदस्यता नं.:", "Search by ID, name, mobile, citizenship #" to "नं., नाम, मोबाइल वा नागरिकता खोज्नुहोस्",
    "Add Monthly Due" to "मासिक किस्ता थप्नुहोस्", "Edit Monthly Due" to "मासिक किस्ता सम्पादन गर्नुहोस्",
    "Grand Total *" to "कुल जम्मा *", "Amount Deposit" to "जम्मा रकम", "Remaining Balance:" to "बाँकी रकम:",
    "Annual interest rate." to "वार्षिक ब्याजदर।", "Duration in years." to "अवधि (वर्षमा)",
    "Monthly Due" to "मासिक किस्ता", "Membership" to "सदस्यता", "Loan or Deposit" to "ऋण वा बचत",
    "Loans" to "ऋणहरू", "Deposits" to "बचतहरू", "Collected" to "संकलित", "Pending" to "बाँकी",
    "People registered under this account." to "यस खातामा दर्ता भएका सदस्यहरू।", "No members found" to "सदस्य भेटिएनन्",
    "No records yet" to "अहिलेसम्म कुनै रेकर्ड छैन", "No monthly dues yet" to "अहिलेसम्म मासिक किस्ता छैन",
    "No loan or deposit records" to "ऋण वा बचतको रेकर्ड छैन", "No history yet" to "अहिलेसम्म इतिहास छैन",
    "Loan saved." to "ऋण सुरक्षित भयो।", "Deposit saved." to "बचत सुरक्षित भयो.", "Membership saved." to "सदस्यता सुरक्षित भयो।",
    "Membership deleted." to "सदस्यता मेटाइयो।", "Monthly due saved." to "मासिक किस्ता सुरक्षित भयो।",
    "Monthly due deleted." to "मासिक किस्ता मेटाइयो।", "Delete monthly due?" to "मासिक किस्ता मेटाउने?",
    "Delete member?" to "सदस्य मेटाउने?", "Delete record?" to "रेकर्ड मेटाउने?", "Clear history?" to "इतिहास हटाउने?",
    "Google Sign-In is not configured. Enable the Google provider in Firebase, add your SHA-1/SHA-256 fingerprint, then re-download google-services.json or set GOOGLE_WEB_CLIENT_ID. See docs/FIREBASE_SETUP.md." to "Google साइन इन सेटअप भएको छैन। Firebase मा Google provider सक्षम गरी SHA fingerprint थप्नुहोस्।",
)

fun localizedText(value: String, language: AppLanguage): String {
    if (language == AppLanguage.ENGLISH) return value
    nepali[value]?.let { return it }
    return when {
        value.startsWith("Remaining Balance: ") -> "बाँकी रकम: ${value.substringAfter(": ")}"
        value.startsWith("Added ") -> "थपिएको ${value.removePrefix("Added ")}"
        value.startsWith("ID: #") -> "सदस्य नं.: ${value.removePrefix("ID: ")}"
        value.startsWith("Membership ID: ") -> "सदस्यता नं.: ${value.removePrefix("Membership ID: ")}"
        value.matches(Regex("\\d+ total")) -> "${value.substringBefore(' ')} जम्मा"
        value.startsWith("Search by ID") -> "नं., नाम, मोबाइल वा नागरिकता खोज्नुहोस्"
        else -> value
    }
}

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = androidx.compose.material3.LocalTextStyle.current,
) {
    MaterialText(
        text = localizedText(text, LocalAppLanguage.current), modifier = modifier, color = color,
        fontSize = fontSize, fontStyle = fontStyle, fontWeight = fontWeight, fontFamily = fontFamily,
        letterSpacing = letterSpacing, textDecoration = textDecoration, textAlign = textAlign,
        lineHeight = lineHeight, overflow = overflow, softWrap = softWrap, maxLines = maxLines,
        minLines = minLines, onTextLayout = onTextLayout, style = style,
    )
}
