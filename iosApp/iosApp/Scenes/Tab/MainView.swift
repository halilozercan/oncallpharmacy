//
//  TabView.swift
//  iosApp
//
//  Created by Çağatay Emekci on 20.12.2020.
//  Copyright © 2020 orgName. All rights reserved.
//

import SwiftUI
import shared
import MapKit
import Foundation
import ComposableArchitecture

struct AppState: Equatable {
    var mapView: MapState = MapState(cities:[], pharmacies: [], annotations: [], screenState: .list, showingDetail: false)
}

enum AppAction: Equatable {
    case mapView(MapAction)
}

struct AppEnvironment {
    
}

let appReducer =  Reducer<AppState, AppAction, AppEnvironment>.combine(
    mapReducer.pullback(
        state: \AppState.mapView,
        action: /AppAction.mapView,
        environment: { _ in MapEnvironment(mapClient: .live) }
    )
)


struct MainView: View {
    let store: Store<AppState, AppAction>
    
    var body: some View {
        ZStack {
            Color.backColor
            DashboardView(store: self.store.scope(
                state: { $0.mapView },
                action: { .mapView($0) }
            ))
        }
    }
}

