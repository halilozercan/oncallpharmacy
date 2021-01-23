//
//  DashboardViewModel.swift
//  iosApp
//
//  Created by Çağatay Emekci on 20.12.2020.
//  Copyright © 2020 orgName. All rights reserved.
//

import Foundation
import shared
import MapKit
import ComposableArchitecture
import Combine

public enum ScreenState: Equatable {
    case list
    case detail(pharmacy: Pharmacy)
}

public enum MapAction: Equatable {
    case selectAnnotation(MKPointAnnotation)
    case regionWillChange
    case regionDidChange(Coordinates, LocationBounds)
    case showDetail(Bool)
    case annotationTo(Pharmacy)
    case didClickListItem(Pharmacy)
    case getAll([Pharmacy])
    case addAnnotations([MKPointAnnotation])
    case removeAllAnnotation
}

public struct MapState: Equatable {
    var cities: [City]
    var pharmacies: [Pharmacy]
    var annotations: [MKPointAnnotation]
    var screenState: ScreenState
    var showingDetail: Bool
}
struct MapEnvironment {
    var mapClient: MapClient
}

let mapReducer = Reducer<MapState, MapAction, MapEnvironment> { state, action, environment in
    switch action {
    case .selectAnnotation(let annotation):
        return environment.mapClient.findAnnotationFromPharmacies(state.pharmacies, annotation)
            .eraseToEffect()
    case .regionWillChange:
        state.showingDetail = false
        return .none
    case .regionDidChange(let coordinate, let bounds):
        return environment.mapClient.getPharmaciesByLocation(coordinate, bounds)
            .map(MapAction.getAll)
            .eraseToEffect()
    case .showDetail(let detail):
        state.showingDetail = detail
        return .none
    case .annotationTo(let pharmacy):
        state.showingDetail = true
        var copy = state.pharmacies
        copy.change(pharmacy, to: 0)
        state.pharmacies = copy
        state.screenState = .detail(pharmacy: pharmacy)
        return .none
    case .getAll(let pharmacies):
        state.pharmacies = pharmacies
        return  environment.mapClient.crateAnnotation(pharmacies)
            .eraseToEffect()
    case .addAnnotations(let annotations):
        state.annotations = annotations
        return .none
    case .removeAllAnnotation:
        state.annotations = []
        state.pharmacies = []
        return .none
    case .didClickListItem(let pharmacy):
        return .none
    }
    
}


private let pharmacyRepository: PharmacyRepository = PharmacyRepository()

private func getPharmacies(center: Coordinates,
                           bounds: LocationBounds) -> Future<[Pharmacy], Never> {
    return Future { promise in
        pharmacyRepository
            .getPharmaciesByLocation(center: center,
                                     bounds: bounds)
            .watch { array in
                guard let pharmacies = array as? [Pharmacy] else {
                    return
                }
                promise(.success(pharmacies))
            }
    }
}

struct MapClient {
    private let pharmacyRepository: PharmacyRepository = PharmacyRepository()
    var findAnnotationFromPharmacies: ([Pharmacy], MKPointAnnotation) -> Effect<MapAction, Never>
    var getPharmaciesByLocation: (Coordinates, LocationBounds) -> Effect<[Pharmacy], Never>
    var crateAnnotation: ([Pharmacy]) -> Effect<MapAction, Never>
    
    enum Error: Swift.Error, Equatable {
        case taskError
    }
}

extension MapClient {
    static let live = MapClient(findAnnotationFromPharmacies: { allPharmacies, annotation in
        Effect <MapAction, Never>(
            value:
                .annotationTo(
                    allPharmacies
                        .filter { pharmacy in
                            if pharmacy.longitude == annotation.coordinate.longitude
                                &&
                                pharmacy.latitude == annotation.coordinate.latitude {
                                return true
                            }
                            return false
                        }
                        .first ?? allPharmacies[0]))
    }, getPharmaciesByLocation: { coordinate, bounds in
        getPharmacies(
            center: coordinate,
            bounds: bounds
        )
        .eraseToEffect()
    }, crateAnnotation: { selectedPharmacies in
        Effect<MapAction, Never>(value:
                                    .addAnnotations(
                                        selectedPharmacies
                                            .map { pharmacy -> MKPointAnnotation in
                                                let annotation = MKPointAnnotation()
                                                annotation.title = pharmacy.name
                                                annotation.coordinate = CLLocationCoordinate2D(
                                                    latitude: pharmacy.latitude,
                                                    longitude: pharmacy.longitude
                                                )
                                                return annotation
                                            }))
    })
}

private var dependencies: [AnyHashable: SpeechDependencies] = [:]

private struct SpeechDependencies {
    let pharmacyRepository: PharmacyRepository
    func finish() {
    }
    
    func cancel() {
    }
}


extension Array where Element: Equatable {
    mutating func change(_ element: Element, to newIndex: Index) {
        if let firstIndex = self.firstIndex(of: element) {
            self.insert(element, at: 0)
            self.remove(at: firstIndex + 1)
        }
    }
}
