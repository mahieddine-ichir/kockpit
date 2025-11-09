# Module description
An Audit Notification starter (via Spring boot starters logic) offers a way to notify audits 
(through a broker, a console or else).

To provide a new starter, on should provide a Bean that implements the 
`org.kockpit.audit.api.AuditReportNotificationService` class.
