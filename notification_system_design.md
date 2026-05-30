# Campus Notifications Microservice — System Design
Roll Number: 2300032669
Name: SURUBHOTLA SRI RAM SAKETH

Stage 1 — REST API Design

The notification system allows students to receive updates about Placements, Results, and Events.

Endpoints I designed:

GET /api/notifications - Fetch notifications for a student with pagination
POST /api/notifications - Admin creates and sends notification
PUT /api/notifications/:id/read - Mark notification as read
GET /api/notifications/priority-inbox - Get top n unread notifications

Real-Time Mechanism: I used Server-Sent Events (SSE) because notifications are one-way from server to client. WebSockets are overkill.

Stage 2 — Database Design

I chose PostgreSQL because data has fixed structure.

Tables:
- students: id, roll_no, name, email
- notifications: id, student_id, type, message, is_read, created_at

SQL Query Example:
SELECT id, type, message FROM notifications WHERE student_id = 2300032669 AND is_read = false ORDER BY created_at DESC LIMIT 10;

Stage 3 — Query Optimization

The query is slow because there is no index on student_id and is_read.

Fix: CREATE INDEX idx_student_read ON notifications(student_id, is_read, created_at DESC);

Indexing every column is bad because it slows down writes.

Query for placement notifications last 7 days:
SELECT DISTINCT s.* FROM students s JOIN notifications n ON s.id = n.student_id WHERE n.type = 'Placement' AND n.created_at >= NOW() - INTERVAL '7 days';

Stage 4 — Performance

Problems: DB gets overwhelmed with repeated queries.

Solutions:
1. Redis cache with 2 minute TTL
2. Pagination - load 20 at a time
3. Connection pooling

Stage 5 — Fault Tolerance

Problems: No error handling, one failure stops everything, email and DB together.

Should email and DB happen together? No. Save to DB first, then send email separately.

Redesigned code:
for each student in students:
    try:
        save_to_db(student)
        push_to_app(student)
    catch error:
        log_error(student)
    try:
        send_email(student)
    catch error:
        retry_queue.push(student)

Stage 6 — Priority Inbox

Priority formula: Placement(3) > Result(2) > Event(1) and newer notifications come first.

Java code:
public List<Notification> getTop10(List<Notification> list) {
    list.sort((a,b) -> {
        int scoreA = getWeight(a.type);
        int scoreB = getWeight(b.type);
        if(scoreA == scoreB) {
            return b.timestamp.compareTo(a.timestamp);
        }
        return scoreB - scoreA;
    });
    return list.subList(0, Math.min(10, list.size()));
}

To maintain top 10 efficiently, use Min-Heap of size 10.
