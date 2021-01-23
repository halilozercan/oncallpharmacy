# OnCall Pharmacy Map Application (Turkey)

I developed an API for querying OnCall pharmacies in Turkey with respect to city and district a while back for a completely unrelated reason to this repository. 
API is actually as simple as it sounds. 

Authors: [@halilozercan](https://github.com/halilozercan) [@cagatayemekci](https://github.com/cagatayemekci)

```
GET https://eczane.turqu.net/{cityLicensePlateId}

RETURNS 200
[{
    "address": "Yıldırım Mahallesi, Dr.Neslihan Özenli Caddesi, No:26/A Akyurt/ Ankara"
    "city_name": "Ankara"
    "latitude": 40.128034
    "longitude": 33.08795
    "name": "Nur Eczanesi"
    "notes": ""
    "phone": "0 (312) 844-00-14"
},
{	
    "address": "Hükümet Caddesi, Mahmut Atalay Sokak, No:5/1 Ulus Altındağ / Ankara"
    "city_name": "Ankara"
    "latitude": 39.9423578
    "longitude": 32.8569794
    "name": "Çorum Eczanesi"
    "notes": "
    "phone": "0 (312) 324-13-37"
}, ...]
```  

Luckily, this API ignited another idea that would eventually get me to finally give Kotlin Mobile Multiplatform(KMM) a try.
However, first I needed another endpoint on my new API that would let me query pharmacies according to location proximity as well.
So, I re-indexed my original database on backend to accommodate for latitude-longitude based searching.

```
GET https://eczane.turqu.net/location?lat=39.02&lng=32.0

RETURNS 200
[{
    "address": "Fatih Mahallesi, Mevlana Caddesi, No:27/D Çeltik"
    "city_id": 42
    "city_name": "Konya"
    "distance": 18.045276942151773
    "latitude": 39.02137
    "longitude": 31.79119
    "name": "Yeşil Çeltik Eczanesi"
    "notes": ""
    "phone": "0 (332) 871-25-18"
},
{
    "address": "Yeni Mahalle Bademlik Caddesi No:6B Yunak / Konya"
    "city_id": 42
    "city_name": "Konya"
    "distance": 32.45241342177606
    "latitude": 38.814301
    "longitude": 31.734067
    "name": "Kurtoğlu Eczanesi"
    "notes": ""
    "phone": "0 (332) 851-21-15"
}, ...]
```

Now, I had everything ready on backend side to develop a proper KMM application.

## What is shared?

First, I wanted to have this codebase share almost everything from presentation layer to domain layer. However, I knew that iosApp and androidApp were going to be developed by
different people. So, instead of forcing ourselves to work every detail in the shared code to make sure ViewModels are reusable in SwiftUI and Jetpack Compose, 
we decided to only share crucial backend and local caching features and implement the navigation and ViewModels in their each app's native code.

### Domain Layer Sharing

Both applications use the same code when it comes to fetching and caching oncall pharmacies on country's map. It doesn't make sense to replicate this logic in both iOS and Android, so 
we implemented the map travel and place rendering logic in `shared` module.

Both Apple Maps and Google Maps have similar APIs that made it easy to implement a common repository that provides the functionality for map browsing.

1. Map should be zoomed at least onto a city to start fetching available pharmacies in that location window.
  1.1. Common repository does not decide on whether zoom level is sufficient because Apple Maps and Google Maps might use different scaling factors.
  1.2. Both apps are responsible for figuring out whether zoom level is acceptable to fetch pharmacies in near proximity. 
2. The whole country is divided into around 200 squares of longitude and latitude lines. 
  2.1. When a location window is passed into repository, all squares which belong in the window are calculated.
  2.2. Pharmacies are queried from local database according to squares that they belong.
  2.3. Local database has 10 minutes of cache window. If available data is older than 10 minutes, a new request is made to the backend to refresh local database.
3. Shared Repository is optimized according to map browsing experience.
  3.1. Zooming in, where requested squares were already part of the previous request, does not execute a new query on local database.
  3.2. If a central point, e.g. user's current location, is passed the repository, results are ordered by distance to this central point.
  
### Shared pieces

- Dependency Injection modules (Koin)
- Local Database (sqldelight)
- Remote API (ktor)
- Repository Pattern
- Domain Model

## How does app look like?

Here is a demo of Android Application.

[![Android App Demo Video](https://img.youtube.com/vi/TEpAg6cHo_E/0.jpg)](https://www.youtube.com/watch?v=TEpAg6cHo_E)
