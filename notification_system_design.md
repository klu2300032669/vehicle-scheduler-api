# Notification System Design

## Stage 1

**API Endpoints:**

- GET /notifications - get all notifications for a student
- GET /notifications/unread - get only unread notifications  
- PUT /notifications/{id}/read - mark one notification as read
- GET /notifications/count - get number of unread notifications

**Real-time:** I will use WebSocket. When a new notification comes, server pushes it directly to student's browser.

**Response format:**
{
  "id": "abc-123",
  "type": "Placement",
  "message": "Amazon hiring",
  "isRead": false,
  "createdAt": "2026-05-30 12:00:00"
}

## Stage 2

I will use PostgreSQL because it is reliable for this type of data.

**Tables:**
students (id, name, roll_no, email)
notifications (id, student_id, type, message, is_read, created_at)

When data grows large, I will add indexes and partition by date.

## Stage 3

The query is slow because there is no index. Database scans all rows.

**Fix:** Add index on (student_id, is_read, created_at)

**Query for placement notifications in last 7 days:**
SELECT DISTINCT s.* FROM students s JOIN notifications n ON s.id = n.student_id WHERE n.type = 'Placement' AND n.created_at >= NOW() - INTERVAL '7 days';

## Stage 4

I will use Redis cache to store unread notifications for 1 minute. This reduces DB load. Pagination also helps - load only 20 at a time.

## Stage 5

The problem is if email fails for 200 students, everything stops. I will use a message queue. Save to DB first, then send emails asynchronously with retry for failures.

## Stage 6

**Priority formula:** Placement(3) > Result(2) > Event(1) + recency (newer = higher)

**Code to find top 10:**
public List<Notification> getTop10(List<Notification> list) {
    list.sort((a,b) -> {
        int scoreA = getWeight(a.type) - daysSince(a.timestamp);
        int scoreB = getWeight(b.type) - daysSince(b.timestamp);
        return scoreB - scoreA;
    });
    return list.subList(0, Math.min(10, list.size()));
}

To maintain top 10 efficiently, use min-heap.
