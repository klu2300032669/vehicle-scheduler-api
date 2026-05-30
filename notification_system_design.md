
# Notification System Design - Stage 1 to 6

## Stage 1
REST APIs:
- GET /api/notifications - Get student notifications
- PUT /api/notifications/{id}/read - Mark as read
Real-time: Use WebSocket

## Stage 2
Use PostgreSQL. Good for large data.

## Stage 3
Add index on (student_id, is_read, created_at)

## Stage 4
Use Redis cache for unread notifications.

## Stage 5
Use queue (RabbitMQ) for sending emails to avoid failure in middle.

## Stage 6
Priority = (Type weight) * recency. 
Placement = 3, Result = 2, Event = 1.
