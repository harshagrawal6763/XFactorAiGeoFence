# XFactorAiGeoFence


Now when i started thinking about the solution to develop the case study, i went through google's articles on how to implement the
geofencing in Android. I found that there is a geofencing api which is a part of google's android gms library.

I implemented the solution as per the article keeping in the problem statement in mind. First i wanted to get a trigger event,
whenever i entered or exited the geofence. After implementing as per the article , i was unable to get events, 
as the location was not getting updated. I thought about it for a while, and understood that we would require some kind of location update,
either in background or in foreground. So I decided on using two approaches, one with UI which had google map implementation,
and other one where I would run a foreground service to update user's location. 

Solution Details

Architecture pattern : MVVM

Design Patterns used : Builder Design

Geofence Functionality with Google Maps - User Flow

Flow Overview

In this scenario, the app allows users to create a geofence around any location they select on a map. The app utilizes Google Maps to display the user’s current location, allowing them to place custom geofences by long-pressing on specific map points. This flow also includes permissions, GPS checks, and geofence event handling.


Step-by-Step User Flow

1. User Clicks “Set Geofence on Map” Button:
    - On the home screen (LandingFragment), the user sees a button labeled “Set Geofence on Map.”

2. Google Map Loading and Current Location Display:
    - The app loads Google Maps within the fragment.

3. Permission Check:
    - If location permissions are already granted: The app skips to the next step.
    - If location permissions are missing: The app displays a permissions dialog, requesting access to fine and background location.
    - The user must grant these permissions to proceed.

4. GPS Check:
    - If GPS is enabled: The app continues without interruption.
    - If GPS is disabled: A prompt appears, asking the user to enable GPS.
    - The user can activate GPS from this prompt and then return to the app to continue.
    - Once permissions are granted and GPS is enabled
    - The user’s current location is displayed with the option to center the map on their position using the “Current Location” button.
5. Geofence Creation via Long Press on Map:
    - The user long-presses on any desired location point on the map, which initiates the creation of a geofence around that point with the fixed radius.
    - The geofence is set to trigger entry and exit events at this selected location.

6. BroadcastReceiver for Geofence Events:
    - The app registers a `GeofenceBroadcastReceiver` to listen for geofence transitions (entry and exit).
    - When the user crosses into or out of the geofence area, the `GeofenceBroadcastReceiver` is triggered.

7. Event Handling and ViewModel Update:
    - The `GeofenceBroadcastReceiver` captures the geofence event and updates a shared `GeoFenceViewModel` with the latest geofence status (e.g., "Entered" or "Exited").
    - The `GeoFenceViewModel` maintains the current geofence status.

8. Real-Time UI Update:
    - Any fragment or activity observing the `GeoFenceViewModel` (like the LandingFragment) displays the updated geofence status to the user in real-time, showing messages such as “Geofence Entered” or “Geofence Exited.”

9. Ongoing Geofence Monitoring:
    - This approach does not require a continuous foreground service. Instead, Google Maps maintains location updates, and the geofence is managed through the Google Location API, minimizing battery usage.

![Map Flow](https://github.com/harshagrawal6763/XFactorAiGeoFence/blob/main/gmap/GoogleMapsFlow.png)

[View the gmap folder](https://github.com/harshagrawal6763/XFactorAiGeoFence/tree/main/gmap)

This folder also contains a video that demonstrates the event trigger when the user enters the geofence.

![Map Flow](https://github.com/harshagrawal6763/XFactorAiGeoFence/blob/main/gmap/gMapVideo.mp4)



Additional Notes
- Location Customization: This flow provides users with full control to set their preferred geofence location, enhancing flexibility and usability.

This approach offers an interactive and flexible user experience by allowing users to select custom geofence locations directly on a map, providing precise geofence setup and real-time feedback on transitions.


Now if we use the second approach, that matches the description of the use case,

This approach allows users to initiate geofence tracking with just a button press, while managing necessary permissions, location settings, and background updates. Below is a detailed flow of how the app ensures smooth and uninterrupted geofence functionality.


Step-by-Step User Flow
1. User Clicks “Start Geofence” Button:
    - On the home screen (ServiceLocationFragment), the user sees a button labeled “Start Geofence.”
    - When clicked, this button initiates a sequence of checks to ensure that location permissions and settings are correctly configured.

2. Permission Check:
    - If location permissions are already granted: The app skips to the next step.
    - If location permissions are missing: The app displays a permissions dialog, requesting access to fine and background location.
    - The user must grant these permissions to proceed.

3. GPS Check:
    - If GPS is enabled: The app continues without interruption.
    - If GPS is disabled: A prompt appears, asking the user to enable GPS.
    - The user can then activate GPS directly from this prompt, returning to the app to continue the flow.

4. Service Initiation:
    - Once permissions are granted and GPS is enabled, the app starts a foreground service.
    - This service handles background location updates and initiates the geofence setup process.

5. Geofence Setup and BroadcastReceiver Registration:
    - Within the service, the app registers a geofence at a specified location which is added in GeoConsts file.
    - The service also registers a `GeofenceBroadcastReceiver` to listen for geofence events.
    - The `GeofenceBroadcastReceiver` is set to handle two events: when the user enters or exits the geofence area.

6. Geofence Event Handling:
    - When a geofence event occurs (entry or exit), the `GeofenceBroadcastReceiver` captures the event and updates the app’s shared `GeoFenceViewModel` with the latest geofence status.
    - The `GeoFenceViewModel` holds the geofence status (e.g., "Entered" or "Exited" or “Dwell”)

7. UI Update (Real-Time Feedback to User):
    - Any fragment observing the `GeoFenceViewModel` (like ServiceLocationFragment) immediately reflects the geofence status change.
    - This allows the user to see real-time updates on their screen, such as "Geofence Entered" or "Geofence Exited."

8. Background Operation:
    - Even if the app is minimized, the foreground service continues running to maintain geofence monitoring.
    - The service stops either when the user manually turns off geofencing or when the app is killed.


![Service Flow](https://github.com/harshagrawal6763/XFactorAiGeoFence/blob/main/service/LocationServiceUserFlow.png)

[View the service folder](https://github.com/harshagrawal6763/XFactorAiGeoFence/tree/main/service)


Room Database is used in this application to save the events that are triggered while the user enters/exists or stays in the fence.
I have used room database as according to the case study document, it was suggested that we can use database to mimic api calls.

For updating the default location and radius, please update com.harsh.geofence.consts.GeoConsts file. 
Please update the location in case change is needed. I would set it to XFactr.Ai's default address for now.

Using the first scenario won't need the default lat long as the location is set according to user's selection.

Please reach out if you face any issues while implementing or using this.

