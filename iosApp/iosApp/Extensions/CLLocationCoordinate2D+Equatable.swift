//
//  CLLocationCoordinate2D+Equatable.swift
//  iosApp
//
//  Created by Çağatay Emekci on 10.01.2021.
//  Copyright © 2021 orgName. All rights reserved.
//

import MapKit

extension CLLocationCoordinate2D: Equatable {}

public func ==(lhs: CLLocationCoordinate2D, rhs: CLLocationCoordinate2D) -> Bool {
    return lhs.latitude == rhs.latitude && lhs.longitude == rhs.longitude
}
