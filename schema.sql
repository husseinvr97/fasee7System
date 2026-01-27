-- ============================================
-- STUDENT PERFORMANCE TRACKER - SQLite Schema
-- ============================================

-- Enable foreign key constraints
PRAGMA foreign_keys = ON;

-- ============================================
-- 1. USER MANAGEMENT
-- ============================================

CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK(role IN ('ADMIN', 'ASSISTANT')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT 1
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);

-- ============================================
-- 2. STUDENT MANAGEMENT
-- ============================================

CREATE TABLE IF NOT EXISTS students (
    student_id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name VARCHAR(200) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    whatsapp_number VARCHAR(20),
    parent_phone_number VARCHAR(20) NOT NULL,
    parent_whatsapp_number VARCHAR(20),
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'ARCHIVED')),
    archived_at TIMESTAMP,
    archived_by INTEGER,
    FOREIGN KEY (archived_by) REFERENCES users(user_id)
);

CREATE INDEX idx_students_status ON students(status);
CREATE INDEX idx_students_registration_date ON students(registration_date);
CREATE INDEX idx_students_phone ON students(phone_number);

-- ============================================
-- 3. LESSON MANAGEMENT
-- ============================================

CREATE TABLE IF NOT EXISTS lessons (
    lesson_id INTEGER PRIMARY KEY AUTOINCREMENT,
    lesson_date DATE NOT NULL,
    month_group VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER NOT NULL,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE INDEX idx_lessons_date ON lessons(lesson_date);
CREATE INDEX idx_lessons_month_group ON lessons(month_group);

CREATE TABLE IF NOT EXISTS lesson_topics (
    topic_id INTEGER PRIMARY KEY AUTOINCREMENT,
    lesson_id INTEGER NOT NULL,
    category VARCHAR(50) NOT NULL CHECK(category IN ('نحو', 'أدب', 'قصة', 'تعبير', 'نصوص', 'قراءة')),
    specific_topic TEXT,
    FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE
);

CREATE INDEX idx_lesson_topics_lesson ON lesson_topics(lesson_id);
CREATE INDEX idx_lesson_topics_category ON lesson_topics(category);

-- ============================================
-- 4. ATTENDANCE
-- ============================================

CREATE TABLE IF NOT EXISTS attendance (
    attendance_id INTEGER PRIMARY KEY AUTOINCREMENT,
    lesson_id INTEGER NOT NULL,
    student_id INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL CHECK(status IN ('PRESENT', 'ABSENT')),
    marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    marked_by INTEGER NOT NULL,
    FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (marked_by) REFERENCES users(user_id),
    UNIQUE(lesson_id, student_id)
);

CREATE INDEX idx_attendance_lesson ON attendance(lesson_id);
CREATE INDEX idx_attendance_student ON attendance(student_id);
CREATE INDEX idx_attendance_status ON attendance(status);

-- ============================================
-- 5. HOMEWORK
-- ============================================

CREATE TABLE IF NOT EXISTS homework (
    homework_id INTEGER PRIMARY KEY AUTOINCREMENT,
    lesson_id INTEGER NOT NULL,
    student_id INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL CHECK(status IN ('DONE', 'PARTIALLY_DONE', 'NOT_DONE')),
    marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    marked_by INTEGER NOT NULL,
    FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (marked_by) REFERENCES users(user_id),
    UNIQUE(lesson_id, student_id)
);

CREATE INDEX idx_homework_lesson ON homework(lesson_id);
CREATE INDEX idx_homework_student ON homework(student_id);

-- ============================================
-- 6. QUIZZES
-- ============================================

CREATE TABLE IF NOT EXISTS quizzes (
    quiz_id INTEGER PRIMARY KEY AUTOINCREMENT,
    lesson_id INTEGER NOT NULL,
    quiz_pdf_data BLOB,
    total_marks DECIMAL(5,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER NOT NULL,
    FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE INDEX idx_quizzes_lesson ON quizzes(lesson_id);

CREATE TABLE IF NOT EXISTS quiz_questions (
    question_id INTEGER PRIMARY KEY AUTOINCREMENT,
    quiz_id INTEGER NOT NULL,
    question_number INTEGER NOT NULL,
    question_type VARCHAR(20) NOT NULL CHECK(question_type IN ('MCQ', 'ESSAY')),
    category VARCHAR(50) NOT NULL CHECK(category IN ('نحو', 'أدب', 'قصة', 'تعبير', 'نصوص', 'قراءة')),
    points DECIMAL(5,2) NOT NULL,
    model_answer TEXT,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE
);

CREATE INDEX idx_quiz_questions_quiz ON quiz_questions(quiz_id);
CREATE INDEX idx_quiz_questions_category ON quiz_questions(category);

CREATE TABLE IF NOT EXISTS quiz_scores (
    score_id INTEGER PRIMARY KEY AUTOINCREMENT,
    quiz_id INTEGER NOT NULL,
    student_id INTEGER NOT NULL,
    question_id INTEGER NOT NULL,
    points_earned DECIMAL(5,2) NOT NULL,
    entered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    entered_by INTEGER NOT NULL,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES quiz_questions(question_id) ON DELETE CASCADE,
    FOREIGN KEY (entered_by) REFERENCES users(user_id),
    UNIQUE(quiz_id, student_id, question_id)
);

CREATE INDEX idx_quiz_scores_quiz ON quiz_scores(quiz_id);
CREATE INDEX idx_quiz_scores_student ON quiz_scores(student_id);

CREATE TABLE IF NOT EXISTS quiz_category_totals (
    total_id INTEGER PRIMARY KEY AUTOINCREMENT,
    quiz_id INTEGER NOT NULL,
    student_id INTEGER NOT NULL,
    category VARCHAR(50) NOT NULL CHECK(category IN ('نحو', 'أدب', 'قصة', 'تعبير', 'نصوص', 'قراءة')),
    points_earned DECIMAL(5,2) NOT NULL,
    total_points DECIMAL(5,2) NOT NULL,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    UNIQUE(quiz_id, student_id, category)
);

CREATE INDEX idx_quiz_category_totals_student ON quiz_category_totals(student_id);

-- ============================================
-- 7. BEHAVIORAL INCIDENTS
-- ============================================

CREATE TABLE IF NOT EXISTS behavioral_incidents (
    incident_id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    lesson_id INTEGER NOT NULL,
    incident_type VARCHAR(50) NOT NULL CHECK(incident_type IN ('LATE', 'DISRESPECTFUL', 'LEFT_EARLY', 'OTHER')),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE INDEX idx_behavioral_incidents_student ON behavioral_incidents(student_id);
CREATE INDEX idx_behavioral_incidents_lesson ON behavioral_incidents(lesson_id);
CREATE INDEX idx_behavioral_incidents_type ON behavioral_incidents(incident_type);

-- ============================================
-- 8. CONSECUTIVITY TRACKING
-- ============================================

CREATE TABLE IF NOT EXISTS consecutivity_tracking (
    tracking_id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    tracking_type VARCHAR(50) NOT NULL CHECK(tracking_type IN ('ABSENCE', 'BEHAVIORAL_INCIDENT')),
    consecutive_count INTEGER DEFAULT 0,
    last_lesson_id INTEGER,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (last_lesson_id) REFERENCES lessons(lesson_id),
    UNIQUE(student_id, tracking_type)
);

CREATE INDEX idx_consecutivity_student ON consecutivity_tracking(student_id);

-- ============================================
-- 9. WARNINGS
-- ============================================

CREATE TABLE IF NOT EXISTS warnings (
    warning_id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    warning_type VARCHAR(50) NOT NULL CHECK(warning_type IN ('CONSECUTIVE_ABSENCE', 'BEHAVIORAL', 'ARCHIVED')),
    warning_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT 1,
    resolved_at TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

CREATE INDEX idx_warnings_student ON warnings(student_id);
CREATE INDEX idx_warnings_active ON warnings(is_active);

-- ============================================
-- 10. MISSIONS
-- ============================================

CREATE TABLE IF NOT EXISTS missions (
    mission_id INTEGER PRIMARY KEY AUTOINCREMENT,
    lesson_id INTEGER NOT NULL,
    mission_type VARCHAR(50) NOT NULL CHECK(mission_type IN ('ATTENDANCE_HOMEWORK', 'QUIZ_GRADING')),
    assigned_to INTEGER NOT NULL,
    assigned_by INTEGER NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'IN_PROGRESS' CHECK(status IN ('IN_PROGRESS', 'COMPLETED')),
    completed_at TIMESTAMP,
    FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_to) REFERENCES users(user_id),
    FOREIGN KEY (assigned_by) REFERENCES users(user_id)
);

CREATE INDEX idx_missions_lesson ON missions(lesson_id);
CREATE INDEX idx_missions_assigned_to ON missions(assigned_to);
CREATE INDEX idx_missions_status ON missions(status);

CREATE TABLE IF NOT EXISTS mission_drafts (
    draft_id INTEGER PRIMARY KEY AUTOINCREMENT,
    mission_id INTEGER NOT NULL UNIQUE,
    draft_data TEXT NOT NULL,
    last_saved TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mission_id) REFERENCES missions(mission_id) ON DELETE CASCADE
);

-- ============================================
-- 11. UPDATE REQUESTS
-- ============================================

CREATE TABLE IF NOT EXISTS update_requests (
    request_id INTEGER PRIMARY KEY AUTOINCREMENT,
    request_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id INTEGER NOT NULL,
    requested_changes TEXT NOT NULL,
    requested_by INTEGER NOT NULL,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK(status IN ('PENDING', 'APPROVED', 'APPLIED', 'REJECTED', 'COMPLETED', 'FAILED', 'BLOCKED')),
    reviewed_by INTEGER,
    reviewed_at TIMESTAMP,
    review_notes TEXT,
    FOREIGN KEY (requested_by) REFERENCES users(user_id),
    FOREIGN KEY (reviewed_by) REFERENCES users(user_id)
);

CREATE INDEX idx_update_requests_status ON update_requests(status);
CREATE INDEX idx_update_requests_requested_by ON update_requests(requested_by);

-- ============================================
-- 12. PERFORMANCE INDICATORS
-- ============================================

CREATE TABLE IF NOT EXISTS performance_indicators (
    pi_id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    category VARCHAR(50) NOT NULL CHECK(category IN ('نحو', 'أدب', 'قصة', 'تعبير', 'نصوص', 'قراءة')),
    quiz_id INTEGER NOT NULL,
    correct_answers INTEGER NOT NULL,
    wrong_answers INTEGER NOT NULL,
    pi_value INTEGER NOT NULL,
    cumulative_pi INTEGER NOT NULL,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE
);

CREATE INDEX idx_pi_student ON performance_indicators(student_id);
CREATE INDEX idx_pi_category ON performance_indicators(category);
CREATE INDEX idx_pi_quiz ON performance_indicators(quiz_id);

-- ============================================
-- 13. TARGETS
-- ============================================

CREATE TABLE IF NOT EXISTS targets (
    target_id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    category VARCHAR(50) NOT NULL CHECK(category IN ('نحو', 'أدب', 'قصة', 'تعبير', 'نصوص', 'قراءة')),
    target_pi_value INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_achieved BOOLEAN DEFAULT 0,
    achieved_at TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

CREATE INDEX idx_targets_student ON targets(student_id);
CREATE INDEX idx_targets_achieved ON targets(is_achieved);

CREATE TABLE IF NOT EXISTS target_achievement_streak (
    streak_id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL UNIQUE,
    current_streak INTEGER DEFAULT 0,
    last_achievement_at TIMESTAMP,
    total_points_earned INTEGER DEFAULT 0,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

-- ============================================
-- 14. FASEE7 TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS fasee7_points (
    points_id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL UNIQUE,
    quiz_points DECIMAL(10,2) DEFAULT 0,
    attendance_points INTEGER DEFAULT 0,
    homework_points INTEGER DEFAULT 0,
    target_points INTEGER DEFAULT 0,
    total_points DECIMAL(10,2) DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

CREATE INDEX idx_fasee7_total ON fasee7_points(total_points DESC);

CREATE TABLE IF NOT EXISTS fasee7_snapshots (
    snapshot_id INTEGER PRIMARY KEY AUTOINCREMENT,
    snapshot_date DATE NOT NULL,
    snapshot_data TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_fasee7_snapshots_date ON fasee7_snapshots(snapshot_date);

-- ============================================
-- 15. NOTIFICATIONS
-- ============================================

CREATE TABLE IF NOT EXISTS notifications (
    notification_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(is_read);

-- ============================================
-- 16. MONTHLY REPORTS
-- ============================================

CREATE TABLE IF NOT EXISTS monthly_reports (
    report_id INTEGER PRIMARY KEY AUTOINCREMENT,
    report_month VARCHAR(20) NOT NULL,
    report_data TEXT NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    generated_by INTEGER NOT NULL,
    FOREIGN KEY (generated_by) REFERENCES users(user_id)
);

CREATE INDEX idx_monthly_reports_month ON monthly_reports(report_month);

-- ============================================
-- INSERT DEFAULT ADMIN USER
-- ============================================
-- Password: "admin123" (BCrypt hashed)
-- You should change this after first login!

INSERT INTO users (username, password_hash, full_name, role) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J7qYhLjYkIvFprXyzvEkYQHKQVqVEO', 'System Admin', 'ADMIN');

-- ============================================
-- SCHEMA CREATION COMPLETE
-- ============================================