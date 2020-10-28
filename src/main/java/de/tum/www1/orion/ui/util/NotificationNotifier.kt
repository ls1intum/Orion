package de.tum.www1.orion.ui.util

import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * Inform the user about something using the notification balloon (bottom right corner). It is logged so the user can
 * open it later
 */
@Service
class NotificationNotifier(val project: Project) {
    private val notificationGroup = NotificationGroup("Orion Errors", NotificationDisplayType.BALLOON, true)

    fun notify(content: String, type: NotificationType = NotificationType.ERROR): Notification {
        val notification: Notification = notificationGroup.createNotification(content, type)
        notification.notify(project)
        return notification
    }
}

fun Project.notify(message: String, type: NotificationType = NotificationType.ERROR) {
    this.service<NotificationNotifier>().notify(message, type)
}