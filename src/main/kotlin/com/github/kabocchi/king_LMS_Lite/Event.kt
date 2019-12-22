package com.github.kabocchi.king_LMS_Lite

import com.google.api.client.util.DateTime

class Event(summary: String, startTime: DateTime, endTime: DateTime, reminders: Map<ReminderType, Int>) {

    var summary: String = summary
    var startTime: DateTime = startTime
    var endTime: DateTime = endTime
    var reminders: Map<ReminderType, Int> = reminders
}
