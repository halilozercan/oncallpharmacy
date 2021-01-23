//
//  MapView.swift
//  iosApp
//
//  Created by Çağatay Emekci on 20.12.2020.
//  Copyright © 2020 orgName. All rights reserved.
//

import SwiftUI
import MapKit
import shared
import Foundation
import Combine

class MapViewContainer: ObservableObject {
    let defaultCoordiante = CLLocationCoordinate2D(latitude: 39, longitude: 42)
    @Published public private(set) var mapView = MKMapView(frame: .zero)
    @Published public private(set) var locationManager: CLLocationManager = CLLocationManager()
}

// MARK: - MKMapViewRepresentable

struct MapView: UIViewRepresentable {
    
    var userTrackingMode: Binding<MKUserTrackingMode>
    var selectLocation: Binding<CLLocationCoordinate2D>
    
    @EnvironmentObject private var mapViewContainer: MapViewContainer
    
    var newLocation: ((Result<CLLocation, Error>) -> Void)?
    var annotations: [MKPointAnnotation]
    var didSelect: (MKPointAnnotation) -> ()
    var removeAllAnnotation: () -> ()
    var regionWillChange: () -> ()
    var regionDidChange: (Coordinates,LocationBounds) -> ()
    
    func makeUIView(context: UIViewRepresentableContext<MapView>) -> MKMapView {
        mapViewContainer.mapView.delegate = context.coordinator
        context.coordinator.followUserIfPossible()
        mapViewContainer.locationManager.requestWhenInUseAuthorization()
        mapViewContainer.locationManager.delegate = context.coordinator
        mapViewContainer.mapView.showsUserLocation = true
        mapViewContainer.mapView.isZoomEnabled = true
        return mapViewContainer.mapView
    }
    
    func updateUIView(_ mapView: MKMapView,
                      context: UIViewRepresentableContext<MapView>) {
        if mapView.userTrackingMode != userTrackingMode.wrappedValue {
            mapView.setUserTrackingMode(userTrackingMode.wrappedValue,
                                        animated: true)
        }
        if mapViewContainer.defaultCoordiante !=  selectLocation.wrappedValue
        {
            mapView.setCenter(selectLocation.wrappedValue, animated: false)
        }
        if annotations.count != mapView.annotations.count {
            mapView.removeAnnotations(mapView.annotations)
            mapView.addAnnotations(annotations)
        }
    }
    
    func makeCoordinator() -> MapViewCoordinator {
        let coordinator = MapViewCoordinator(self)
        return coordinator
    }
    
    func centerMapOnUserLocation(isUserCoordinate: Bool = false) {
        if let coordinate = mapViewContainer.locationManager.location?.coordinate {
            mapViewContainer.mapView.setRegion(MKCoordinateRegion(center: coordinate,
                                                                  span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01)),
                                               animated: false)
        }
    }
    
    // MARK: - Coordinator
    class MapViewCoordinator: NSObject, MKMapViewDelegate, CLLocationManagerDelegate {
        
        var parent: MapView
        var center: Coordinates?
        var bounds: LocationBounds?
        
        init(_ control: MapView) {
            self.parent = control
            
            super.init()
            
            setupLocationManager()
        }
        
        func setupLocationManager() {
            parent.mapViewContainer.locationManager.delegate = self
            parent.mapViewContainer.locationManager.desiredAccuracy = kCLLocationAccuracyBest
            parent.mapViewContainer.locationManager.pausesLocationUpdatesAutomatically = true
        }
        
        func followUserIfPossible() {
            switch CLLocationManager.authorizationStatus() {
            case .authorizedAlways, .authorizedWhenInUse:
                parent.userTrackingMode.wrappedValue = .follow
            default:
                break
            }
        }
        
        private func present(_ alert: UIAlertController,
                             animated: Bool = true,
                             completion: (() -> Void)? = nil) {
            let keyWindow = UIApplication.shared.windows.first { $0.isKeyWindow }
            keyWindow?.rootViewController?.present(alert, animated: animated, completion: completion)
        }
        
        func mapView(_ mapView: MKMapView,
                     regionDidChangeAnimated animated: Bool) {
            center = Coordinates(latitude: mapView.region.center.latitude, longitude: mapView.region.center.longitude)
            bounds = LocationBounds(southWest:
                                        Coordinates(latitude: mapView.region.southWest.latitude,
                                                    longitude: mapView.region.southWest.longitude),
                                    northEast:
                                        Coordinates(latitude: mapView.region.northEast.latitude,
                                                    longitude: mapView.region.northEast.latitude))
            if let center = center,
                let bounds = bounds,
                mapView.zoomLevel > 12 {
                self.parent.mapViewContainer.mapView.setRegion(mapView.region, animated: true)
                self.parent.regionDidChange(center, bounds)
            } else {
                let annotations =  self.parent.mapViewContainer.mapView.annotations.filter({ !($0 is MKUserLocation) })
                self.parent.mapViewContainer.mapView.removeAnnotations(annotations)
                self.parent.annotations = []
                self.parent.removeAllAnnotation()
            }
        }
        
        func mapView(_ mapView: MKMapView, regionWillChangeAnimated animated: Bool) {
            self.parent.regionWillChange()
        }

        // MARK: MKMapViewDelegate
        
        func mapView(_ mapView: MKMapView, didSelect view: MKAnnotationView) {
            if let v = view.annotation as? MKPointAnnotation {
                print(v.coordinate)
                mapView.setCenter(view.annotation?.coordinate ?? mapView.centerCoordinate, animated: true)
                self.parent.didSelect(v)
            }
        }
        
        func mapView(_ mapView: MKMapView,
                     didChange mode: MKUserTrackingMode,
                     animated: Bool) {

            if CLLocationManager.locationServicesEnabled() {
                switch mode {
                case .follow, .followWithHeading:
                    switch CLLocationManager.authorizationStatus() {
                    case .notDetermined:
                        parent.mapViewContainer.locationManager.requestWhenInUseAuthorization()
                    case .restricted:
                        // Possibly due to active restrictions such as parental controls being in place
                        let alert = UIAlertController(title: "Location Permission Restricted",
                                                      message: "The app cannot access your location. This is possibly due to active restrictions such as parental controls being in place. Please disable or remove them and enable location permissions in settings.",
                                                      preferredStyle: .alert)
                        alert.addAction(UIAlertAction(title: "Settings",
                                                      style: .default) { _ in
                            // Redirect to Settings app
                            UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
                        })
                        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
                        
                        present(alert)
                        
                        DispatchQueue.main.async {
                            self.parent.userTrackingMode.wrappedValue = .none
                        }
                    case .denied:
                        let alert = UIAlertController(title: "Location Permission Denied",
                                                      message: "Please enable location permissions in settings.",
                                                      preferredStyle: .alert)
                        alert.addAction(UIAlertAction(title: "Settings",
                                                      style: .default) { _ in
                            // Redirect to Settings app
                            UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
                        })
                        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
                        present(alert)
                        
                        DispatchQueue.main.async {
                            self.parent.userTrackingMode.wrappedValue = .none
                        }
                    default:
                        DispatchQueue.main.async {
                            self.parent.userTrackingMode.wrappedValue = mode
                        }
                    }
                default:
                    DispatchQueue.main.async {
                        self.parent.userTrackingMode.wrappedValue = mode
                    }
                }
            } else {
                let alert = UIAlertController(title: "Location Services Disabled",
                                              message: "Please enable location services in settings.",
                                              preferredStyle: .alert)
                alert.addAction(UIAlertAction(title: "Settings",
                                              style: .default) { _ in
                    // Redirect to Settings app
                    UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
                })
                alert.addAction(UIAlertAction(title: "Cancel",
                                              style: .cancel))
                present(alert)
                
                DispatchQueue.main.async {
                    self.parent.userTrackingMode.wrappedValue = mode
                }
            }
        }
        
        // MARK: CLLocationManagerDelegate
        
        func locationManager(_ manager: CLLocationManager,
                             didChangeAuthorization status: CLAuthorizationStatus) {
            #if DEBUG
            print("\(type(of: self)).\(#function): status=", terminator: "")
            switch status {
            case .notDetermined:       print(".notDetermined")
            case .restricted:          print(".restricted")
            case .denied:              print(".denied")
            case .authorizedAlways:    print(".authorizedAlways")
            case .authorizedWhenInUse: print(".authorizedWhenInUse")
            @unknown default:          print("@unknown")
            }
            #endif
            
            switch status {
            case .authorizedAlways, .authorizedWhenInUse:
                parent.mapViewContainer.locationManager.startUpdatingLocation()
                self.parent.centerMapOnUserLocation()
                parent.mapViewContainer.mapView.setUserTrackingMode(parent.userTrackingMode.wrappedValue, animated: true)
            default:
                parent.mapViewContainer.mapView.setUserTrackingMode(.none, animated: true)
            }
        }
    }
}

extension MKCoordinateRegion {
    var northWest: CLLocationCoordinate2D {
        return CLLocationCoordinate2D(latitude: center.latitude + span.latitudeDelta  / 2,
                                      longitude: center.longitude - span.longitudeDelta / 2)
    }
    var northEast: CLLocationCoordinate2D {
        return CLLocationCoordinate2D(latitude: center.latitude + span.latitudeDelta  / 2,
                                      longitude: center.longitude + span.longitudeDelta / 2)
    }
    var southWest: CLLocationCoordinate2D {
        return CLLocationCoordinate2D(latitude: center.latitude - span.latitudeDelta  / 2,
                                      longitude: center.longitude - span.longitudeDelta / 2)
    }
    var southEast: CLLocationCoordinate2D {
        return CLLocationCoordinate2D(latitude: center.latitude - span.latitudeDelta  / 2,
                                      longitude: center.longitude + span.longitudeDelta / 2)
    }
}

extension MKMapView {
    
    var zoomLevel: Int {
        let maxZoom: Double = 20
        let zoomScale = self.visibleMapRect.size.width / Double(self.frame.size.width)
        let zoomExponent = log2(zoomScale)
        return Int(maxZoom - ceil(zoomExponent))
    }
    
    func centerToLocation(
        _ location: CLLocation,
        regionRadius: CLLocationDistance = 1000
    ) {
        let coordinateRegion = MKCoordinateRegion(
            center: location.coordinate,
            latitudinalMeters: regionRadius,
            longitudinalMeters: regionRadius)
        setRegion(coordinateRegion, animated: true)
    }
}
