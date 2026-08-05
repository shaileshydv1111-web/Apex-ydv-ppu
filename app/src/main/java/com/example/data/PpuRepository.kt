package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PpuRepository(private val dao: PpuDao) {

    val allNotices: Flow<List<NoticeEntity>> = dao.getAllNotices()
    val allResults: Flow<List<ResultEntity>> = dao.getAllResults()
    val allPyqs: Flow<List<PyqEntity>> = dao.getAllPyqs()
    val allAdmissions: Flow<List<AdmissionEntity>> = dao.getAllAdmissions()
    val allScholarships: Flow<List<ScholarshipEntity>> = dao.getAllScholarships()
    val allBanners: Flow<List<BannerEntity>> = dao.getAllBanners()
    val allNotifications: Flow<List<UserNotificationEntity>> = dao.getAllNotifications()

    val bookmarkedNotices: Flow<List<NoticeEntity>> = dao.getBookmarkedNotices()
    val bookmarkedResults: Flow<List<ResultEntity>> = dao.getBookmarkedResults()
    val bookmarkedPyqs: Flow<List<PyqEntity>> = dao.getBookmarkedPyqs()

    // Bookmark actions
    suspend fun syncNotices(notices: List<NoticeEntity>) {
        if (notices.isNotEmpty()) {
            dao.clearUnbookmarkedNotices()
            dao.insertNotices(notices)
        }
    }

    suspend fun toggleNoticeBookmark(id: String, isBookmarked: Boolean) {
        dao.updateNoticeBookmark(id, isBookmarked)
    }

    suspend fun toggleResultBookmark(id: String, isBookmarked: Boolean) {
        dao.updateResultBookmark(id, isBookmarked)
    }

    suspend fun togglePyqBookmark(id: String, isBookmarked: Boolean) {
        dao.updatePyqBookmark(id, isBookmarked)
    }

    // Insert actions (Admin & Local)
    suspend fun insertNotice(notice: NoticeEntity) {
        dao.insertNotice(notice)
        // Auto trigger user notification for new notice
        dao.insertNotification(
            UserNotificationEntity(
                id = "notif_${System.currentTimeMillis()}",
                title = "New Notice Published!",
                body = notice.title,
                date = notice.date,
                type = "Notice"
            )
        )
    }

    suspend fun insertResult(result: ResultEntity) {
        dao.insertResult(result)
        dao.insertNotification(
            UserNotificationEntity(
                id = "notif_${System.currentTimeMillis()}",
                title = "New Result Declared!",
                body = "${result.title} (${result.session})",
                date = result.publishDate,
                type = "Result"
            )
        )
    }

    suspend fun insertPyq(pyq: PyqEntity) {
        dao.insertPyq(pyq)
    }

    suspend fun insertAdmission(admission: AdmissionEntity) {
        dao.insertAdmissions(listOf(admission))
        dao.insertNotification(
            UserNotificationEntity(
                id = "notif_${System.currentTimeMillis()}",
                title = "New Admission Notice!",
                body = admission.title,
                date = admission.lastDate,
                type = "Admission"
            )
        )
    }

    suspend fun insertScholarship(scholarship: ScholarshipEntity) {
        dao.insertScholarships(listOf(scholarship))
    }

    suspend fun deleteNotice(id: String) = dao.deleteNotice(id)
    suspend fun deleteResult(id: String) = dao.deleteResult(id)
    suspend fun deletePyq(id: String) = dao.deletePyq(id)

    suspend fun markNotificationAsRead(id: String) = dao.markNotificationAsRead(id)

    suspend fun sendBroadcastNotification(title: String, body: String, type: String) {
        dao.insertNotification(
            UserNotificationEntity(
                id = "notif_admin_${System.currentTimeMillis()}",
                title = title,
                body = body,
                date = "Today",
                type = type
            )
        )
    }

    // Seed database if empty
    suspend fun seedInitialDataIfEmpty() {
        val existingNotices = allNotices.first()
        if (existingNotices.isEmpty()) {
            dao.insertNotices(getInitialNotices())
            dao.insertResults(getInitialResults())
            dao.insertPyqs(getInitialPyqs())
            dao.insertAdmissions(getInitialAdmissions())
            dao.insertScholarships(getInitialScholarships())
            dao.insertBanners(getInitialBanners())
            dao.insertNotification(
                UserNotificationEntity(
                    id = "welcome_1",
                    title = "Welcome to PPU Patna Info!",
                    body = "Get instant updates for Patliputra University Notices, Results, Admit Cards, Syllabus & PYQs.",
                    date = "August 2026",
                    type = "System",
                    isRead = false
                )
            );
        }
    }

    private fun getInitialNotices(): List<NoticeEntity> = listOf(
        NoticeEntity(
            id = "n_latest_1",
            title = "[URGENT] Notice regarding UG (Regular/Vocational) Semester 1, 2, 3 & 4 Examination Form Fill-Up 2026",
            category = "Exam",
            date = "04 Aug 2026",
            description = "Official notification regarding online examination form submission deadline extension and late fee guidelines for UG Semester students.",
            pdfUrl = "https://ppup.ac.in/upload/notices/UG_Sem_Exam_Form_Notice_2026.pdf",
            isImportant = true,
            isBookmarked = false,
            timestamp = System.currentTimeMillis()
        ),
        NoticeEntity(
            id = "n_latest_2",
            title = "[IMPORTANT] Patliputra University PG Semester 2 & 4 End Term Examination Schedule & Admit Card 2026",
            category = "Exam",
            date = "02 Aug 2026",
            description = "Detailed subject-wise exam schedule and admit card downloading link for M.A / M.Sc / M.Com Semester 2 and 4 students.",
            pdfUrl = "https://ppup.ac.in/upload/notices/PG_Exam_Schedule_AdmitCard_2026.pdf",
            isImportant = true,
            isBookmarked = false,
            timestamp = System.currentTimeMillis() - 86400000L
        ),
        NoticeEntity(
            id = "n_latest_3",
            title = "Notice for Online Application & Scrutiny of UG Part 3 Examination Results 2026",
            category = "Exam",
            date = "31 Jul 2026",
            description = "Students seeking re-evaluation or copy scrutiny for Part 3 examinations can apply online on the university portal.",
            pdfUrl = "https://ppup.ac.in/upload/notices/UG_Part3_Scrutiny_Notice_2026.pdf",
            isImportant = false,
            isBookmarked = false,
            timestamp = System.currentTimeMillis() - 172800000L
        ),
        NoticeEntity(
            id = "n_latest_4",
            title = "Instructions for B.Ed & M.Ed Practical Examination Centers Session 2024-26",
            category = "Exam",
            date = "28 Jul 2026",
            description = "Center allocation list and internal assessment submission dates for constituent and affiliated colleges.",
            pdfUrl = "https://ppup.ac.in/upload/notices/BEd_Practical_Notice_2026.pdf",
            isImportant = false,
            isBookmarked = false,
            timestamp = System.currentTimeMillis() - 259200000L
        ),
        NoticeEntity(
            id = "n_latest_5",
            title = "[URGENT] Notice regarding Spot Round Admission Guidelines for Four Year UG Degree Course (Session 2026-30)",
            category = "Admission",
            date = "25 Jul 2026",
            description = "Vacant seats detail and online registration procedure for Spot Admission in B.A, B.Sc, B.Com courses.",
            pdfUrl = "https://ppup.ac.in/upload/notices/UG_Spot_Admission_Notice_2026.pdf",
            isImportant = true,
            isBookmarked = false,
            timestamp = System.currentTimeMillis() - 345600000L
        ),
        NoticeEntity(
            id = "n_latest_6",
            title = "Patliputra University Ph.D Course Work Examination & Viva-Voce Schedule Notice 2026",
            category = "Academic",
            date = "20 Jul 2026",
            description = "Date sheet and departmental instructions for Research Scholars appearing for Ph.D Course Work exam.",
            pdfUrl = "https://ppup.ac.in/upload/notices/PhD_Coursework_Notice_2026.pdf",
            isImportant = false,
            isBookmarked = false,
            timestamp = System.currentTimeMillis() - 43200000L
        )
    )

    private fun getInitialResults(): List<ResultEntity> = listOf(
        ResultEntity(
            id = "r1",
            title = "UG Regular B.A / B.Sc / B.Com Part 3 Final Result 2026",
            course = "UG",
            session = "2023-2026",
            publishDate = "31 Jul 2026",
            pdfUrl = "https://ppup.ac.in/results/UG_Part3_Result_2026.pdf"
        ),
        ResultEntity(
            id = "r2",
            title = "BCA & BBA Vocational Semester 4 Result Session 2024-27",
            course = "Vocational",
            session = "2024-2027",
            publishDate = "26 Jul 2026",
            pdfUrl = "https://ppup.ac.in/results/BCA_BBA_Sem4_2026.pdf"
        ),
        ResultEntity(
            id = "r3",
            title = "M.A / M.Sc / M.Com Semester 3 Examination Result 2026",
            course = "PG",
            session = "2024-2026",
            publishDate = "22 Jul 2026",
            pdfUrl = "https://ppup.ac.in/results/PG_Sem3_Result_2026.pdf"
        ),
        ResultEntity(
            id = "r4",
            title = "B.Ed Semester 2 Examination Result Session 2024-26",
            course = "B.Ed",
            session = "2024-2026",
            publishDate = "18 Jul 2026",
            pdfUrl = "https://ppup.ac.in/results/Bed_Sem2_Result_2026.pdf"
        ),
        ResultEntity(
            id = "r5",
            title = "B.Tech Mechanical & CSE Semester 6 Results 2026",
            course = "B.Tech",
            session = "2022-2026",
            publishDate = "10 Jul 2026",
            pdfUrl = "https://ppup.ac.in/results/BTech_Sem6_2026.pdf"
        )
    )

    private fun getInitialPyqs(): List<PyqEntity> = listOf(
        PyqEntity(
            id = "p1",
            title = "BCA - Object Oriented Programming in C++ & Java",
            course = "BCA",
            department = "Computer Applications",
            year = "2025",
            semester = "Sem 2",
            pdfUrl = "https://ppup.ac.in/pyq/BCA_OOP_2025.pdf"
        ),
        PyqEntity(
            id = "p2",
            title = "B.Sc Physics Honours - Classical Mechanics & Thermodynamics",
            course = "B.Sc",
            department = "Physics",
            year = "2024",
            semester = "Sem 1",
            pdfUrl = "https://ppup.ac.in/pyq/BSc_Physics_2024.pdf"
        ),
        PyqEntity(
            id = "p3",
            title = "B.A History Honours - History of India (Ancient & Medieval)",
            course = "B.A",
            department = "History",
            year = "2025",
            semester = "Sem 3",
            pdfUrl = "https://ppup.ac.in/pyq/BA_History_2025.pdf"
        ),
        PyqEntity(
            id = "p4",
            title = "B.Com Accounts - Corporate Accounting & Business Law",
            course = "B.Com",
            department = "Commerce",
            year = "2024",
            semester = "Sem 2",
            pdfUrl = "https://ppup.ac.in/pyq/BCom_Accounts_2024.pdf"
        ),
        PyqEntity(
            id = "p5",
            title = "M.Sc Mathematics - Real Analysis & Abstract Algebra",
            course = "M.Sc",
            department = "Mathematics",
            year = "2025",
            semester = "Sem 1",
            pdfUrl = "https://ppup.ac.in/pyq/MSc_Maths_2025.pdf"
        ),
        PyqEntity(
            id = "p6",
            title = "BBA - Principles of Management & Business Economics",
            course = "BBA",
            department = "Management",
            year = "2023",
            semester = "Sem 1",
            pdfUrl = "https://ppup.ac.in/pyq/BBA_Management_2023.pdf"
        )
    )

    private fun getInitialAdmissions(): List<AdmissionEntity> = listOf(
        AdmissionEntity(
            id = "a1",
            title = "UG Admission 4-Year CBCS Degree (B.A, B.Sc, B.Com)",
            type = "UG Admission",
            academicYear = "2026-2030",
            lastDate = "15 Aug 2026",
            linkUrl = "https://ppup.ac.in/online_admission_ug.php",
            description = "Online application active for admission in affiliated and constituent colleges of Patliputra University Patna. Entrance / Merit-based allocation."
        ),
        AdmissionEntity(
            id = "a2",
            title = "PG Master Degree Admission (M.A, M.Sc, M.Com)",
            type = "PG Admission",
            academicYear = "2026-2028",
            lastDate = "25 Aug 2026",
            linkUrl = "https://ppup.ac.in/online_admission_pg.php",
            description = "Apply online for Post-Graduate regular programs across university department and constituent Patna / Nalanda colleges."
        ),
        AdmissionEntity(
            id = "a3",
            title = "Ph.D. Entrance Test (PAT) Admission Notification 2026",
            type = "Ph.D Entrance",
            academicYear = "2026-2027",
            lastDate = "10 Sep 2026",
            linkUrl = "https://ppup.ac.in/phd_entrance_2026.php",
            description = "Registration open for PAT 2026 in Sciences, Humanities, Commerce, and Social Sciences faculties."
        )
    )

    private fun getInitialScholarships(): List<ScholarshipEntity> = listOf(
        ScholarshipEntity(
            id = "s1",
            title = "Bihar Post Matric Scholarship (PMS) BC / EBC / SC / ST 2026-27",
            provider = "Govt of Bihar",
            amount = "₹5,000 - ₹18,000 / year",
            deadline = "15 September 2026",
            eligibility = "Domicile of Bihar, Enrolled in PPU college, Family income < ₹3 Lakhs",
            applyUrl = "https://pmsonline.bih.nic.in"
        ),
        ScholarshipEntity(
            id = "s2",
            title = "Mukhyamantri Kanya Utthan Yojana (Graduation Passed Girls)",
            provider = "Bihar Education Dept",
            amount = "₹50,000 one-time",
            deadline = "15 Sep 2026",
            eligibility = "Female students who passed Graduation degree from PPU",
            applyUrl = "https://medhasoft.bih.nic.in"
        ),
        ScholarshipEntity(
            id = "s3",
            title = "National Scholarship Portal (NSP) Central Sector Scheme 2026-27",
            provider = "Govt of India",
            amount = "₹12,000 / year",
            deadline = "31 October 2026",
            eligibility = "Top 20th percentile in Class 12 board, pursuing regular UG degree",
            applyUrl = "https://scholarships.gov.in"
        )
    )

    private fun getInitialBanners(): List<BannerEntity> = listOf(
        BannerEntity(
            id = "b1",
            title = "PPU UG Admission 2026 Active",
            subtitle = "Apply online for B.A, B.Sc, B.Com, BCA CBCS 2026-30",
            imageUrl = "img_ppu_banner",
            actionRoute = "notices"
        ),
        BannerEntity(
            id = "b2",
            title = "Semester Results Declared",
            subtitle = "Check your UG & PG Part 3 Marksheet online now",
            imageUrl = "img_ppu_banner",
            actionRoute = "results"
        ),
        BannerEntity(
            id = "b3",
            title = "Download Previous Year Question Papers",
            subtitle = "Access 5+ years solved PYQs & official syllabus PDFs",
            imageUrl = "img_ppu_banner",
            actionRoute = "pyq"
        )
    )
}
