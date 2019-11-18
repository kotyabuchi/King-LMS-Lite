package com.github.kabocchi.king_LMS_Lite

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.github.kabocchi.king_LMS_Lite.Utility.decryptFile2
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.security.GeneralSecurityException
import java.util.*

class GoogleCalendar {

    private val APPLICATION_NAME = "King-LMS Lite"
    private val JSON_FACTORY = JacksonFactory.getDefaultInstance()
    private val TOKENS_DIRECTORY_PATH = "tokens"

    private val SCOPES = listOf(CalendarScopes.CALENDAR)
    private val CREDENTIALS_FILE_PATH = "credentials.json"

    private lateinit var service: Calendar

    private var registeredEvents: JsonObject

    init {
        println("GoogleCalendar Init")
        registeredEvents = if (File(EVENTS_FILE_PATH).exists()) {
            Json.parse(decryptFile2(EVENTS_FILE_PATH)).asObject()
        } else {
            JsonObject()
        }
        try {
            val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
            service = Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
        }
        getAllEvents("KCG Reports", "KCG Reports 通知用")
    }
    
    @Throws(IOException::class)
    private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
        // Load client secrets.
        val `in` = ClassLoader.getSystemResourceAsStream(CREDENTIALS_FILE_PATH) ?: throw FileNotFoundException(
                "Resource not found: $CREDENTIALS_FILE_PATH"
        )
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))
        
        // Build flow and trigger user authorization request.
        val authorizationBuilder = (GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES))
                .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
        authorizationBuilder.accessType = "offline"
        val flow = authorizationBuilder.build()
        val receiverBuilder = LocalServerReceiver.Builder()
        receiverBuilder.port = 8888
        val receiver = receiverBuilder.build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }
    
    @Throws(IOException::class, GeneralSecurityException::class)
    fun registerEvent(calendarName: String, eventName: String, start: DateTime, end: DateTime, reminderMap: Map<ReminderType, Int>) {
        val startDateTime = EventDateTime().setDateTime(start).setTimeZone("Asia/Tokyo")
        val endDateTime = EventDateTime().setDateTime(end).setTimeZone("Asia/Tokyo")
        val reminders = ArrayList<EventReminder>()
        for (type in reminderMap.keys) reminders.add(EventReminder().setMethod(type.apiName).setMinutes(reminderMap[type]))
        
        val report = Event()
        report.summary = eventName
        report.start = startDateTime
        report.end = endDateTime
        if (reminderMap.isNotEmpty()) report.reminders = Event.Reminders().setUseDefault(false).setOverrides(reminders)
        val calendarId = getCalendarId(calendarName, true)
        if (calendarId == null) {
            println("CalendarId is null")
        } else {
            val registered = service.events().insert(calendarId, report).execute()
            registeredEvents.add(eventName, registered.id)
        }
    }
    
    @Throws(IOException::class, GeneralSecurityException::class)
    fun registerEvent(calendarName: String, event: com.github.kabocchi.king_LMS_Lite.Event) {
        registerEvent(calendarName, event.summary, event.startTime, event.endTime, event.reminders)
    }
    
    @Throws(IOException::class)
    internal fun getCalendarId(calendarName: String, create: Boolean): String? {
        var pageToken: String? = null
        do {
            val calendarList = service.calendarList().list().setPageToken(pageToken).execute()
            val items = calendarList.items
            
            for (calendarListEntry in items) {
                if (calendarListEntry.summary.equals(calendarName, ignoreCase = true)) {
                    return calendarListEntry.id
                }
            }
            pageToken = calendarList.nextPageToken
        } while (pageToken != null)
        if (create) {
            println("Calendar Create")
            return createCalendar(calendarName)
        }
        return null
    }
    
    @Throws(IOException::class)
    internal fun createCalendar(calendarName: String?): String {
        val calendar = com.google.api.services.calendar.model.Calendar()
        calendar.summary = calendarName
        calendar.timeZone = "Asia/Tokyo"
        val createdCalendar = service.calendars().insert(calendar).execute()
        println("Calendar created! ID: " + createdCalendar.id)
        return createdCalendar.id
    }

    fun hasEvent(summary: String): Boolean {
        return registeredEvents.getString(summary, "") != ""
    }
    
    private fun getAllEvents(vararg calendarNames: String) {
        var pageToken: String? = null
        for (calendarName in calendarNames) {
            val calendarId = getCalendarId(calendarName, true)
            do {
                val events = service.events().list(calendarId).setPageToken(pageToken).execute()
                val items = events.items
                for (event in items) {
                    registeredEvents[event.summary] = event.id
                    println(event.summary)
                }
                pageToken = events.nextPageToken
            } while (pageToken != null)
        }
    }
}
